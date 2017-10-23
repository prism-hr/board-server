package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings({"JpaQlInspection", "SameParameterValue"})
public interface ResourceTaskRepository extends MyRepository<ResourceTask, Long> {

    @Query(value =
        "select resourceTask.id " +
            "from ResourceTask resourceTask " +
            "where resourceTask.notifiedCount is null and resourceTask.createdTimestamp < :baseline1 " +
            "or (resourceTask.notifiedCount = :notifiedCount2 and resourceTask.createdTimestamp < :baseline2) " +
            "or (resourceTask.notifiedCount = :notifiedCount3 and resourceTask.createdTimestamp < :baseline3)")
    List<Long> findAllIds(@Param("baseline1") LocalDateTime baseline1, @Param("notifiedCount2") Integer notifiedCount2, @Param("baseline2") LocalDateTime baseline2,
                          @Param("notifiedCount3") Integer notifiedCount3, @Param("baseline3") LocalDateTime baseline3);

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
            "where reosurceTask.resource = :resource " +
            "and suppression.user = :user")
    List<ResourceTask> findByResourceAndSuppressions(@Param("resource") Resource resource, @Param("user") User user);

    @Modifying
    @Query(value =
        "delete from ResourceTask resourceTask " +
            "where resourceTask.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);

    @Modifying
    @Query(value =
        "delete from ResourceTask resourceTask " +
            "where resourceTask.resource = :resource " +
            "and resourceTask.task in (:tasks)")
    void deleteByResourceAndTasks(@Param("resource") Resource resource, @Param("tasks") List<hr.prism.board.enums.ResourceTask> tasks);

}
