package hr.prism.board.service;

import hr.prism.board.domain.ActivityDismissal;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.mapper.ActivityMapper;
import hr.prism.board.repository.ActivityDismissalRepository;
import hr.prism.board.repository.ActivityRepository;
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
    private ActivityDismissalRepository activityDismissalRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserActivityService userActivityService;

    @Inject
    private ActivityMapper activityMapper;

    public List<ActivityRepresentation> getActivities() {
        User user = userService.getCurrentUserSecured();
        return getActivities(user.getId());
    }

    public List<ActivityRepresentation> getActivities(Long userId) {
        return activityRepository.findByUserId(userId).stream().map(activityMapper).collect(Collectors.toList());
    }

    public hr.prism.board.domain.Activity getOrCreateActivity(Resource resource, Scope scope, Role role, Activity activity) {
        return getOrCreateActivity(resource, null, scope, role, activity);
    }

    public hr.prism.board.domain.Activity getOrCreateActivity(UserRole userRole, Scope scope, Role role, Activity activity) {
        return getOrCreateActivity(userRole.getResource(), userRole, scope, role, activity);
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
        activityRepository.deleteByResource(resource);
    }

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
