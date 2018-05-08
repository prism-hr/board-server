package hr.prism.board.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Collection;

@Repository
@Transactional
public class ResourceSearchDAO {

    private static final String INSERT_STATEMENT =
        "INSERT INTO resource_search (resource_id, search, creator_id, created_timestamp, updated_timestamp) " +
            "SELECT resource_search_result.resource_id, resource_search_result.search, " +
            "resource_search_result.creator_id, :baseline, :baseline " +
            "FROM (" +
            "SELECT resource.id as resource_id, :search as search, resource.creator_id as creator_id, " +
            "MATCH(resource.index_data) AGAINST(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM resource " +
            "WHERE resource.id IN (:resourceIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY ";

    private final EntityManager entityManager;

    @Inject
    public ResourceSearchDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insertBySearch(String search, LocalDateTime baseline, String searchTerm, Collection<Long> resourceIds,
                               String orderStatement) {
        entityManager.createNativeQuery(
            INSERT_STATEMENT + orderStatement + ") AS resource_search_result")
            .setParameter("search", search)
            .setParameter("baseline", baseline)
            .setParameter("searchTerm", searchTerm)
            .setParameter("resourceIds", resourceIds)
            .executeUpdate();

    }

}
