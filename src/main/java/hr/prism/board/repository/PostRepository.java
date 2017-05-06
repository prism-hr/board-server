package hr.prism.board.repository;

import hr.prism.board.domain.Post;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface PostRepository extends MyRepository<Post, Long> {
    
    @Query(value =
        "select post.id " +
            "from Post post " +
            "where post.state = :state " +
            "and post.deadTimestamp is not null " +
            "and post.deadTimestamp < :baseline")
    List<Long> findPostsToRetire(@Param("state") State state, @Param("baseline") LocalDateTime baseline);
    
    @Query(value =
        "select post.id " +
            "from Post post " +
            "where post.state = :state " +
            "and post.liveTimestamp < :baseline " +
            "and (post.deadTimestamp >= :baseline " +
            "or post.deadTimestamp is null)")
    List<Long> findPostsToPublish(@Param("state") State state, @Param("baseline") LocalDateTime baseline);
    
}
