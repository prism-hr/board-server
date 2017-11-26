package hr.prism.board.repository;

import hr.prism.board.domain.ResourceEventSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ResourceEventSearchRepository extends SearchRepository<ResourceEventSearch> {

    @Modifying
    @Query(value =
        "INSERT INTO resource_event_search (resource_event_id, search, creator_id, created_timestamp) " +
            "SELECT resource_event_search_result.resource_event_id, resource_event_search_result.search, resource_event_search_result.creator_id, :baseline " +
            "FROM (" +
            "SELECT resource_event.id as resource_event_id, :search as search, resource_event.creator_id AS creator_id, " +
            "MATCH(resource_event.index_data) AGAINST(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM resource_event " +
            "WHERE resource_event.user_id IN (:userIds) " +
            "HAVING similarity > 0 " +
            "ORDER BY similarity DESC, resource_event.id DESC) AS resource_event_search_result",
        nativeQuery = true)
    void insertBySearch(@Param("search") String search, @Param("baseline") LocalDateTime localDateTime, @Param("searchTerm") String searchTerm,
                        @Param("userIds") Collection<Long> userIds);

}
