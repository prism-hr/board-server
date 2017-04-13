package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import java.util.Optional;

public class BoardServiceTest {
    
    private BoardService boardService = new BoardService();
    
    private Board board = (Board) new Board().setParent(new Department());
    
    @Test
    public void shouldNotBeAbleToPatchBoardWithNullName() {
        ExceptionUtil.verifyIllegalStateException(() ->
                boardService.updateBoard(board, new BoardPatchDTO().setName(Optional.empty())),
            "Attempted to set board name to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchBoardWithNullHandle() {
        ExceptionUtil.verifyIllegalStateException(() ->
                boardService.updateBoard(board, new BoardPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
            "Attempted to set board handle to null");
    }
    
}
