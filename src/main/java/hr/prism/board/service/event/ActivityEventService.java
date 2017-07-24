package hr.prism.board.service.event;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.UserRole;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.service.*;
import hr.prism.board.workflow.Activity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

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
    private UserActivityService userActivityService;

    @Inject
    private UserService userService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long resourceId, List<Activity> activities) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, activities));
    }

    public void publishEvent(Object source, Long resourceId, Long userRoleId, List<Activity> activities) {
        applicationEventPublisher.publishEvent(new ActivityEvent(source, resourceId, userRoleId, activities));
    }

    @Async
    @TransactionalEventListener
    public void sendActivitiesAsync(ActivityEvent activityEvent) {
        sendActivities(activityEvent);
    }

    protected void sendActivities(ActivityEvent activityEvent) {
        Resource resource;
        Long userRoleId = activityEvent.getUserRoleId();
        if (userRoleId == null) {
            resource = resourceService.findOne(activityEvent.getResourceId());
            activityService.deleteActivities(resource);
            activityEvent.getActivities().forEach(activity ->
                activityService.getOrCreateActivity(resource, activity.getScope(), activity.getRole(), activity.getActivity()));
        } else {
            UserRole userRole = userRoleService.fineOne(userRoleId);
            activityEvent.getActivities().forEach(activity ->
                activityService.getOrCreateActivity(userRole, activity.getScope(), activity.getRole(), activity.getActivity()));
            resource = userRole.getResource();
        }

        Collection<Long> userIds = userActivityService.getUserIds();
        if (!userIds.isEmpty()) {
            for (Long userId : userService.findByResourceAndUserIds(resource, userIds)) {
                userActivityService.processRequests(userId, activityService.getActivities(userId));
            }
        }
    }

}
