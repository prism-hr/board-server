package hr.prism.board.repository;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ActivityRoleRepository extends MyRepository<ActivityRole, Long> {
    
    ActivityRole findByActivityAndScopeAndRole(Activity activity, Scope scope, Role role);
    
    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in (" +
            "select activity " +
            "from Activity activity " +
            "left join activity.activityUsers activityUser " +
            "where activity.resource = :resource " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null " +
            "and activityUser.id is null)")
    void deleteByResource(@Param("resource") Resource resource);
    
    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.activity in (:activities))")
    void deleteByResourceAndActivities(@Param("resource") Resource resource, @Param("activities") List<hr.prism.board.enums.Activity> activities);
    
    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.userRole = :userRole)")
    void deleteByUserRole(@Param("userRole") UserRole userRole);
    
    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.userRole in (:userRoles))")
    void deleteByUserRoles(@Param("userRoles") List<UserRole> userRoles);
    
    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in ( " +
            "select activity " +
            "from Activity activity " +
            "inner join activity.userRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);
    
    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in ( " +
            "select activity " +
            "from Activity activity " +
            "inner join activity.userRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role = :role)")
    void deleteByResourceAndUserAndRole(@Param("resource") Resource resource, @Param("user") User user, @Param("role") Role role);
    
}
