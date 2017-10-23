package hr.prism.board.repository;

import hr.prism.board.domain.ActivityEvent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ResourceTaskRepository extends MyRepository<ActivityEvent, Long> {

    @Modifying
    @Query(value =
        "INSERT INTO resource_task (resource_id, task, created_timestamp) " +
            "VALUES (:resourceId, :createMember, :baseline), (:resourceId, :createInternalPost, :baseline), (:resourceId, :notifyAuthor, :baseline)",
        nativeQuery = true)
    void insertForNewResource(@Param("resourceId") Long resourceId, @Param("createMember") String createMember, @Param("createInternalPost") String createInternalPost,
                              @Param("notifyAuthor") String notifyAuthor, @Param("baseline") LocalDateTime baseline);

}
