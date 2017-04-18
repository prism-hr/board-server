package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserRoleRepository extends MyRepository<UserRole, Long> {
    
    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);
    
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
