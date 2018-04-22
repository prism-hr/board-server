package hr.prism.board.event.consumer;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.service.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Component
public class ActivityEventConsumer {

    private final ActivityService activityService;

    private final ResourceService resourceService;

    private final NewUserRoleService userRoleService;

    private final ResourceEventService resourceEventService;

    private final NewUserService userService;

    @Inject
    public ActivityEventConsumer(ActivityService activityService, ResourceService resourceService,
                                 NewUserRoleService userRoleService, ResourceEventService resourceEventService,
                                 NewUserService userService) {
        this.activityService = activityService;
        this.resourceService = resourceService;
        this.userRoleService = userRoleService;
        this.resourceEventService = resourceEventService;
        this.userService = userService;
    }

    @Async
    @TransactionalEventListener
    public void consume(ActivityEvent activityEvent) {
        Resource resource;
        Class<? extends BoardEntity> entityClass = activityEvent.getEntityClass();

        Long userRoleId = null;
        Long resourceEventId = null;
        if (entityClass == UserRole.class) {
            userRoleId = activityEvent.getEntityId();
        } else if (entityClass == ResourceEvent.class) {
            resourceEventId = activityEvent.getEntityId();
        } else if (entityClass != null) {
            throw new UnsupportedOperationException("No known activities for type: " + entityClass.getSimpleName());
        }

        Map<Pair<BoardEntity, hr.prism.board.enums.Activity>, hr.prism.board.domain.Activity>
            activityEntitiesByEntity = new HashMap<>();
        if (resourceEventId != null) {
            resource = processResourceEvent(activityEvent, resourceEventId, activityEntitiesByEntity);
        } else if (userRoleId != null) {
            resource = processUserRole(activityEvent, userRoleId, activityEntitiesByEntity);
        } else {
            resource = processResource(activityEvent, activityEntitiesByEntity);
        }

        activityService.sendActivities(resource);
    }

    private Resource processResourceEvent(
        ActivityEvent activityEvent, Long resourceEventId,
        Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity) {
        Resource resource;
        ResourceEvent resourceEvent = resourceEventService.getById(resourceEventId);
        activityEvent.getActivities().forEach(activity -> {
            Activity activityEnum = activity.getActivity();
            hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity.computeIfAbsent(
                Pair.of(resourceEvent, activityEnum),
                value -> activityService.createOrUpdateActivity(resourceEvent, activityEnum));
            activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
        });

        resource = resourceEvent.getResource();
        return resource;
    }

    private Resource processUserRole(
        ActivityEvent activityEvent, Long userRoleId,
        Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity) {
        UserRole userRole = userRoleService.getById(userRoleId);
        if (userRole.getState() == State.PENDING) {
            activityEvent.getActivities().forEach(activity -> {
                Activity activityEnum = activity.getActivity();
                hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity.computeIfAbsent(
                    Pair.of(userRole, activityEnum),
                    value -> activityService.createOrUpdateActivity(userRole, activityEnum));
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            });
        } else {
            activityService.deleteActivities(userRole);
        }

        return userRole.getResource();
    }

    private Resource processResource(
        ActivityEvent activityEvent,
        Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity) {
        Resource resource = resourceService.findOne(activityEvent.getResourceId());
        if (activityEvent.isStateChange()) {
            activityService.deleteActivities(resource);
        }

        activityEvent.getActivities().forEach(activity -> {
            Activity activityEnum = activity.getActivity();
            hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity.computeIfAbsent(
                Pair.of(resource, activityEnum),
                value -> activityService.createOrUpdateActivity(resource, activityEnum));

            Long userId = activity.getUserId();
            if (userId == null) {
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            } else {
                User user = userService.getById(userId);
                activityService.getOrCreateActivityUser(activityEntity, user);
            }
        });

        return resource;
    }

}
