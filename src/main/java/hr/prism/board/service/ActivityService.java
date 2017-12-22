package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.ActivityMapper;
import hr.prism.board.repository.ActivityEventRepository;
import hr.prism.board.repository.ActivityRepository;
import hr.prism.board.repository.ActivityRoleRepository;
import hr.prism.board.repository.ActivityUserRepository;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityService {

    @Inject
    private ActivityRepository activityRepository;

    @Inject
    private ActivityRoleRepository activityRoleRepository;

    @Inject
    private ActivityUserRepository activityUserRepository;

    @Inject
    private ActivityEventRepository activityEventRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserActivityService userActivityService;

    @Inject
    private ActivityMapper activityMapper;

    public Activity findByResourceAndActivity(Resource resource, hr.prism.board.enums.Activity activity) {
        return activityRepository.findByResourceAndActivity(resource, activity);
    }

    public Activity findByUserRoleAndActivity(UserRole userRole, hr.prism.board.enums.Activity activity) {
        return activityRepository.findByUserRoleAndActivity(userRole, activity);
    }

    public List<ActivityRepresentation> getActivities(Long userId) {
        List<Activity> activities =
            activityRepository.findByUserId(userId, State.ACTIVE_USER_ROLE_STATES, CategoryType.MEMBER, hr.prism.board.enums.ActivityEvent.DISMISSAL);
        if (activities.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> viewedActivityIds = activityRepository.findIdsByActivitiesAndUserIdAndEvent(activities, userId, hr.prism.board.enums.ActivityEvent.VIEW);
        return activities.stream().map(activity -> activity.setViewed(viewedActivityIds.contains(activity.getId()))).map(activityMapper).collect(Collectors.toList());
    }

    public Activity getOrCreateActivity(Resource resource, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByResourceAndActivity(resource, activity);
        if (entity == null) {
            entity = createActivity(resource, null, null, activity);
        }

        return entity;
    }

    public Activity getOrCreateActivity(UserRole userRole, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByUserRoleAndActivity(userRole, activity);
        if (entity == null) {
            entity = createActivity(userRole.getResource(), userRole, null, activity);
        }

        return entity;
    }

    public Activity getOrCreateActivity(ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByResourceEventAndActivity(resourceEvent, activity);
        if (entity == null) {
            entity = createActivity(resourceEvent.getResource(), null, resourceEvent, activity);
        }

        return entity;
    }

    public ActivityRole getOrCreateActivityRole(Activity activity, Scope scope, Role role) {
        ActivityRole activityRole = activityRoleRepository.findByActivityAndScopeAndRole(activity, scope, role);
        if (activityRole == null) {
            activityRole = activityRoleRepository.save(new ActivityRole().setActivity(activity).setScope(scope).setRole(role));
        }

        return activityRole;
    }

    public ActivityUser getOrCreateActivityUser(Activity activity, User user) {
        ActivityUser activityUser = activityUserRepository.findByActivityAndUser(activity, user);
        if (activityUser == null) {
            activityUser = activityUserRepository.save(new ActivityUser().setActivity(activity).setUser(user));
        }

        return activityUser;
    }

    public List<ActivityEvent> findViews(Collection<Activity> activities, User user) {
        return activityEventRepository.findByActivitiesAndUserAndEvent(activities, user, hr.prism.board.enums.ActivityEvent.VIEW);
    }

    public void viewActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        Activity activity = activityRepository.findOne(activityId);
        viewActivity(activity, user);
    }

    public void viewActivity(Activity activity, User user) {
        if (activity != null) {
            ActivityEvent activityEvent = activityEventRepository.findByActivityAndUserAndEvent(activity, user, hr.prism.board.enums.ActivityEvent.VIEW);
            if (activityEvent == null) {
                activityEventRepository.save(new ActivityEvent().setActivity(activity).setUser(user).setEvent(hr.prism.board.enums.ActivityEvent.VIEW).setEventCount(1L));
            } else {
                activityEvent.setEventCount(activityEvent.getEventCount() + 1);
                activityEventRepository.update(activityEvent);
            }
        }
    }

    public void dismissActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        hr.prism.board.domain.Activity activity = activityRepository.findOne(activityId);
        if (activity != null) {
            ActivityEvent activityEvent = activityEventRepository.findByActivityAndUserAndEvent(activity, user, hr.prism.board.enums.ActivityEvent.DISMISSAL);
            if (activityEvent == null) {
                activityEventRepository.save(new ActivityEvent().setActivity(activity).setUser(user).setEvent(hr.prism.board.enums.ActivityEvent.DISMISSAL).setEventCount(1L));
                Long userId = user.getId();
                userActivityService.processRequests(userId, getActivities(userId));
            }
        }
    }

    public void deleteActivities(Resource resource) {
        activityEventRepository.deleteByResource(resource);
        activityRoleRepository.deleteByResource(resource);

        List<Long> ignores = activityRepository.findByResourceWithActivityUsers(resource);
        if (ignores.isEmpty()) {
            activityRepository.deleteByResource(resource);
        } else {
            activityRepository.deleteByResourceWithIgnores(resource, ignores);
        }
    }

    public void deleteActivities(UserRole userRole) {
        activityEventRepository.deleteByUserRole(userRole);
        activityRoleRepository.deleteByUserRole(userRole);
        activityRepository.deleteByUserRole(userRole);
    }

    public void deleteActivities(Resource resource, User user) {
        activityEventRepository.deleteByResourceAndUser(resource, user);
        activityRoleRepository.deleteByResourceAndUser(resource, user);
        activityRepository.deleteByResourceAndUser(resource, user);
    }

    public void deleteActivities(Resource resource, User user, Role role) {
        activityEventRepository.deleteByResourceAndUserAndRole(resource, user, role);
        activityRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
        activityRepository.deleteByResourceAndUserAndRole(resource, user, role);
    }

    public void deleteActivities(List<UserRole> userRoles) {
        activityEventRepository.deleteByUserRoles(userRoles);
        activityRoleRepository.deleteByUserRoles(userRoles);
        activityRepository.deleteByUserRoles(userRoles);
    }

    public void deleteActivityUsers(User user) {
        activityUserRepository.deleteByUser(user);
    }

    private Activity createActivity(Resource resource, UserRole userRole, ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity) {
        return activityRepository.save(new hr.prism.board.domain.Activity()
            .setResource(resource).setUserRole(userRole).setResourceEvent(resourceEvent).setActivity(activity).setFilterByCategory(activity.isFilterByCategory()));
    }

}
