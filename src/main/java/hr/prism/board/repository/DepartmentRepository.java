package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends MyRepository<Department, Long> {
    
    Department findByName(String name);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "where department.id = :id " +
            "or department.name = :name")
    Department findByIdOrName(@Param("id") Long id, @Param("name") String name);
    
    @Query(value =
        "select department " +
            "from Department department " +
            "inner join department.children child " +
            "where child.resource2 = :board")
    Department findByBoard(@Param("board") Board board);
    
}
