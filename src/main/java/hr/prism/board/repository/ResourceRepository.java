package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("JpaQlInspection")
public interface ResourceRepository extends MyRepository<Resource, Long> {

    Resource findByHandle(String handle);

    @Query(value =
        "select resourceRelation.resource1 " +
            "from ResourceRelation resourceRelation " +
            "inner join resourceRelation.resource1 resource1 " +
            "where resourceRelation.resource2 = :resource " +
            "and resource1.scope = :scope")
    Resource findByResourceAndEnclosingScope(@Param("resource") Resource resource, @Param("scope") Scope scope);

    @Modifying
    @Query(value =
        "update resource " +
            "set handle = concat(:newHandle, substring(handle, length(:handle) + 1)) " +
            "where handle like concat(:handle, '/%')",
        nativeQuery = true)
    void updateHandle(@Param("handle") String handle, @Param("newHandle") String newHandle);

}
