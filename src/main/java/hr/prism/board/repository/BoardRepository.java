package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Override
    @EntityGraph("board.extended")
    List<Board> findAll();

}
