package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends MyRepository<Resource, Long> {
    
    String RESOURCE_ACTIONS =
        "select resource.id, permission.action, " +
            "permission.resource3_scope, permission.resource3_state " +
            "from resource " +
            "inner join permission " +
            "on resource.scope = permission.resource2_scope " +
            "and resource.state = permission.resource2_state " +
            "inner join resource_relation " +
            "on resource.id = resource_relation.resource2_id " +
            "inner join resource as parent " +
            "on resource_relation.resource1_id = parent.id " +
            "and permission.resource1_scope = parent.scope " +
            "left join user_role " +
            "on parent.id = user_role.resource_id " +
            "and permission.role = user_role.role ";
    
    Resource findByHandle(String handle);
    
    @Modifying
    @Query(value =
        "update resource " +
            "set handle = concat(:newHandle, substring(handle, length(:handle) + 1)) " +
            "where handle like concat(:handle, '/%')",
        nativeQuery = true)
    void updateHandle(@Param("handle") String handle, @Param("newHandle") String newHandle);
    
    @Query(value =
        RESOURCE_ACTIONS +
            "where resource.id = :id " +
            "  and user_role.user_id = :userId " +
            "  and (permission.role = 'PUBLIC' " +
            "    or user_role.id is not null)", nativeQuery = true)
    List<Object[]> findResourceActionsByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
    
    @Query(value =
        RESOURCE_ACTIONS +
            "where resource.scope = :scope " +
            "  and user_role.user_id = :userId " +
            "  and (permission.role = 'PUBLIC' " +
            "    or user_role.id is not null)", nativeQuery = true)
    List<Object[]> findResourceActionsByScopeAndUser(@Param("scope") Scope scope, @Param("userId") Long userId);
    
}
