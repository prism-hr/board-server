package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends MyRepository<Resource, Long> {
    
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
