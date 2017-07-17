package hr.prism.board.service;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class ActivityService {

    @Inject
    private ActivityRepository activityRepository;

    public Activity getOrCreateActivity(UserRole userRole, Scope scope, Role role) {
        return getOrCreateActivity(userRole.getResource(), userRole, scope, role);
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
