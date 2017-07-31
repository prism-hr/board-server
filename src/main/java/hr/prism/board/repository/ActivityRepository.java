package hr.prism.board.repository;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.CategoryType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ActivityRepository extends MyRepository<Activity, Long> {

    String ACTIVITY_STATEMENT =
        "select distinct activity " +
            "from Activity activity " +
            "inner join activity.activityRoles activityRole " +
            "inner join activity.resource resource " +
            "inner join resource.parents parentRelation " +
            "inner join parentRelation.resource1 parent " +
            "inner join parent.userRoles userRole " +
            "left join resource.categories resourceCategory " +
            "left join userRole.categories userRoleCategory " +
            "where activityRole.scope = parent.scope " +
            "and activityRole.role = userRole.role " +
            "and userRole.user.id = :userId " +
            "and (activity.filterByCategory = false or resourceCategory.type = :categoryType and resourceCategory.name = userRoleCategory.name)" +
            "and activity.id not in (" +
            "select activityDismissal.activity.id " +
            "from ActivityDismissal activityDismissal " +
            "where activityDismissal.user.id = :userId)";

    @Query(value =
        ACTIVITY_STATEMENT + " " +
            "order by activity.id desc")
    List<Activity> findByUserId(@Param("userId") Long userId, @Param("categoryType") CategoryType categoryType);

    @Query(value =
        ACTIVITY_STATEMENT + " " +
            "and resource.id = :resourceId " +
            "and activity.userRole is not null " +
            "order by activity.id desc")
    List<Activity> findByUserIdAndResourceIdAndUserRoleNotNull(@Param("userId") Long userId, @Param("resourceId") Long resourceId, @Param("categoryType") CategoryType categoryType);

    @Query(value =
        "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.activity = :activity " +
            "and activity.userRole is null")
    Activity findByResourceAndActivity(@Param("resource") Resource resource, @Param("activity") hr.prism.board.enums.Activity activity);

    Activity findByUserRoleAndActivity(UserRole userRole, hr.prism.board.enums.Activity activity);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.userRole is null")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole = :userRole")
    void deleteByUserRole(@Param("userRole") UserRole userRole);

}
