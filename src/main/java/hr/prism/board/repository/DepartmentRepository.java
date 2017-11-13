package hr.prism.board.repository;

import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface DepartmentRepository extends MyRepository<Department, Long> {

    @Query(value =
        "select department.id " +
            "from Department department " +
            "where department.createdTimestamp < :baseline1 " +
            "and (department.lastTaskCreationTimestamp is null or department.lastTaskCreationTimestamp < :baseline2) " +
            "and (department.lastMemberTimestamp < :baseline1 or department.lastInternalPostTimestamp < :baseline1) " +
            "order by department.id")
    List<Long> findAllIds(@Param("baseline1") LocalDateTime baseline1, @Param("baseline2") LocalDateTime baseline2);

    @Query(value =
        "select department.handle " +
            "from Department department " +
            "where department.handle like concat('%', :suggestedHandle) " +
            "or department.handle like concat('%', :suggestedHandle, '-%') " +
            "order by department.handle desc")
    List<String> findHandleByLikeSuggestedHandle(@Param("suggestedHandle") String suggestedHandle);

}
