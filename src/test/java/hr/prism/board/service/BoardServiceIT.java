package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceFilter.ResourceFilterList;
import hr.prism.board.workflow.Execution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.REJECTED;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/boardService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class BoardServiceIT {

    private static final Logger LOGGER = getLogger(BoardServiceIT.class);

    @Inject
    private BoardService boardService;

    @Inject
    private UserService userService;

    @Inject
    private ServiceHelper serviceHelper;

    @SpyBean
    private ResourceDAO resourceDAO;

    @SpyBean
    private ActionService actionService;

    @SpyBean
    private ResourceService resourceService;

    @Before
    public void setUp() {
        reset(actionService, resourceService);
    }

    @After
    public void tearDown() {
        reset(actionService, resourceService);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyGetById(users, 3L,
            department, "department-accepted-board-accepted", new Action[]{VIEW, EDIT, EXTEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyGetById(users, 3L,
            department, "department-accepted-board-accepted", new Action[]{VIEW, EXTEND});
        verifyGetById((User) null, 3L,
            department, "department-accepted-board-accepted", new Action[]{VIEW, EXTEND});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyGetById(users, 5L,
            department, "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedAndBoardRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = new Board();
        board.setId(5L);

        verifyGetByIdFailure(users, 5L, board);
        verifyGetByIdFailure((User) null, 5L, board);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-rejected");
        verifyGetById(users, 8L,
            department, "department-rejected-board-accepted", new Action[]{VIEW, EDIT, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedAndBoardAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-rejected");
        verifyGetById(users, 8L,
            department, "department-rejected-board-accepted", new Action[]{VIEW});
        verifyGetById((User) null, 8L,
            department, "department-rejected-board-accepted", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-rejected");
        verifyGetById(users, 10L,
            department, "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = new Board();
        board.setId(10L);

        verifyGetByIdFailure(users, 10L, board);
        verifyGetByIdFailure((User) null, 10L, board);
    }

    @Test
    public void getByHandle_successWhenDepartmentAndBoardAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyGetById(users, 3L,
            department, "department-accepted-board-accepted", new Action[]{VIEW, EDIT, EXTEND, REJECT});
    }

    @Test
    public void getByHandle_successWhenDepartmentAndBoardAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyGetByHandle(users, "university/department-accepted/board-accepted",
            department, "department-accepted-board-accepted", new Action[]{VIEW, EXTEND});
        verifyGetByHandle((User) null, "university/department-accepted/board-accepted",
            department, "department-accepted-board-accepted", new Action[]{VIEW, EXTEND});
    }

    @Test
    public void getByHandle_successWhenDepartmentAcceptedBoardRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyGetByHandle(users, "university/department-accepted/board-rejected",
            department, "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getByHandle_failureWhenDepartmentAcceptedAndBoardRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = new Board();
        board.setId(5L);

        verifyGetByHandleFailure(users, "university/department-accepted/board-rejected", board);
        verifyGetByHandleFailure((User) null, "university/department-accepted/board-rejected", board);
    }

    @Test
    public void getByHandle_successWhenDepartmentRejectedBoardAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-rejected");
        verifyGetByHandle(users, "university/department-rejected/board-accepted",
            department, "department-rejected-board-accepted", new Action[]{VIEW, EDIT, REJECT});
    }

    @Test
    public void getByHandle_successWhenDepartmentRejectedAndBoardAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-rejected");
        verifyGetByHandle(users, "university/department-rejected/board-accepted",
            department, "department-rejected-board-accepted", new Action[]{VIEW});
        verifyGetByHandle((User) null, "university/department-rejected/board-accepted",
            department, "department-rejected-board-accepted", new Action[]{VIEW});
    }

    @Test
    public void getByHandle_successWhenDepartmentAndBoardRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-rejected");
        verifyGetByHandle(users, "university/department-rejected/board-rejected",
            department, "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getByHandle_failureWhenDepartmentAndBoardRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = new Board();
        board.setId(10L);

        verifyGetByHandleFailure(users, "university/department-rejected/board-rejected", board);
        verifyGetByHandleFailure((User) null, "university/department-rejected/board-rejected", board);
    }

    @Test
    public void createBoard_success() {
        LocalDateTime baseline = LocalDateTime.now();
        User user = userService.getByEmail("department-administrator@prism.hr");

        Board createdBoard =
            boardService.createBoard(user, 2L,
                new BoardDTO()
                    .setName("department-accepted-board")
                    .setPostCategories(ImmutableList.of("post category 1", "post category 2")));

        Department department = new Department();
        department.setId(2L);

        Board selectedBoard = boardService.getById(user, createdBoard.getId());
        Stream.of(createdBoard, selectedBoard).forEach(board -> {
            verifyBoard(board, department, "department-accepted-board",
                new Action[]{VIEW, EDIT, EXTEND, REJECT});
            assertEquals("university/department-accepted/department-accepted-board", board.getHandle());
            assertEquals(ACCEPTED, board.getState());
            assertEquals(ACCEPTED, board.getPreviousState());
            assertEquals(ImmutableList.of("post category 1", "post category 2"), board.getPostCategoryStrings());
            serviceHelper.verifyTimestamps(board, baseline);
        });

        verify(actionService, times(1))
            .executeAction(eq(user), eq(department), eq(EXTEND), any(Execution.class));

        verify(resourceDAO, times(1))
            .checkUniqueName(BOARD, null, department, "department-accepted-board");

        verify(resourceService, times(1)).createHandle(createdBoard);

        verify(resourceService, times(1))
            .updateCategories(createdBoard, POST, ImmutableList.of("post category 1", "post category 2"));

        verify(resourceService, times(1))
            .createResourceRelation(department, createdBoard);

        verify(resourceService, times(1)).setIndexDataAndQuarter(createdBoard);

        verify(resourceService, times(1))
            .createResourceOperation(createdBoard, EXTEND, user);
    }

    @Test
    public void getBoards_successWhenDepartmentAdministrator() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter());

        assertThat(boards).hasSize(4);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-accepted", new Action[]{VIEW, EDIT, EXTEND, REJECT});

        verifyBoard(boards.get(1), departmentAccepted,
            "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(2), departmentRejected,
            "department-rejected-board-accepted", new Action[]{VIEW, EDIT, REJECT});

        verifyBoard(boards.get(3), departmentRejected,
            "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_successWhenDepartmentAdministratorAndState() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user,
            new ResourceFilter().setState(ResourceFilterList.of(REJECTED)));

        assertThat(boards).hasSize(2);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(1), departmentRejected,
            "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_successWhenDepartmentAdministratorAndDepartment() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter().setParentId(2L));

        assertThat(boards).hasSize(2);

        Department department = new Department();
        department.setId(2L);

        verifyBoard(boards.get(0), department,
            "department-accepted-board-accepted", new Action[]{VIEW, EDIT, EXTEND, REJECT});

        verifyBoard(boards.get(1), department,
            "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_successWhenDepartmentAdministratorAndAction() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter().setAction(RESTORE));

        assertThat(boards).hasSize(2);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(1), departmentRejected,
            "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_successWhenDepartmentAdministratorAndSearchTerm() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter().setSearchTerm("rejected"));

        assertThat(boards).hasSize(3);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(1), departmentRejected,
            "department-rejected-board-accepted", new Action[]{VIEW, EDIT, REJECT});

        verifyBoard(boards.get(2), departmentRejected,
            "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_successWhenDepartmentAdministratorAndSearchTermTypo() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter().setSearchTerm("rIJECT"));

        assertThat(boards).hasSize(3);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-rejected", new Action[]{VIEW, EDIT, RESTORE});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(1), departmentRejected,
            "department-rejected-board-accepted", new Action[]{VIEW, EDIT, REJECT});

        verifyBoard(boards.get(2), departmentRejected,
            "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_failureWhenDepartmentAdministratorAndSearchTermWithoutResults() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter().setSearchTerm("xyz"));

        assertThat(boards).hasSize(0);
    }

    @Test
    public void getBoards_successWhenOtherDepartmentAdministrator() {
        User user = userService.getByEmail("department-rejected-administrator@prism.hr");

        List<Board> boards = boardService.getBoards(user, new ResourceFilter());

        assertThat(boards).hasSize(3);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-accepted", new Action[]{VIEW, EXTEND});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(1), departmentRejected,
            "department-rejected-board-accepted", new Action[]{VIEW, EDIT, REJECT});

        verifyBoard(boards.get(2), departmentRejected,
            "department-rejected-board-rejected", new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getBoards_successWhenUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        verifyGetBoards(users);
        verifyGetBoards((User) null);
    }

    private void verifyGetById(User[] users, Long id, Department expectedDepartment, String expectedName,
                               Action[] expectedActions) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetById(user, id, expectedDepartment, expectedName, expectedActions);
        });
    }

    private void verifyGetById(User user, Long id, Department expectedDepartment, String expectedName,
                               Action[] expectedActions) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by id: " + id + ": " + userGivenName);

        Board board = boardService.getById(user, id);

        verifyBoard(board, expectedDepartment, expectedName, expectedActions);
        verifyInvocations(user, id, board);
    }

    private void verifyGetByIdFailure(User[] users, Long id, Board board) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByIdFailure(user, id, board);
        });
    }

    private void verifyGetByIdFailure(User user, Long id, Board board) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by id: " + id + ": " + userGivenName);

        assertThatThrownBy(() -> boardService.getById(user, id))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

        verifyInvocations(user, id, board);
    }

    private void verifyGetByHandle(User[] users, String handle, Department expectedDepartment, String expectedName,
                                   Action[] expectedActions) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByHandle(user, handle, expectedDepartment, expectedName, expectedActions);
        });
    }

    private void verifyGetByHandle(User user, String handle, Department expectedDepartment, String expectedName,
                                   Action[] expectedActions) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by handle: " + handle + ": " + userGivenName);

        Board board = boardService.getByHandle(user, handle);

        verifyBoard(board, expectedDepartment, expectedName, expectedActions);
        verifyInvocations(user, handle, board);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByHandleFailure(User[] users, String handle, Board board) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByHandleFailure(user, handle, board);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByHandleFailure(User user, String handle, Board board) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by handle: " + handle + ": " + userGivenName);

        assertThatThrownBy(() -> boardService.getByHandle(user, handle))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

        verifyInvocations(user, handle, board);
    }

    private void verifyBoard(Board board, Department expectedDepartment, String expectedName,
                             Action[] expectedActions) {
        serviceHelper.verifyIdentity(board, expectedDepartment, expectedName);
        serviceHelper.verifyActions(board, expectedActions);
    }

    private void verifyGetBoards(User[] users) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetBoards(user);
        });
    }

    private void verifyGetBoards(User user) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Getting boards: " + userGivenName);
        List<Board> boards = boardService.getBoards(user, new ResourceFilter());

        assertThat(boards).hasSize(2);

        Department departmentAccepted = new Department();
        departmentAccepted.setId(2L);

        verifyBoard(boards.get(0), departmentAccepted,
            "department-accepted-board-accepted", new Action[]{VIEW, EXTEND});

        Department departmentRejected = new Department();
        departmentRejected.setId(7L);

        verifyBoard(boards.get(1), departmentRejected,
            "department-rejected-board-accepted", new Action[]{VIEW});
    }

    private void verifyInvocations(User user, Long id, Board board) {
        verify(resourceService, times(1))
            .getResource(user, BOARD, id);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(board), eq(VIEW), any(Execution.class));
    }

    private void verifyInvocations(User user, String handle, Board board) {
        verify(resourceService, times(1))
            .getResource(user, BOARD, handle);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(board), eq(VIEW), any(Execution.class));
    }

}
