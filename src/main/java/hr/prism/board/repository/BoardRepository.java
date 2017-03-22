package hr.prism.board.repository;

import hr.prism.board.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends MyRepository<Board, Long> {

    Board findByHandle(String handle);

    @Query(value =
        "select board " +
            "from Board board " +
            "where board.id in (" +
            ResourceRepository.RESOURCE_IDS_BY_USER + " = '" + Scope.Value.BOARD + "') " +
            "order by board.name")
    List<Board> findAllByUserByOrderByName(@Param("user") User user);

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
