package hr.prism.board.repository;

import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.ResourceTaskCompletion;
import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("JpaQlInspection")
public interface ResourceTaskCompletionRepository extends MyRepository<ResourceTaskCompletion, Long> {
    
    ResourceTaskCompletion findByResourceTaskAndUser(ResourceTask resourceTask, User user);
    
    @Modifying
    @Query(value =
        "delete from ResourceTaskCompletion resourceTaskSuppression " +
            "where resourceTaskSuppression.resourceTask in (" +
            "select resourceTask " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource.id = :resourceId)")
    void deleteByResourceId(@Param("resourceId") Long resourceId);
    
}
