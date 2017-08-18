package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.ActivityMapper;
import hr.prism.board.repository.ActivityDismissalRepository;
import hr.prism.board.repository.ActivityRepository;
import hr.prism.board.repository.ActivityRoleRepository;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
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
    private ActivityDismissalRepository activityDismissalRepository;

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
        return activityRepository.findByUserId(userId, State.ACTIVE_USER_ROLE_STATES, CategoryType.MEMBER).stream().map(activityMapper).collect(Collectors.toList());
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

    public void dismissActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        hr.prism.board.domain.Activity activity = activityRepository.findOne(activityId);
        ActivityDismissal activityDismissal = activityDismissalRepository.findByActivityAndUser(activity, user);
        if (activityDismissal == null) {
            activityDismissalRepository.save(new ActivityDismissal().setActivity(activity).setUser(user));
            Long userId = user.getId();
            userActivityService.processRequests(userId, getActivities(userId));
        }
    }

    public void deleteActivities(Resource resource) {
        activityDismissalRepository.deleteByResource(resource);
        activityRoleRepository.deleteByResource(resource);
        activityRepository.deleteByResource(resource);
    }

    public void deleteActivities(UserRole userRole) {
        activityDismissalRepository.deleteByUserRole(userRole);
        activityRoleRepository.deleteByUserRole(userRole);
        activityRepository.deleteByUserRole(userRole);
    }

    private Activity createActivity(Resource resource, UserRole userRole, ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity) {
        return activityRepository.save(new hr.prism.board.domain.Activity()
            .setResource(resource).setUserRole(userRole).setResourceEvent(resourceEvent).setActivity(activity).setFilterByCategory(activity.isFilterByCategory()));
    }

}
