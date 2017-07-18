package hr.prism.board.repository;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ActivityRepository extends MyRepository<Activity, Long> {

    @Query(value =
        "select distinct activity " +
            "from Activity activity " +
            "inner join activity.resource resource " +
            "inner join resource.parents parentRelation " +
            "inner join parentRelation.resource1 parent " +
            "inner join parent.userRoles userRole " +
            "where activity.scope = parent.scope " +
            "and activity.role = userRole.role " +
            "and userRole.user = :user " +
            "and activity.id not in (" +
            "select activityDismissal.activity.id " +
            "from ActivityDismissal activityDismissal " +
            "where activityDismissal.user = :user) " +
            "order by activity.id desc")
    List<Activity> findByUser(@Param("user") User user);

    Activity findByResourceAndScopeAndRole(Resource resource, Scope scope, Role role);

    Activity findByResourceAndUserRoleAndScopeAndRole(Resource resource, UserRole userRole, Scope scope, Role role);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole = :userRole")
    void deleteByUserRole(@Param("userRole") UserRole userRole);

}
