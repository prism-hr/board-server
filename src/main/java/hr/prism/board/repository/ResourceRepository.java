package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends MyRepository<Resource, Long> {
    
    Resource findByHandle(String handle);
    
    @Modifying
    @Query(value =
        "update resource " +
            "set handle = concat(:newHandle, substring(handle, length(:handle) + 1)) " +
            "where handle like concat(:handle, '/%')",
        nativeQuery = true)
    void updateHandle(@Param("handle") String handle, @Param("newHandle") String newHandle);
    
}
