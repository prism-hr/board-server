package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import org.springframework.data.jpa.repository.Modifying;
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
    
    @Modifying
    @Query(value =
        "UPDATE resource " +
            "INNER JOIN ( " +
            "SELECT resource.parent_id AS board_id, " +
            "COUNT(resource.id) AS post_count " +
            "FROM resource " +
            "WHERE resource.parent_id IN ( " +
            "SELECT resource.parent_id " +
            "FROM resource " +
            "WHERE resource.id IN (:postIds)) " +
            "AND resource.state = :state " +
            "GROUP BY resource.parent_id) as post_summary " +
            "ON resource.id = post_summary.board_id " +
            "SET resource.post_count = post_summary.post_count",
        nativeQuery = true)
    void updateBoardPostCounts(@Param("postIds") List<Long> postIds, @Param("state") String state);
    
}
