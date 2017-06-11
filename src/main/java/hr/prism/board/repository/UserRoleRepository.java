package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserRoleRepository extends MyRepository<UserRole, Long> {

    List<UserRole> findByResource(Resource resource);

    List<UserRole> findByResourceAndRole(Resource resource, Role role);

    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);

    Long deleteByResourceAndUser(Resource resource, User user);

}
