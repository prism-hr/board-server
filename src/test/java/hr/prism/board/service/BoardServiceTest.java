package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.workflow.Execution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_BOARD;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ActionService actionService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private BoardPatchService boardPatchService;

    @Mock
    private UserService userService;

    private BoardService boardService;

    @Before
    public void setUp() {
        boardService = new BoardService(
            boardRepository, actionService, resourceService, boardPatchService, userService);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(boardRepository, actionService, resourceService, boardPatchService, userService);
        reset(boardRepository, actionService, resourceService, boardPatchService, userService);
    }

    @Test
    public void createBoard_success() {
        User user = new User();
        user.setId(1L);

        when(userService.getUserSecured()).thenReturn(user);

        Department department = new Department();
        department.setId(1L);

        when(resourceService.getResource(user, DEPARTMENT, 1L)).thenReturn(department);
        when(actionService.executeAction(eq(user), eq(department), eq(EXTEND), any(Execution.class)))
            .then(invocation -> ((Execution) invocation.getArguments()[3]).execute());

        ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
        when(boardRepository.save(boardCaptor.capture())).thenAnswer(invocation -> {
            Board board = (Board) invocation.getArguments()[0];
            board.setId(2L);
            return board;
        });

        boardService.createBoard(1L,
            new BoardDTO().setName("board").setPostCategories(ImmutableList.of("category")));

        Board board = boardCaptor.getValue();
        verify(userService, times(1)).getUserSecured();
        verify(resourceService, times(1)).getResource(user, DEPARTMENT, 1L);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(department), eq(EXTEND), any(Execution.class));
        verify(resourceService, times(1))
            .checkUniqueName(BOARD, null, department, "board", DUPLICATE_BOARD);

        verify(resourceService, times(1)).createHandle(department, BOARD, "board");
        verify(boardRepository, times(1)).save(any(Board.class));

        verify(resourceService, times(1))
            .updateCategories(board, POST, ImmutableList.of("category"));
        verify(resourceService, times(1)).createResourceRelation(department, board);
        verify(resourceService, times(1)).setIndexDataAndQuarter(board);
    }

}
