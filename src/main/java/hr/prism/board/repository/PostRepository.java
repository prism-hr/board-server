package hr.prism.board.repository;

import hr.prism.board.domain.Post;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Transactional
public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph("post.extended")
    List<Post> findAll();

    @Query(value =
        "select post.id " +
            "from Post post " +
            "where post.state in (:states) " +
            "and post.deadTimestamp is not null " +
            "and post.deadTimestamp < :baseline")
    List<Long> findPostsToRetire(@Param("states") Collection<State> states, @Param("baseline") LocalDateTime baseline);

    @Query(value =
        "select post.id " +
            "from Post post " +
            "inner join post.parent board " +
            "inner join board.parent department " +
            "where post.state in (:states) " +
            "and board.state <> :rejectedState " +
            "and department.state <> :rejectedState " +
            "and (post.liveTimestamp <= :baseline " +
            "or post.liveTimestamp is null) " +
            "and (post.deadTimestamp >= :baseline " +
            "or post.deadTimestamp is null)")
    List<Long> findPostsToPublish(@Param("states") Collection<State> states,
                                  @Param("rejectedState") State rejectedState,
                                  @Param("baseline") LocalDateTime baseline);

}
