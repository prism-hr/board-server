package hr.prism.board.dao;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.repository.ActivityEventRepository;
import hr.prism.board.repository.ActivityRepository;
import hr.prism.board.repository.ActivityRoleRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static hr.prism.board.enums.ActivityEvent.DISMISSAL;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.State.ACTIVE_USER_ROLE_STATE_STRINGS;
import static java.util.Collections.emptyList;

@Repository
@Transactional
public class ActivityDAO {

    private final ActivityRepository activityRepository;

    private final ActivityEventRepository activityEventRepository;

    private final ActivityRoleRepository activityRoleRepository;

    private final EntityManager entityManager;

    @Inject
    public ActivityDAO(ActivityRepository activityRepository, ActivityEventRepository activityEventRepository,
                       ActivityRoleRepository activityRoleRepository, EntityManager entityManager) {
        this.activityRepository = activityRepository;
        this.activityEventRepository = activityEventRepository;
        this.activityRoleRepository = activityRoleRepository;
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public List<Activity> getActivities(Long userId) {
        List<Long> ids = entityManager.createNamedQuery("userActivities", Long.class)
            .setParameter("userId", userId)
            .setParameter("userRoleStates", ACTIVE_USER_ROLE_STATE_STRINGS)
            .setParameter("categoryType", MEMBER.name())
            .setParameter("activityEvent", DISMISSAL.name())
            .getResultList();

        return ids.isEmpty() ? emptyList() : activityRepository.findByIds(ids);
    }

    public void deleteActivities(Resource resource) {
        activityEventRepository.deleteByResource(resource);
        activityRoleRepository.deleteByResource(resource);

        List<Long> exclusions = activityRepository.findByResourceWithActivityUsers(resource);
        if (exclusions.isEmpty()) {
            activityRepository.deleteByResource(resource);
        } else {
            activityRepository.deleteByResource(resource, exclusions);
        }
    }

    public void deleteActivities(UserRole userRole) {
        activityEventRepository.deleteByUserRole(userRole);
        activityRoleRepository.deleteByUserRole(userRole);
        activityRepository.deleteByUserRole(userRole);
    }

    public void deleteActivities(List<UserRole> userRoles) {
        activityEventRepository.deleteByUserRoles(userRoles);
        activityRoleRepository.deleteByUserRoles(userRoles);
        activityRepository.deleteByUserRoles(userRoles);
    }

    public void deleteActivities(Resource resource, User user, Role role) {
        activityEventRepository.deleteByResourceAndUserAndRole(resource, user, role);
        activityRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
        activityRepository.deleteByResourceAndUserAndRole(resource, user, role);
    }

    public void deleteActivities(Resource resource, List<hr.prism.board.enums.Activity> activities) {
        activityEventRepository.deleteByResourceAndActivities(resource, activities);
        activityRoleRepository.deleteByResourceAndActivities(resource, activities);
        activityRepository.deleteByResourceAndActivities(resource, activities);
    }

}
