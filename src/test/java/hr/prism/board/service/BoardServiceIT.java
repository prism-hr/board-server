package hr.prism.board.service;

import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Optional;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
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
