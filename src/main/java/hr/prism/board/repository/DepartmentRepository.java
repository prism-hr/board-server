package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRepository extends MyRepository<Department, Long> {
    
    Department findByName(String name);
    
    Department findByHandle(String handle);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "where department.id in (" +
            ResourceRepository.RESOURCE_IDS_BY_USER + " = '" + Scope.Value.DEPARTMENT + "') " +
            "order by department.name")
    List<Department> findAllByUserByOrderByName(@Param("user") User user);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "where department.id = :id " +
            "or department.name = :name")
    List<Department> findByIdOrName(@Param("id") Long id, @Param("name") String name);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "inner join department.children child " +
            "where child.resource2 = :board")
    Department findByBoard(@Param("board") Board board);
    
}
