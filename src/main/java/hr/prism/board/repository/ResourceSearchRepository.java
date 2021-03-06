package hr.prism.board.repository;

import hr.prism.board.domain.ResourceSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Transactional
public interface ResourceSearchRepository extends SearchRepository<ResourceSearch> {

    @Modifying
    @Query(value =
        "INSERT INTO resource_search (resource_id, search, creator_id, created_timestamp, updated_timestamp) " +
            "SELECT resource_search_result.resource_id, resource_search_result.search, " +
            "resource_search_result.creator_id, :baseline, :baseline " +
            "FROM (" +
            "SELECT resource.id as resource_id, :search as search, resource.creator_id as creator_id, " +
            "MATCH(resource.index_data) AGAINST(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM resource " +
            "WHERE resource.id IN (:resourceIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY similarity DESC, resource.id) AS resource_search_result",
        nativeQuery = true)
    void insertBySearch(@Param("search") String search, @Param("baseline") LocalDateTime localDateTime,
                        @Param("searchTerm") String searchTerm, @Param("resourceIds") Collection<Long> resourceIds);

}
