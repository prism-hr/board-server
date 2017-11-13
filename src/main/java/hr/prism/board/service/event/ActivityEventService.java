package hr.prism.board.service.event;

import hr.prism.board.domain.*;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.service.*;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
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
    private ResourceTaskService resourceTaskService;

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

    public void publishEvent(Object source, Long resourceId, List<hr.prism.board.workflow.Activity> activities) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, activities));
    }

    public void publishEvent(Object source, Long resourceId, BoardEntity entity) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, entity.getClass(), entity.getId()));
    }

    public void publishEvent(Object source, Long resourceId, BoardEntity entity, List<hr.prism.board.workflow.Activity> activities) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, entity.getClass(), entity.getId(), activities));
    }

    @Async
    @SuppressWarnings("unused")
    @TransactionalEventListener
    public void sendActivitiesAsync(ActivityEvent activityEvent) {
        sendActivities(activityEvent);
    }

    void sendActivities(ActivityEvent activityEvent) {
        Resource resource;
        Class<? extends BoardEntity> entityClass = activityEvent.getEntityClass();

        Long userRoleId = null;
        Long resourceEventId = null;
        Long resourceTaskId = null;
        if (entityClass == UserRole.class) {
            userRoleId = activityEvent.getEntityId();
        } else if (entityClass == ResourceEvent.class) {
            resourceEventId = activityEvent.getEntityId();
        } else if (entityClass == ResourceTask.class) {
            resourceTaskId = activityEvent.getEntityId();
        } else if (entityClass != null) {
            throw new UnsupportedOperationException("No registered activities for type: " + entityClass.getSimpleName());
        }

        Map<Pair<BoardEntity, hr.prism.board.enums.Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity = new HashMap<>();
        if (resourceTaskId != null) {
            ResourceTask resourceTask = resourceTaskService.findOne(resourceTaskId);
            activityEvent.getActivities().forEach(activity -> {
                hr.prism.board.enums.Activity activityEnum = activity.getActivity();
                hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                    .computeIfAbsent(Pair.of(resourceTask, activityEnum), value -> activityService.getOrCreateActivity(resourceTask, activityEnum));
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            });

            resource = resourceTask.getResource();
        } else if (resourceEventId != null) {
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

        List<Long> userIds = userActivityService.getUserIds();
        if (!userIds.isEmpty()) {
            for (Long userId : userService.findByResourceAndUserIds(resource, userIds)) {
                userActivityService.processRequests(userId, activityService.getActivities(userId));
            }
        }
    }

}
