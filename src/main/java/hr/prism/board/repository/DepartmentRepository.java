package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DepartmentRepository extends MyRepository<Department, Long> {
    
    Department findByName(String name);
    
    Department findByHandle(String handle);
    
    List<Department> findByIdIn(Collection<Long> ids);
    
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
    
    @Query(value =
        "select department " +
            "from Department department " +
            "inner join department.children child " +
            "where child.resource2 = :board")
    Department findByBoard(@Param("board") Board board);
    
}
