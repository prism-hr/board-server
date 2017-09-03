package hr.prism.board.repository;

import hr.prism.board.domain.ResourceSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface ResourceSearchRepository extends MyRepository<ResourceSearch, Long> {

    @Modifying
    @Query(value =
        "INSERT INTO resource_search(resource_id, search) " +
            "SELECT resource.id , :search " +
            "MATCH resource.indexData against(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM resource " +
            "WHERE resource.id IN (:resourceIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY similarity DESC, resource.id DESC",
        nativeQuery = true)
    void insertBySearch(String search, String searchTerm, Collection<Long> resourceIds);

    void deleteBySearch(String search);

}
