package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends MyRepository<Post, Long> {

    @Query(value =
        "select post " +
            "from Post post " +
            "inner join post.parents parent " +
            "where parent.resource1.id = :boardId " +
            "order by post.name")
    List<Post> findByBoard(@Param("boardId") Long boardId);


    @Query(value =
        "select post " +
            "from Post post " +
            "inner join post.parents parent " +
            "where post.name = :name " +
            "and parent.resource1 = :board")
    Post findByNameAndBoard(@Param("name") String name, @Param("board") Board board);

}
