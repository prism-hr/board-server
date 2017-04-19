package hr.prism.board.service;

import hr.prism.board.TestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Optional;

@TestContext
public class BoardServiceIT {
    
    @Inject
    private BoardService boardService;
    
    private Board board = (Board) new Board().setParent(new Department());
    
    @Test
    public void shouldNotBeAbleToPatchBoardWithNullName() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                boardService.updateBoard(board, new BoardPatchDTO().setName(Optional.empty())),
            ExceptionCode.MISSING_BOARD_NAME, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchBoardWithNullHandle() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                boardService.updateBoard(board, new BoardPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
            ExceptionCode.MISSING_BOARD_HANDLE, null);
    }
    
}
