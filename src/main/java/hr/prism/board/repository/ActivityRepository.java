package hr.prism.board.repository;

import hr.prism.board.domain.*;
import hr.prism.board.enums.ActivityEvent;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ActivityRepository extends MyRepository<Activity, Long> {

    @Query(value =
        "select distinct activity " +
            "from Activity activity " +
            "left join activity.activityRoles activityRole " +
            "left join activity.resource resource " +
            "left join resource.parents parentRelation " +
            "left join parentRelation.resource1 parent " +
            "left join parent.userRoles userRole " +
            "left join resource.categories resourceCategory " +
            "left join userRole.categories userRoleCategory " +
            "left join activity.activityUsers activityUser " +
            "where activityUser.id is null " +
            "and activityRole.scope = parent.scope " +
            "and activityRole.role = userRole.role " +
            "and userRole.user.id = :userId " +
            "and userRole.state in (:userRoleStates) " +
            "and (activity.filterByCategory = false or resourceCategory.type = :categoryType and resourceCategory.name = userRoleCategory.name) " +
            "or activityUser.user.id = :userId " +
            "and activity.id not in (" +
            "select activityEvent.activity.id " +
            "from ActivityEvent activityEvent " +
            "where activityEvent.user.id = :userId " +
            "and activityEvent.event = :activityEvent) " +
            "order by activity.id desc")
    List<Activity> findByUserId(@Param("userId") Long userId, @Param("userRoleStates") List<State> state, @Param("categoryType") CategoryType categoryType,
                                @Param("activityEvent") ActivityEvent activityEvent);

    @Query(value =
        "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.activity = :activity " +
            "and activity.userRole is null")
    Activity findByResourceAndActivity(@Param("resource") Resource resource, @Param("activity") hr.prism.board.enums.Activity activity);

    Activity findByUserRoleAndActivity(UserRole userRole, hr.prism.board.enums.Activity activity);

    Activity findByResourceEventAndActivity(ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.id in (:ignores) " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null")
    void deleteByResourceWithIgnores(@Param("resource") Resource resource, @Param("ignores") List<Long> ignores);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole = :userRole")
    void deleteByUserRole(@Param("userRole") UserRole userRole);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole in (:userRoles)")
    void deleteByUserRoles(@Param("userRoles") List<UserRole> userRoles);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole in (" +
            "select userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole in (" +
            "select userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role = :role)")
    void deleteByResourceAndUserAndRole(@Param("resource") Resource resource, @Param("user") User user, @Param("role") Role role);

    @Query(value =
        "select activity.id " +
            "from Activity activity " +
            "inner join activity.activityUsers activityUser " +
            "where activity.resource = :resource " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null")
    List<Long> findByResourceWithActivityUsers(@Param("resource") Resource resource);

}
