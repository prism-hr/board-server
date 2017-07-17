package hr.prism.board.repository;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;

public interface ActivityRepository extends MyRepository<Activity, Long> {

    Activity findByResourceAndScopeAndRole(Resource resource, Scope scope, Role role);

    Activity findByResourceAndUserRoleAndScopeAndRole(Resource resource, UserRole userRole, Scope scope, Role role);

}
