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
    
    List<UserRole> findByResource(Resource resource);
    
    List<UserRole> findByResourceAndUser(Resource resource, User user);
    
    List<UserRole> findByResourceAndRole(Resource resource, Role role);
    
    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);
    
    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "inner join userRole.resource resource " +
            "inner join resource.children child " +
            "where userRole.role in (:roles) " +
            "and child.resource2 = :resource " +
            "and userRole.resource <> child.resource2")
    List<UserRole> findInParentScopesByResourceAndRole(@Param("resource") Resource resource, @Param("role") Role role);
    
    Long deleteByResourceAndUser(Resource resource, User user);
    
    Long deleteByResourceAndUserAndRole(Resource resource, User user, Role role);
    
}
