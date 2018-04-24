package hr.prism.board.repository;

import hr.prism.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BoardRepository extends JpaRepository<Board, Long> {

}
