package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.service.BoardService;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceFilter.ResourceFilterList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static hr.prism.board.enums.State.ACCEPTED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/api_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class BoardApiIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ApiHelper apiHelper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private BoardMapper boardMapper;

    private User user;

    private BoardDTO boardDTO;

    private Board board;

    private ResourceFilter filter;

    @Before
    public void setUp() {
        user = new User();
        user.setId(1L);

        boardDTO =
            new BoardDTO()
                .setName("board");

        board = new Board();
        board.setId(3L);

        filter =
            new ResourceFilter()
                .setSearchTerm("search")
                .setState(ResourceFilterList.of(ACCEPTED));

        when(boardService.createBoard(user, 2L, boardDTO)).thenReturn(board);
        when(boardService.getBoards(user, filter)).thenReturn(singletonList(board));
        when(boardService.getBoards(null, new ResourceFilter())).thenReturn(emptyList());
        when(boardService.getById(any(User.class), eq(3L))).thenReturn(board);
        when(boardService.getByHandle(any(User.class), eq("university/department/board"))).thenReturn(board);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(boardService, boardMapper);
    }

    @Test
    public void createBoard_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            post("/api/departments/2/boards")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization)
                .content(objectMapper.writeValueAsString(boardDTO)))
            .andExpect(status().isOk());

        verify(boardService, times(1)).createBoard(user, 2L, boardDTO);
        verify(boardMapper, times(1)).apply(board);
    }

    @Test
    public void createBoard_failureWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            post("/api/departments/2/boards")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(boardDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getBoards_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/boards?searchTerm=search&state=ACCEPTED")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(boardService, times(1)).getBoards(user, filter);
        verify(boardMapper, times(1)).apply(board);
    }

    @Test
    public void getBoards_successWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/boards")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(boardService, times(1)).getBoards(null, new ResourceFilter());
    }

    @Test
    public void getBoard_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/boards/3")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(boardService, times(1)).getById(user, 3L);
        verify(boardMapper, times(1)).apply(board);
    }

    @Test
    public void getBoard_successWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/boards/3")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(boardService, times(1)).getById(null, 3L);
        verify(boardMapper, times(1)).apply(board);
    }

    @Test
    public void getBoard_successWhenHandleAndAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/boards?handle=university/department/board")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(boardService, times(1)).getByHandle(user, "university/department/board");
        verify(boardMapper, times(1)).apply(board);
    }

    @Test
    public void getBoard_successWhenHandleAndUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/boards?handle=university/department/board")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(boardService, times(1))
            .getByHandle(null, "university/department/board");
        verify(boardMapper, times(1)).apply(board);
    }

}
