package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.repository.ActivityDismissalRepository;
import hr.prism.board.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
public class ActivityService {

    @Inject
    private ActivityRepository activityRepository;

    @Inject
    private ActivityDismissalRepository activityDismissalRepository;

    @Inject
    private UserService userService;

    public List<Activity> getActivities() {
        User user = userService.getCurrentUserSecured();
        return activityRepository.findByUser(user);
    }

    // TODO: update views for logged in users (long polling)
    public Activity getOrCreateActivity(Resource resource, Scope scope, Role role) {
        return getOrCreateActivity(resource, null, scope, role);
    }

    // TODO: update views for logged in users (long polling)
    public Activity getOrCreateActivity(UserRole userRole, Scope scope, Role role) {
        return getOrCreateActivity(userRole.getResource(), userRole, scope, role);
    }

    public void dismissActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        Activity activity = activityRepository.findOne(activityId);
        ActivityDismissal activityDismissal = activityDismissalRepository.findByActivityAndUser(activity, user);
        if (activityDismissal == null) {
            activityDismissalRepository.save(new ActivityDismissal().setActivity(activity).setUser(user));
        }
    }

    // TODO: update views for logged in users (long polling)
    public void deleteActivities(Resource resource) {
        activityDismissalRepository.deleteByResource(resource);
        activityRepository.deleteByResource(resource);
    }

    // TODO: update views for logged in users (long polling)
    public void deleteActivities(UserRole userRole) {
        activityDismissalRepository.deleteByUserRole(userRole);
        activityRepository.deleteByUserRole(userRole);
    }

    private Activity getOrCreateActivity(Resource resource, UserRole userRole, Scope scope, Role role) {
        Activity activity;
        if (userRole == null) {
            activity = activityRepository.findByResourceAndScopeAndRole(resource, scope, role);
        } else {
            activity = activityRepository.findByResourceAndUserRoleAndScopeAndRole(resource, userRole, scope, role);
        }

        if (activity == null) {
            activity = activityRepository.save(new Activity().setResource(resource).setUserRole(userRole).setScope(scope).setRole(role));
        }

        return activity;
    }

}
