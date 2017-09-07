package hr.prism.board.repository;

import hr.prism.board.domain.ResourceSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ResourceSearchRepository extends MyRepository<ResourceSearch, Long> {

    @Modifying
    @Query(value =
        "INSERT INTO resource_search(resource_id, search) " +
            "SELECT resource_search_result.resource_id, resource_search_result.search " +
            "FROM (" +
            "SELECT resource.id as resource_id, :search as search, MATCH resource.index_data against(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM resource " +
            "WHERE resource.id IN (:resourceIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY similarity DESC, resource.id DESC) AS resource_search_result",
        nativeQuery = true)
    void insertBySearch(@Param("search") String search, @Param("searchTerm") String searchTerm,
                        @Param("resourceIds") Collection<Long> resourceIds);

    void deleteBySearch(@Param("search") String search);

}
