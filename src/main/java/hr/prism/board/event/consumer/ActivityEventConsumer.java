package hr.prism.board.event.consumer;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.Role;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.service.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.prism.board.enums.State.PENDING;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Component
public class ActivityEventConsumer {

    private final ActivityService activityService;

    private final ResourceService resourceService;

    private final UserRoleService userRoleService;

    private final ResourceEventService resourceEventService;

    private final UserService userService;

    @Inject
    public ActivityEventConsumer(ActivityService activityService, ResourceService resourceService,
                                 UserRoleService userRoleService, ResourceEventService resourceEventService,
                                 UserService userService) {
        this.activityService = activityService;
        this.resourceService = resourceService;
        this.userRoleService = userRoleService;
        this.resourceEventService = resourceEventService;
        this.userService = userService;
    }

    @Async
    @TransactionalEventListener
    public void consume(ActivityEvent activityEvent) {
        Long userRoleId = null;
        Long resourceEventId = null;

        Class<? extends BoardEntity> entityClass = activityEvent.getEntityClass();
        if (entityClass == UserRole.class) {
            userRoleId = activityEvent.getEntityId();
        } else if (entityClass == ResourceEvent.class) {
            resourceEventId = activityEvent.getEntityId();
        } else if (entityClass != null) {
            throw new UnsupportedOperationException("No known activities for type: " + entityClass.getSimpleName());
        }

        Resource resource;
        if (resourceEventId != null) {
            resource = processResourceEvent(activityEvent, resourceEventId);
        } else if (userRoleId != null) {
            resource = processUserRole(activityEvent, userRoleId);
        } else {
            resource = processResource(activityEvent);
        }

        activityService.sendActivities(resource);
    }

    private Resource processResourceEvent(ActivityEvent activityEvent, Long resourceEventId) {
        ResourceEvent resourceEvent = resourceEventService.getById(resourceEventId);

        Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> createdActivities = new HashMap<>();
        activityEvent.getActivities().forEach(activity -> {
            Activity activityEnum = activity.getActivity();
            hr.prism.board.domain.Activity activityEntity =
                createdActivities.computeIfAbsent(
                    Pair.of(resourceEvent, activityEnum),
                    value -> activityService.createOrUpdateActivity(resourceEvent, activityEnum));
            activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
        });

        return resourceEvent.getResource();
    }

    private Resource processUserRole(ActivityEvent activityEvent, Long userRoleId) {
        UserRole userRole = userRoleService.getById(userRoleId);

        if (userRole.getState() == PENDING) {
            Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> createdActivities = new HashMap<>();
            activityEvent.getActivities().forEach(activity -> {
                Activity activityEnum = activity.getActivity();
                hr.prism.board.domain.Activity activityEntity =
                    createdActivities.computeIfAbsent(
                        Pair.of(userRole, activityEnum),
                        value -> activityService.createOrUpdateActivity(userRole, activityEnum));
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            });
        } else {
            activityService.deleteActivities(userRole);
        }

        return userRole.getResource();
    }

    private Resource processResource(ActivityEvent activityEvent) {
        Resource resource = resourceService.getById(activityEvent.getResourceId());
        List<hr.prism.board.workflow.Activity> activities = activityEvent.getActivities();
        if (isEmpty(activities)) {
            activityService.sendActivities(resource);
            return resource;
        }

        Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> createdActivities = new HashMap<>();
        activityEvent.getActivities().forEach(activity -> {
            Activity activityEnum = activity.getActivity();
            hr.prism.board.domain.Activity activityEntity =
                createdActivities.computeIfAbsent(
                    Pair.of(resource, activityEnum),
                    value -> activityService.createOrUpdateActivity(resource, activityEnum));

            Role role = activity.getRole();
            Long userId = activity.getUserId();
            if (role != null) {
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            } else {
                User user = userService.getById(userId);
                activityService.getOrCreateActivityUser(activityEntity, user);
            }
        });

        return resource;
    }

}
