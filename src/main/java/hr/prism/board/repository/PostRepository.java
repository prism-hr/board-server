package hr.prism.board.repository;

import hr.prism.board.domain.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends MyRepository<Post, Long> {
    
    List<Post> findByIdIn(Collection<Long> ids);
    
    @Query(value =
        "select post " +
            "from Post post " +
            "where post.id in (:ids) " +
            "order by post.updatedTimestamp")
    List<Post> findAllByUserByOrderByUpdatedTimestamp(@Param("ids") Collection<Long> ids);
    
}
