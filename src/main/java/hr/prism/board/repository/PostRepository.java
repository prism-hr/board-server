package hr.prism.board.repository;

import hr.prism.board.domain.Post;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends MyRepository<Post, Long> {
    
    @Query(value =
        "select post " +
            "from Post post " +
            "where post.state = :state " +
            "and post.deadTimestamp < :deadTimestamp")
    List<Post> findPostsToRetire(@Param("state") State state, @Param("deadTimestamp") LocalDateTime deadTimestamp);
    
    @Query(value =
        "select post " +
            "from Post post " +
            "where post.state = :state " +
            "and post.liveTimestamp >= :liveTimestamp " +
            "and post.deadTimestamp < :liveTimestamp")
    List<Post> findPostsToPublish(@Param("state") State state, @Param("liveTimestamp") LocalDateTime liveTimestamp);
    
}
