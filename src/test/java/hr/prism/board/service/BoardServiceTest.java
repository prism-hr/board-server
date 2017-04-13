package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import java.util.Optional;

public class BoardServiceTest {
    
    BoardService boardService = new BoardService();
    
    @Test
    public void shouldNotBeAbleToPatchBoardWithNullName() {
        ExceptionUtil.verifyIllegalStateException(() ->
                boardService.patchBoard(new Board(), new BoardPatchDTO().setName(Optional.empty())),
            "Attempted to set department name to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchBoardWithNullHandle() {
        ExceptionUtil.verifyIllegalStateException(() ->
                boardService.patchBoard(new Board(), new BoardPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
            "Attempted to set department handle to null");
    }
    
}
