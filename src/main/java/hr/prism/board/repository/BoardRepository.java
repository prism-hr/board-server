package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends MyRepository<Board, Long> {
    
    @Query(value =
        "select board " +
            "from Board board " +
            "inner join board.parents parent " +
            "where (board.name = :name " +
            "or board.handle = :handle) " +
            "and parent.resource1 = :department")
    Iterable<Board> findByNameOrHandleAndDepartment(@Param("name") String name, @Param("handle") String handle, @Param("department") Department department);
    
    @Query(value =
        "select board " +
            "from Board board " +
            "inner join board.parents parent " +
            "where parent.resource1 = :department " +
            "order by board.id")
    List<Board> findByDepartment(@Param("department") Department department);
    
}
