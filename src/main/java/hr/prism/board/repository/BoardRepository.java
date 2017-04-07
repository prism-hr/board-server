package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends MyRepository<Board, Long> {
    
    Board findByHandle(String handle);
    
    @Query(value =
        "select board " +
            "from Board board " +
            "inner join board.parents parent " +
            "where board.name = :name " +
            "and parent.resource1 = :department")
    Board findByNameAndDepartment(@Param("name") String name, @Param("department") Department department);
    
}
