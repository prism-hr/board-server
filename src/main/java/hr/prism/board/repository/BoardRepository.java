package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface BoardRepository extends MyRepository<Board, Long> {
    
    @Query(value =
        "select board.handle " +
            "from Board board " +
            "where board.handle like concat('%', :suggestedHandle) " +
            "order by board.handle desc")
    List<String> findHandleLikeSuggestedHandle(@Param("suggestedHandle") String suggestedHandle);
    
}
