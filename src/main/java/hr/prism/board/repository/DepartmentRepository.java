package hr.prism.board.repository;

import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface DepartmentRepository extends MyRepository<Department, Long> {

    @Query(value =
        "select department.id " +
            "from Department department " +
            "order by department.id")
    List<Long> findAllIds();

    @Query(value =
        "select department " +
            "from Department department " +
            "where department.id = :id " +
            "or department.name = :name")
    List<Department> findByIdOrName(@Param("id") Long id, @Param("name") String name);

    @Query(value =
        "select department.handle " +
            "from Department department " +
            "where department.handle like concat('%', :suggestedHandle) " +
            "order by department.handle desc")
    List<String> findHandleByLikeSuggestedHandle(@Param("suggestedHandle") String suggestedHandle);

}
