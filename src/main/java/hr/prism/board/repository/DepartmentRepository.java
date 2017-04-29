package hr.prism.board.repository;

import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface DepartmentRepository extends MyRepository<Department, Long> {
    
    Department findByName(String name);
    
    @Query(value =
        "select department.handle " +
            "from Department department " +
            "where department.handle like concat('%', :suggestedHandle) " +
            "order by department.handle desc")
    List<String> findHandleByLikeSuggestedHandle(@Param("suggestedHandle") String suggestedHandle);
    
}
