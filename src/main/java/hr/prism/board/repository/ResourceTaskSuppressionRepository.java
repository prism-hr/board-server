package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.ResourceTaskSuppression;
import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ResourceTaskSuppressionRepository extends MyRepository<ResourceTaskSuppression, Long> {

    ResourceTaskSuppression findByResourceTaskAndUser(ResourceTask resourceTask, User user);

    @Modifying
    @Query(value =
        "delete from ResourceTaskSuppression resourceTaskSuppression " +
            "where resourceTaskSuppression.resourceTask in (" +
            "select resourceTask " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource.id = :resourceId)")
    void deleteByResourceId(@Param("resourceId") Long resourceId);

    @Modifying
    @Query(value =
        "delete from ResourceTaskSuppression resourceTaskSuppression " +
            "where resourceTaskSuppression.resourceTask in (" +
            "select resourceTask " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource = :resource " +
            "and resourceTask.task in (:tasks))")
    void deleteByResourceAndTasks(@Param("resource") Resource resource, @Param("tasks") List<hr.prism.board.enums.ResourceTask> tasks);

}