package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@Transactional
public class BoardMapper implements Function<Board, BoardDTO> {
    @Override
    public BoardDTO apply(Board board) {
        return new BoardDTO()
                .withId(board.getId())
                .withName(board.getName())
                .withPurpose(board.getPurpose());
    }
}
