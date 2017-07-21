package hr.prism.board.service;

import hr.prism.board.domain.ActivityDismissal;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Activity;
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

    public List<hr.prism.board.domain.Activity> getActivities() {
        User user = userService.getCurrentUserSecured();
        return activityRepository.findByUser(user);
    }

    // TODO: update views for logged in users (long polling)
    public hr.prism.board.domain.Activity getOrCreateActivity(Resource resource, Scope scope, Role role, Activity category) {
        return getOrCreateActivity(resource, null, scope, role, category);
    }

    // TODO: update views for logged in users (long polling)
    public hr.prism.board.domain.Activity getOrCreateActivity(UserRole userRole, Scope scope, Role role) {
        return getOrCreateActivity(userRole.getResource(), userRole, scope, role, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY);
    }

    public void dismissActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        hr.prism.board.domain.Activity activity = activityRepository.findOne(activityId);
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

    private hr.prism.board.domain.Activity getOrCreateActivity(Resource resource, UserRole userRole, Scope scope, Role role, Activity category) {
        hr.prism.board.domain.Activity activity;
        if (userRole == null) {
            activity = activityRepository.findByResourceAndScopeAndRole(resource, scope, role);
        } else {
            activity = activityRepository.findByResourceAndUserRoleAndScopeAndRole(resource, userRole, scope, role);
        }

        if (activity == null) {
            activity = activityRepository.save(new hr.prism.board.domain.Activity().setResource(resource).setUserRole(userRole).setScope(scope).setRole(role).setActivity(category));
        }

        return activity;
    }

}
