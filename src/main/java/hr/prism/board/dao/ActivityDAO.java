package hr.prism.board.dao;

import hr.prism.board.domain.Activity;
import hr.prism.board.repository.ActivityRepository;
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

    private static final String USER_ACTIVITY =
        "select distinct activity " +
            "from Activity activity " +
            "left join activity.activityRoles activityRole " +
            "left join activity.resource resource " +
            "left join resource.parents parentRelation " +
            "left join parentRelation.resource1 parent " +
            "left join parent.userRoles userRole " +
            "left join resource.categories resourceCategory " +
            "left join activity.activityUsers activityUser " +
            "where (activityUser.id is null " +
            "and activityRole.scope = parent.scope " +
            "and activityRole.role = userRole.role " +
            "and userRole.user.id = :userId " +
            "and userRole.state in (:userRoleStates) " +
            "and (activity.filterByCategory = false " +
            "or resourceCategory.id is null " +
            "or resourceCategory.type = :categoryType " +
            "and resourceCategory.name = userRole.memberCategory) " +
            "or activityUser.user.id = :userId) " +
            "and activity.id not in (" +
            "select activityEvent.activity.id " +
            "from ActivityEvent activityEvent " +
            "where activityEvent.user.id = :userId " +
            "and activityEvent.event = :activityEvent) " +
            "order by activity.updatedTimestamp desc, activity.id desc";

    private final ActivityRepository activityRepository;

    private final EntityManager entityManager;

    @Inject
    public ActivityDAO(ActivityRepository activityRepository, EntityManager entityManager) {
        this.activityRepository = activityRepository;
        this.entityManager = entityManager;
    }

    public List<Activity> getActivities(Long userId) {
        List<Long> ids = entityManager.createNamedQuery("userActivities", Long.class)
            .setParameter("userId", userId)
            .setParameter("userRoleStates", ACTIVE_USER_ROLE_STATE_STRINGS)
            .setParameter("categoryType", MEMBER.name())
            .setParameter("activityEvent", DISMISSAL.name())
            .getResultList();

        return ids.isEmpty() ? emptyList() : activityRepository.findByIds(ids);
    }

}
