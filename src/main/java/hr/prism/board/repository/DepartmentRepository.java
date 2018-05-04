package hr.prism.board.repository;

import hr.prism.board.domain.Department;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Override
    @EntityGraph("department.extended")
    List<Department> findAll();

    @Query(value =
        "select department.id " +
            "from Department department " +
            "where department.createdTimestamp < :baseline1 " +
            "and (department.lastMemberTimestamp is null or department.lastMemberTimestamp < :baseline1) " +
            "and (department.lastTaskCreationTimestamp is null or department.lastTaskCreationTimestamp < :baseline2) " +
            "order by department.id")
    List<Long> findAllIdsForTaskUpdates(@Param("baseline1") LocalDateTime baseline1,
                                        @Param("baseline2") LocalDateTime baseline2);

    @Query(value =
        "select department.id " +
            "from Department department " +
            "where department.state = :pendingState " +
            "and (department.notifiedCount is null " +
            "or (department.notifiedCount = :notifiedCount1 and department.stateChangeTimestamp < :baseline1) " +
            "or (department.notifiedCount = :notifiedCount2 and department.stateChangeTimestamp < :baseline2)) " +
            "order by department.id")
    List<Long> findAllIdsForSubscribeNotification(@Param("pendingState") State pendingState,
                                                  @Param("notifiedCount1") Integer notifiedCount1,
                                                  @Param("notifiedCount2") Integer notifiedCount2,
                                                  @Param("baseline1") LocalDateTime baseline1,
                                                  @Param("baseline2") LocalDateTime baseline2);

    @Query(value =
        "select department.id " +
            "from Department department " +
            "where department.state = :state " +
            "and department.stateChangeTimestamp < :expiryTimestamp")
    List<Long> findByStateAndStateChangeTimestampLessThan(@Param("state") State state,
                                                          @Param("expiryTimestamp") LocalDateTime expiryTimestamp);

    Department findByCustomerId(String customerId);

}
