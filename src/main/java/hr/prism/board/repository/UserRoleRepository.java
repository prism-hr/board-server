package hr.prism.board.repository;

import java.util.List;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;

@SuppressWarnings("JpaQlInspection")
public interface UserRoleRepository extends MyRepository<UserRole, Long> {

    List<UserRole> findByResource(Resource resource);

    List<UserRole> findByResourceAndUser(Resource resource, User user);

    List<UserRole> findByResourceAndRole(Resource resource, Role role);

    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);

    Long deleteByResourceAndUser(Resource resource, User user);

    Long deleteByResourceAndUserAndRole(Resource resource, User user, Role role);

}
