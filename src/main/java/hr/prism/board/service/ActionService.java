package hr.prism.board.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.interceptor.StateChangeInterceptor;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Execution;
import hr.prism.board.workflow.Notification;
import hr.prism.board.workflow.Update;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class ActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

    @Inject
    private ResourceService resourceService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ApplicationContext applicationContext;

    public Resource executeAction(User user, Resource resource, Action action, Execution execution) {
        List<ActionRepresentation> actions = resource.getActions();
        if (actions != null) {
            for (ActionRepresentation actionRepresentation : actions) {
                if (actionRepresentation.getAction() == action) {
                    Resource newResource = execution.execute();
                    State state = newResource.getState();
                    State newState = actionRepresentation.getState();
                    if (newState == null) {
                        newState = state;
                    } else if (newState == State.PREVIOUS) {
                        newState = newResource.getPreviousState();
                    }

                    Class<? extends StateChangeInterceptor> interceptorClass = newResource.getScope().stateChangeInterceptorClass;
                    if (interceptorClass != null) {
                        newState = applicationContext.getBean(interceptorClass).intercept(user, newResource, action, newState);
                    }

                    boolean stateChanged = newState != state;
                    if (stateChanged) {
                        resourceService.updateState(newResource, newState);
                        newResource = resourceService.getResource(user, newResource.getScope(), newResource.getId());
                    }

                    if (!resource.equals(newResource) || stateChanged || CollectionUtils.isNotEmpty(newResource.getChangeList())) {
                        resourceService.createResourceOperation(newResource, action, user);
                    }

                    Long newResourceId = newResource.getId();
                    List<Activity> activities = deserializeUpdates(actionRepresentation.getActivity(), Activity.class);
                    if (activities != null) {
                        activityEventService.publishEvent(this, newResourceId, activities);
                    }

                    List<Notification> notifications = deserializeUpdates(actionRepresentation.getNotification(), Notification.class);
                    if (notifications != null) {
                        notificationEventService.publishEvent(this, newResourceId, action, notifications);
                    }

                    return newResource;
                }
            }
        }

        if (user == null) {
            LOGGER.info("Public user cannot " + action.name().toLowerCase() + " " + resource.toString());
            throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        LOGGER.info(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
        throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_ACTION);
    }

    public boolean canExecuteAction(Resource resource, Action action) {
        List<ActionRepresentation> actions = resource.getActions();
        return actions != null && actions.stream().map(ActionRepresentation::getAction).anyMatch(action::equals);
    }

    private <T extends Update<T>> List<T> deserializeUpdates(String serializedUpdates, Class<T> updateClass) {
        if (serializedUpdates != null) {
            try {
                return objectMapper.readValue(serializedUpdates, new TypeReference<List<T>>() {
                });
            } catch (IOException e) {
                throw new IllegalStateException("Could not deserialize " + updateClass.getSimpleName().toLowerCase() + " definitions", e);
            }
        }

        return null;
    }

}
