package hr.prism.board.repository;

import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DepartmentRepository extends MyRepository<Department, Long> {
    
    Department findByName(String name);
    
    Department findByHandle(String handle);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "where department.id in (:ids) " +
            "order by department.name")
    List<Department> findAllByUserByOrderByName(@Param("ids") Collection<Long> ids);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "where department.id = :id " +
            "or department.name = :name")
    List<Department> findByIdOrName(@Param("id") Long id, @Param("name") String name);
    
}
