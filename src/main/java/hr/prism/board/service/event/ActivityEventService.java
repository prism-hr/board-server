package hr.prism.board.service.event;

import hr.prism.board.domain.*;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.service.*;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.workflow.Activity;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ActivityEventService {

    @Inject
    private ActivityService activityService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private ResourceEventService resourceEventService;

    @Inject
    private UserActivityService userActivityService;

    @Inject
    private UserService userService;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long resourceId, List<Activity> activities) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, activities));
    }

    public void publishEvent(Object source, Long resourceId, BoardEntity entity, List<Activity> activities) {
        Class<? extends BoardEntity> entityClass = entity.getClass();
        if (entityClass == UserRole.class) {
            applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, entity.getId(), null, activities));
        } else if (entityClass == ResourceEvent.class) {
            applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, null, entity.getId(), activities));
        }
    }

    public void publishEvent(Object source, Long resourceId, Long userRoleId) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, userRoleId));
    }

    @Async
    @TransactionalEventListener
    public void sendActivitiesAsync(ActivityEvent activityEvent) {
        sendActivities(activityEvent);
    }

    protected void sendActivities(ActivityEvent activityEvent) {
        Resource resource;
        Long userRoleId = activityEvent.getUserRoleId();
        Long resourceEventId = activityEvent.getResourceEventId();
        Map<Pair<BoardEntity, hr.prism.board.enums.Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity = new HashMap<>();
        if (resourceEventId != null) {
            ResourceEvent resourceEvent = resourceEventService.findOne(resourceEventId);
            activityEvent.getActivities().forEach(activity -> {
                hr.prism.board.enums.Activity activityEnum = activity.getActivity();
                hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                    .computeIfAbsent(Pair.of(resourceEvent, activityEnum), value -> activityService.getOrCreateActivity(resourceEvent, activityEnum));
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            });

            resource = resourceEvent.getResource();
        } else if (userRoleId != null) {
            UserRole userRole = userRoleService.fineOne(userRoleId);
            if (userRole.getState() == State.PENDING) {
                activityEvent.getActivities().forEach(activity -> {
                    hr.prism.board.enums.Activity activityEnum = activity.getActivity();
                    hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                        .computeIfAbsent(Pair.of(userRole, activityEnum), value -> activityService.getOrCreateActivity(userRole, activityEnum));
                    activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
                });
            } else {
                activityService.deleteActivities(userRole);
            }

            resource = userRole.getResource();
            userRoleCacheService.updateUserRolesSummary(resource);
        } else {
            resource = resourceService.findOne(activityEvent.getResourceId());
            activityService.deleteActivities(resource);
            activityEvent.getActivities().forEach(activity -> {
                hr.prism.board.enums.Activity activityEnum = activity.getActivity();
                hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                    .computeIfAbsent(Pair.of(resource, activityEnum), value -> activityService.getOrCreateActivity(resource, activityEnum));

                Long userId = activity.getUserId();
                if (userId == null) {
                    activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
                } else {
                    User user = userCacheService.findOne(userId);
                    activityService.getOrCreateActivityUser(activityEntity, user);
                }
            });
        }

        Collection<Long> userIds = userActivityService.getUserIds();
        if (!userIds.isEmpty()) {
            for (Long userId : userService.findByResourceAndUserIds(resource, userIds)) {
                userActivityService.processRequests(userId, activityService.getActivities(userId));
            }
        }
    }

}
