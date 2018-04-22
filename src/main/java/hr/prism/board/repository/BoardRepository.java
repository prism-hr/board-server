package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query(value =
        "select board.handle " +
            "from Board board " +
            "where board.handle like concat('%', :suggestedHandle) " +
            "or board.handle like concat('%', :suggestedHandle, '-%') " +
            "order by board.handle desc")
    List<String> findHandleLikeSuggestedHandle(@Param("suggestedHandle") String suggestedHandle);

}
