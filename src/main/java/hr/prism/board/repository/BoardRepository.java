package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends MyRepository<Board, Long> {
    
    List<Board> findByName(@Param("name") String name);
    
}
