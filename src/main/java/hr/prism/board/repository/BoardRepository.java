package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardRepository extends MyRepository<Board, Long> {

    Board findByHandle(String handle);
    
    List<Board> findByIdIn(Collection<Long> ids);

    @Query(value =
        "select board " +
            "from Board board " +
            "where board.id in (:ids) " +
            "order by board.name")
    List<Board> findAllByUserByOrderByName(@Param("ids") Collection<Long> ids);

    @Query(value =
        "select board " +
            "from Board board " +
            "inner join board.parents parent " +
            "where board.name = :name " +
            "and parent.resource1 = :department")
    Board findByNameAndDepartment(@Param("name") String name, @Param("department") Department department);

    @Query(value =
        "select board " +
            "from Board board " +
            "inner join board.parents parent " +
            "where parent.resource1 = :department " +
            "order by board.name")
    List<Board> findByDepartment(@Param("department") Department department);

    @Query(value =
        "select board " +
            "from Board board " +
            "inner join board.children child " +
            "where child.resource2 = :post")
    Board findByPost(@Param("post") Post post);

}
