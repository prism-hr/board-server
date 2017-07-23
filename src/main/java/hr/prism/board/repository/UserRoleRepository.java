package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserRoleRepository extends MyRepository<UserRole, Long> {

    List<UserRole> findByResourceAndUser(Resource resource, User user);

    List<UserRole> findByResourceAndRole(Resource resource, Role role);

    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "where resource = :resource " +
            "and user.id = :userId " +
            "and role = :role")
    UserRole findByResourceAndUserIdAndRole(@Param("resource") Resource resource, @Param("userId") Long userId, @Param("role") Role role);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role <> :role")
    List<UserRole> findByResourceAndUserAndNotRole(@Param("resource") Resource resource, @Param("user") User user, @Param("role") Role role);

    Long deleteByResourceAndUser(Resource resource, User user);

}
