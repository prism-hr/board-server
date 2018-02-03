package hr.prism.board.repository;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Transactional
@SuppressWarnings("JpaQlInspection")
public interface PostRepository extends BoardEntityRepository<Post, Long> {

    List<Post> findByName(String name);

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
            "and department.state <> :rejectedState " +
            "and (post.liveTimestamp <= :baseline " +
            "or post.liveTimestamp is null) " +
            "and (post.deadTimestamp >= :baseline " +
            "or post.deadTimestamp is null)")
    List<Long> findPostsToPublish(@Param("states") Collection<State> states, @Param("rejectedState") State rejectedState, @Param("baseline") LocalDateTime baseline);

    @Query(value =
        "select post " +
            "from Post post " +
            "where post.id in (" +
            "select max (resource.id) " +
            "from UserRole userRole " +
            "inner join userRole.resource resource " +
            "where userRole.user = :user " +
            "and userRole.role = :role " +
            "and resource.scope = :scope)")
    Post findLatestPost(@Param("user") User user, @Param("role") Role role, @Param("scope") Scope scope);

}
