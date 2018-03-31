package hr.prism.board.service.event;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.ResourceEventService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.UserRoleService;
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
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "WeakerAccess"})
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
    private UserCacheService userCacheService;

    @Inject
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long resourceId, boolean stateChange, List<hr.prism.board.workflow.Activity> activities) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, stateChange, activities));
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

    public void sendActivities(ActivityEvent activityEvent) {
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

        Map<Pair<BoardEntity, hr.prism.board.enums.Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity = new HashMap<>();
        if (resourceEventId != null) {
            resource = processResourceEvent(activityEvent, resourceEventId, activityEntitiesByEntity);
        } else if (userRoleId != null) {
            resource = processUserRole(activityEvent, userRoleId, activityEntitiesByEntity);
        } else {
            resource = processResource(activityEvent, activityEntitiesByEntity);
        }

        activityService.sendActivities(resource);
    }

    private Resource processResourceEvent(ActivityEvent activityEvent, Long resourceEventId, Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity>
        activityEntitiesByEntity) {
        Resource resource;
        ResourceEvent resourceEvent = resourceEventService.findOne(resourceEventId);
        activityEvent.getActivities().forEach(activity -> {
            Activity activityEnum = activity.getActivity();
            hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                .computeIfAbsent(Pair.of(resourceEvent, activityEnum), value -> activityService.createOrUpdateActivity(resourceEvent, activityEnum));
            activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
        });

        resource = resourceEvent.getResource();
        return resource;
    }

    private Resource processUserRole(ActivityEvent activityEvent, Long userRoleId, Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity) {
        UserRole userRole = userRoleService.fineOne(userRoleId);
        if (userRole.getState() == State.PENDING) {
            activityEvent.getActivities().forEach(activity -> {
                Activity activityEnum = activity.getActivity();
                hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                    .computeIfAbsent(Pair.of(userRole, activityEnum), value -> activityService.createOrUpdateActivity(userRole, activityEnum));
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            });
        } else {
            activityService.deleteActivities(userRole);
        }

        return userRole.getResource();
    }

    private Resource processResource(ActivityEvent activityEvent, Map<Pair<BoardEntity, Activity>, hr.prism.board.domain.Activity> activityEntitiesByEntity) {
        Resource resource = resourceService.findOne(activityEvent.getResourceId());
        if (activityEvent.isStateChange()) {
            activityService.deleteActivities(resource);
        }

        activityEvent.getActivities().forEach(activity -> {
            Activity activityEnum = activity.getActivity();
            hr.prism.board.domain.Activity activityEntity = activityEntitiesByEntity
                .computeIfAbsent(Pair.of(resource, activityEnum), value -> activityService.createOrUpdateActivity(resource, activityEnum));

            Long userId = activity.getUserId();
            if (userId == null) {
                activityService.getOrCreateActivityRole(activityEntity, activity.getScope(), activity.getRole());
            } else {
                User user = userCacheService.findOne(userId);
                activityService.getOrCreateActivityUser(activityEntity, user);
            }
        });

        return resource;
    }

}
