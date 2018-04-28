package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.service.BoardService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BoardApiTest {

    @Mock
    private BoardService boardService;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private ResourceOperationMapper resourceOperationMapper;

    private BoardApi boardApi;

    @Before
    public void setUp() {
        boardApi = new BoardApi(boardService, boardMapper, resourceOperationMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(boardService, boardMapper, resourceOperationMapper);
        reset(boardService, boardMapper, resourceOperationMapper);
    }

    @Test
    public void createDepartment_success() {
        Board board = new Board();
        board.setId(1L);

        BoardDTO boardDTO =
            new BoardDTO()
                .setName("department");

        when(boardService.createBoard(1L, boardDTO)).thenReturn(board);

        boardApi.createBoard(1L, boardDTO);

        verify(boardService, times(1)).createBoard(1L, boardDTO);
        verify(boardMapper, times(1)).apply(board);
    }

}
