package hr.prism.board.repository;

import hr.prism.board.domain.ResourceSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ResourceSearchRepository extends SearchRepository<ResourceSearch> {

    @Modifying
    @Query(value =
        "INSERT INTO resource_search (resource_id, search, creator_id, created_timestamp) " +
            "SELECT resource_search_result.resource_id, resource_search_result.search, :creatorId, :baseline " +
            "FROM (" +
            "SELECT resource.id as resource_id, :search as search, MATCH(resource.index_data) AGAINST(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM resource " +
            "WHERE resource.id IN (:resourceIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY similarity DESC, resource.id DESC) AS resource_search_result",
        nativeQuery = true)
    void insertBySearch(@Param("search") String search, @Param("creatorId") Long creatorId, @Param("baseline") LocalDateTime localDateTime,
                        @Param("searchTerm") String searchTerm, @Param("resourceIds") Collection<Long> resourceIds);

}
