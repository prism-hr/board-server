package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.enums.Action;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Transactional
public interface ResourceOperationRepository extends BoardEntityRepository<ResourceOperation, Long> {

    ResourceOperation findFirstByResourceAndActionOrderByIdDesc(Resource resource, Action action);

    @Modifying
    @Query(value =
        "INSERT INTO resource_operation (resource_id, action, creator_id, created_timestamp, updated_timestamp) " +
            "SELECT resource.id, :action, resource.creator_id, :baseline, :baseline " +
            "FROM resource " +
            "WHERE resource.id IN (:resourceIds) " +
            "ORDER BY resource.id",
        nativeQuery = true)
    void insertByResourceIdsActionAndCreatedTimestamp(@Param("resourceIds") Collection<Long> resourceIds,
                                                      @Param("action") String action,
                                                      @Param("baseline") LocalDateTime baseline);

}
