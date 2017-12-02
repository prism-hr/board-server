package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings({"JpaQlInspection", "SameParameterValue", "SpringDataRepositoryMethodReturnTypeInspection", "SqlResolve"})
public interface ResourceTaskRepository extends MyRepository<ResourceTask, Long> {
    
    @Query(value =
        "select resourceTask " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource.id = :resourceId")
    List<ResourceTask> findByResourceId(@Param("resourceId") Long resourceId);
    
    @Query(value =
        "select resourceTask.task " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource = :resource " +
            "and resourceTask.id not in (" +
            "select resourceTask.id " +
            "from ResourceTask resourceTask " +
            "inner join resourceTask.suppressions suppression " +
            "where resourceTask.resource = :resource " +
            "and suppression.user = :user)")
    List<hr.prism.board.enums.ResourceTask> findByResource(@Param("resource") Resource resource, @Param("user") User user);
    
    @Query(value =
        "select resourceTask " +
            "from ResourceTask resourceTask " +
            "where resourceTask.notifiedCount is null and resourceTask.createdTimestamp < :baseline1 " +
            "or (resourceTask.notifiedCount = :notifiedCount1 and resourceTask.createdTimestamp < :baseline2) " +
            "or (resourceTask.notifiedCount = :notifiedCount2 and resourceTask.createdTimestamp < :baseline3) " +
            "order by resourceTask.resource, resourceTask.task")
    List<ResourceTask> findByNotificationHistory(@Param("notifiedCount1") Integer notifiedCount1, @Param("notifiedCount2") Integer notifiedCount2,
        @Param("baseline1") LocalDateTime baseline1, @Param("baseline2") LocalDateTime baseline2,
        @Param("baseline3") LocalDateTime baseline3);
    
    @Query(value =
        "select resourceTask " +
            "from ResourceTask resourceTask " +
            "inner join resourceTask.suppressions suppression " +
            "where suppression.user = :user")
    List<ResourceTask> findBySuppressions(@Param("user") User user);
    
    @Query(value =
        "select resourceTask " +
            "from ResourceTask resourceTask " +
            "inner join resourceTask.suppressions suppression " +
            "where resourceTask.resource = :resource " +
            "and suppression.user = :user")
    List<ResourceTask> findByResourceAndSuppressions(@Param("resource") Resource resource, @Param("user") User user);
    
    @Modifying
    @Query(value =
        "delete from ResourceTask resourceTask " +
            "where resourceTask.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);
    
    @Modifying
    @Query(value =
        "update ResourceTask resourceTask " +
            "set resourceTask.completed = :completed " +
            "where resourceTask.resource = :resource " +
            "and resourceTask.task in (:tasks)")
    void updateByResourceAndTasks(@Param("resource") Resource resource, @Param("tasks") List<hr.prism.board.enums.ResourceTask> tasks, @Param("completed") Boolean completed);
    
    @Modifying
    @Query(value =
        "UPDATE resource_task " +
            "SET resource_task.notified_count = IF(resource_task.notified_count IS NULL, 1, resource_task.notified_count + 1) " +
            "WHERE resource_task.resource_id = :resourceId",
        nativeQuery = true)
    void updateNotifiedCountByResourceId(@Param("resourceId") Long resourceId);
    
    @Modifying
    @Query(value =
        "UPDATE resource_task " +
            "SET resource_task.created_timestamp = :createdTimestamp " +
            "WHERE resource_task.resource_id = :resourceId",
        nativeQuery = true)
    void updateCreatedTimestampByResourceId(@Param("resourceId") Long resourceId, @Param("createdTimestamp") LocalDateTime createdTimestamp);
    
    @Query(value =
        "select resourceTask.id " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource = :resource " +
            "and resourceTask.completed is null")
    List<Long> findByResourceAndNotCompleted(@Param("resource") Resource resource);
    
    @Query(value =
        "select resourceTask.id " +
            "from ResourceTask resourceTask " +
            "where resourceTask.resource = :resource " +
            "and resourceTask.id not in (" +
            "select resourceTask.id " +
            "from ResourceTask resourceTask " +
            "inner join resourceTask.suppressions suppression " +
            "where resourceTask.resource = :resource " +
            "and suppression.user = :user)")
    List<Long> findByResourceAndNotSuppressed(@Param("resource") Resource resource, @Param("user") User user);
    
}
