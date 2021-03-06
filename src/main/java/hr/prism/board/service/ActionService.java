package hr.prism.board.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.interceptor.StateChangeInterceptor;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Execution;
import hr.prism.board.workflow.Notification;
import hr.prism.board.workflow.Update;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.State.PREVIOUS;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class ActionService {

    private static final Logger LOGGER = getLogger(ActionService.class);

    private final ResourceService resourceService;

    private final ActivityService activityService;

    private final EventProducer eventProducer;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    @Inject
    public ActionService(ResourceService resourceService, ActivityService activityService, EventProducer eventProducer,
                         ObjectMapper objectMapper, ApplicationContext applicationContext) {
        this.resourceService = resourceService;
        this.activityService = activityService;
        this.eventProducer = eventProducer;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }

    public Resource executeAction(User user, Resource resource, Action action, Execution execution) {
        List<ActionRepresentation> actions = resource.getActions();
        if (isNotEmpty(actions)) {
            for (ActionRepresentation actionRepresentation : actions) {
                if (actionRepresentation.getAction() == action) {
                    Resource newResource = execution.execute();
                    State state = newResource.getState();
                    State newState = getNewState(actionRepresentation, newResource, state);

                    boolean stateChanged = newState != state;
                    if (stateChanged) {
                        resourceService.updateState(newResource, newState);
                        newResource = resourceService.getResource(user, newResource.getScope(), newResource.getId());
                    }

                    if (!resource.equals(newResource) || stateChanged || isNotEmpty(newResource.getChangeList())) {
                        resourceService.createResourceOperation(newResource, action, user);
                    }

                    sendNotifications(newResource, action, actionRepresentation);
                    return newResource;
                }
            }
        }

        String userString = user == null ? "Anonymous" : user.toString();
        LOGGER.info(userString + " cannot " + action.name().toLowerCase() + " " + resource.toString());
        throw new BoardForbiddenException(FORBIDDEN_ACTION, "Action cannot be performed");
    }

    public void executeAnonymously(List<Long> resourceIds, Action action, State state, LocalDateTime baseline) {
        resourceService.updateStates(resourceIds, action, state, baseline);
    }

    public boolean canExecuteAction(Resource resource, Action action) {
        List<ActionRepresentation> actions = resource.getActions();
        return actions != null && actions.stream().map(ActionRepresentation::getAction).anyMatch(action::equals);
    }

    private State getNewState(ActionRepresentation actionRepresentation, Resource resource, State state) {
        State newState = actionRepresentation.getState();
        if (newState == null) {
            newState = state;
        } else if (newState == PREVIOUS) {
            newState = resource.getPreviousState();
        }

        Class<? extends StateChangeInterceptor> interceptorClass = resource.getScope().stateChangeInterceptorClass;
        if (interceptorClass != null) {
            newState = applicationContext.getBean(interceptorClass).intercept(resource, newState);
        }

        return newState;
    }

    private void sendNotifications(Resource resource, Action action, ActionRepresentation actionRepresentation) {
        Long resourceId = resource.getId();
        List<Activity> activities = deserializeUpdates(actionRepresentation.getActivity(), Activity.class);
        if (isNotEmpty(activities)) {
            activityService.deleteActivities(resource);
            eventProducer.produce(
                new ActivityEvent(this, resourceId, activities));
        }

        List<Notification> notifications =
            deserializeUpdates(actionRepresentation.getNotification(), Notification.class);
        if (isNotEmpty(notifications)) {
            eventProducer.produce(
                new NotificationEvent(this, resourceId, action, notifications));
        }
    }

    private <T extends Update<T>> List<T> deserializeUpdates(String serializedUpdates, Class<T> updateClass) {
        if (serializedUpdates != null) {
            try {
                return objectMapper.readValue(serializedUpdates, new TypeReference<List<T>>() {
                });
            } catch (IOException e) {
                throw new IllegalStateException(
                    "Could not deserialize " + updateClass.getSimpleName().toLowerCase() + " definitions", e);
            }
        }

        return null;
    }

}
