package hr.prism.board.repository;

import hr.prism.board.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends MyRepository<UserRole, Long> {

    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "inner join userRole.resource resource " +
            "inner join resource.children child " +
            "inner join child.resource2 childResource " +
            "where userRole.user = :user " +
            "and childResource.scope = :scope")
    List<UserRole> findByScopeAndUser(@Param("scope") Scope scope, @Param("user") User user);

    @Query(value =
        "select userRole.role " +
            "from UserRole userRole " +
            "where userRole.user = :user " +
            "and userRole.resource = :resource " +
            "group by userRole.role")
    List<Role> findByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "inner join userRole.resource resource " +
            "inner join resource.children child " +
            "where userRole.user = :user " +
            "and userRole.role in (:roles) " +
            "and child.resource2 = :resource")
    List<UserRole> findByResourceAndUserAndRoles(@Param("resource") Resource resource, @Param("user") User user, @Param("roles") Role... roles);

}
