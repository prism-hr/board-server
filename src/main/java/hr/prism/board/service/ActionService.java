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
import hr.prism.board.exception.BoardNotFoundException;
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
import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.State.PREVIOUS;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static hr.prism.board.exception.ExceptionCode.RESOURCE_NOT_FOUND;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class ActionService {

    private static final Logger LOGGER = getLogger(ActionService.class);

    private final ResourceService resourceService;

    private final EventProducer eventProducer;

    private final ObjectMapper objectMapper;

    private final EntityManager entityManager;

    private final ApplicationContext applicationContext;

    @Inject
    public ActionService(ResourceService resourceService, EventProducer eventProducer, ObjectMapper objectMapper,
                         EntityManager entityManager, ApplicationContext applicationContext) {
        this.resourceService = resourceService;
        this.eventProducer = eventProducer;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.applicationContext = applicationContext;
    }

    public Resource executeAction(User user, Resource resource, Action action, Execution execution) {
        if (resource == null) {
            throw new BoardNotFoundException(RESOURCE_NOT_FOUND);
        }

        List<ActionRepresentation> actions = resource.getActions();
        if (actions != null) {
            for (ActionRepresentation actionRepresentation : actions) {
                if (actionRepresentation.getAction() == action) {
                    Resource newResource = execution.execute();
                    State state = newResource.getState();
                    State newState = getNewState(user, action, actionRepresentation, newResource, state);

                    boolean stateChanged = newState != state;
                    if (stateChanged) {
                        resourceService.updateState(newResource, newState);
                        newResource = resourceService.getResource(user, newResource.getScope(), newResource.getId());
                    }

                    if (!resource.equals(newResource) || stateChanged || isNotEmpty(newResource.getChangeList())) {
                        resourceService.createResourceOperation(newResource, action, user);
                    }

                    Long newResourceId = newResource.getId();
                    sendNotifications(action, actionRepresentation, newResourceId);
                    return newResource;
                }
            }
        }

        if (user == null) {
            LOGGER.info("Public user cannot " + action.name().toLowerCase() + " " + resource.toString());
            throw new BoardForbiddenException(FORBIDDEN_ACTION, "User not authorized");
        }

        LOGGER.info(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
        throw new BoardForbiddenException(FORBIDDEN_ACTION, "User cannot perform action");
    }

    @SuppressWarnings("JpaQlInspection")
    public void executeAnonymously(List<Long> resourceIds, Action action, State newState, LocalDateTime baseline) {
        entityManager.createQuery(
            "update Resource resource " +
                "set resource.previousState = resource.state, " +
                "resource.state = :newState, " +
                "resource.stateChangeTimestamp = :baseline, " +
                "resource.updatedTimestamp = :baseline " +
                "where resource.id in (:resourceIds)")
            .setParameter("newState", newState)
            .setParameter("baseline", baseline)
            .setParameter("resourceIds", resourceIds)
            .executeUpdate();

        //noinspection SqlResolve
        entityManager.createNativeQuery(
            "INSERT INTO resource_operation (resource_id, action, creator_id, created_timestamp, updated_timestamp) " +
                "SELECT resource.id, :action, resource.creator_id, :baseline, :baseline " +
                "FROM resource " +
                "WHERE resource.id IN (:postIds) " +
                "ORDER BY resource.id")
            .setParameter("action", action.name())
            .setParameter("baseline", baseline)
            .setParameter("postIds", resourceIds)
            .executeUpdate();
    }

    public boolean canExecuteAction(Resource resource, Action action) {
        List<ActionRepresentation> actions = resource.getActions();
        return actions != null && actions.stream().map(ActionRepresentation::getAction).anyMatch(action::equals);
    }

    private State getNewState(User user, Action action, ActionRepresentation actionRepresentation,
                              Resource newResource, State state) {
        State newState = actionRepresentation.getState();
        if (newState == null) {
            newState = state;
        } else if (newState == PREVIOUS) {
            newState = newResource.getPreviousState();
        }

        Class<? extends StateChangeInterceptor> interceptorClass =
            newResource.getScope().stateChangeInterceptorClass;
        if (interceptorClass != null) {
            newState =
                applicationContext.getBean(interceptorClass).intercept(user, newResource, action, newState);
        }
        return newState;
    }

    private void sendNotifications(Action action, ActionRepresentation actionRepresentation, Long newResourceId) {
        List<Activity> activities = deserializeUpdates(actionRepresentation.getActivity(), Activity.class);
        if (activities != null) {
            eventProducer.produce(
                new ActivityEvent(this, newResourceId, true, activities));
        }

        List<Notification> notifications =
            deserializeUpdates(actionRepresentation.getNotification(), Notification.class);
        if (notifications != null) {
            eventProducer.produce(
                new NotificationEvent(this, newResourceId, action, notifications));
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
