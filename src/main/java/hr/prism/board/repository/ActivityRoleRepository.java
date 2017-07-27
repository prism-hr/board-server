package hr.prism.board.repository;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.ActivityRole;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("JpaQlInspection")
public interface ActivityRoleRepository extends MyRepository<ActivityRole, Long> {

    ActivityRole findByActivityAndScopeAndRole(Activity activity, Scope scope, Role role);

    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.userRole is null)")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "delete from ActivityRole activityRole " +
            "where activityRole.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.userRole = :userRole)")
    void deleteByUserRole(@Param("userRole") UserRole userRole);

}
