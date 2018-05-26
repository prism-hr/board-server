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
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/boardService_setUp.sql")
@Sql(scripts = "classpath:data/boardService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
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

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByIdFailure(User[] users, Long id, Board board) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByIdFailure(user, id, board);
        });
    }

    @SuppressWarnings("SameParameterValue")
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

//    @Test
//    public void getBoards_success() {
//        setUpBoards();
//        getBoards_successWhenAdministrator();
//        getBoards_successWhenAdministratorAndState();
//        getBoards_successWhenAdministratorAndDepartment();
//        getBoards_successWhenAdministratorAndAction();
//        getBoards_successWhenAdministratorAndSearchTerm();
//        getBoards_successWhenAdministratorAndSearchTermTypo();
//        getBoards_successWhenAdministratorAndSearchTermWithoutResults();
//        getBoards_successWhenOtherAdministrator();
//        getBoards_successWhenAuthor();
//        getBoards_successWhenOtherAuthor();
//        getBoards_successWhenUnprivileged();
//    }
//
//    private void setUpBoards() {
//        otherAdministrator = serviceHelper.setUpUser();
//        author = serviceHelper.setUpUser();
//        otherAuthor = serviceHelper.setUpUser();
//
//        userRoleService.createUserRole(departmentRejected, otherAdministrator, ADMINISTRATOR);
//        userRoleService.createUserRole(departmentAccepted, author, AUTHOR);
//        userRoleService.createUserRole(departmentRejected, author, AUTHOR);
//        userRoleService.createUserRole(departmentRejected, otherAuthor, AUTHOR);
//
//        departmentAcceptedBoards =
//            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
//        resourceService.updateState(departmentAcceptedBoards.get(1), REJECTED);
//
//        departmentRejectedBoards =
//            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentRejected.getId()));
//        resourceService.updateState(departmentRejectedBoards.get(1), REJECTED);
//    }
//
//    private void getBoards_successWhenAdministrator() {
//        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter());
//
//        assertThat(boards).hasSize(4);
//        verifyAdministratorBoards(departmentAccepted,
//            ImmutableList.of(boards.get(0), boards.get(2)),
//            new Action[]{VIEW, EDIT, EXTEND, REJECT});
//
//        verifyAdministratorBoards(departmentRejected,
//            ImmutableList.of(boards.get(1), boards.get(3)),
//            new Action[]{VIEW, EDIT, REJECT});
//    }
//
//    private void getBoards_successWhenAdministratorAndState() {
//        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setState(REJECTED));
//        assertThat(boards).hasSize(2);
//
//        verifyBoard(boards.get(0), departmentAccepted,
//            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyBoard(boards.get(1), departmentRejected,
//            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
//    }
//
//    private void getBoards_successWhenAdministratorAndDepartment() {
//        List<Board> boards =
//            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
//        assertThat(boards).hasSize(2);
//        verifyAdministratorBoards(departmentAccepted, boards, new Action[]{VIEW, EDIT, EXTEND, REJECT});
//    }
//
//    private void getBoards_successWhenAdministratorAndAction() {
//        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setAction(RESTORE));
//        assertThat(boards).hasSize(2);
//
//        verifyBoard(boards.get(0), departmentAccepted,
//            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyBoard(boards.get(1), departmentRejected,
//            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
//    }
//
//    private void getBoards_successWhenAdministratorAndSearchTerm() {
//        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("career"));
//        assertThat(boards).hasSize(2);
//        verifyCareerBoards(boards);
//    }
//
//    private void getBoards_successWhenAdministratorAndSearchTermTypo() {
//        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("cuREER"));
//        assertThat(boards).hasSize(2);
//        verifyCareerBoards(boards);
//    }
//
//    private void getBoards_successWhenAdministratorAndSearchTermWithoutResults() {
//        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("xyz"));
//        assertThat(boards).hasSize(0);
//    }
//
//    private void getBoards_successWhenAuthor() {
//        List<Board> boards = boardService.getBoards(author, new ResourceFilter());
//        assertThat(boards).hasSize(2);
//
//        verifyAuthorBoards(departmentAccepted,
//            singletonList(boards.get(0)),
//            new Action[]{VIEW, EXTEND}, ACCEPTED);
//
//        verifyUnprivilegedBoards(departmentRejected,
//            singletonList(boards.get(1)),
//            new Action[]{VIEW});
//    }
//
//    private void getBoards_successWhenOtherAdministrator() {
//        List<Board> boards = boardService.getBoards(otherAdministrator, new ResourceFilter());
//        assertThat(boards).hasSize(3);
//
//        verifyAuthorBoards(departmentAccepted,
//            singletonList(boards.get(0)),
//            new Action[]{VIEW, EXTEND}, DRAFT);
//
//        verifyAdministratorBoards(departmentRejected,
//            ImmutableList.of(boards.get(1), boards.get(2)),
//            new Action[]{VIEW, EDIT, REJECT});
//    }
//
//    private void getBoards_successWhenOtherAuthor() {
//        List<Board> boards = boardService.getBoards(otherAuthor, new ResourceFilter());
//        assertThat(boards).hasSize(2);
//
//        verifyAuthorBoards(departmentAccepted,
//            singletonList(boards.get(0)),
//            new Action[]{VIEW, EXTEND}, DRAFT);
//
//        verifyUnprivilegedBoards(departmentRejected,
//            singletonList(boards.get(1)),
//            new Action[]{VIEW});
//    }
//
//    private void getBoards_successWhenUnprivileged() {
//        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
//            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentAccepted, MEMBER)
//                .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentRejected, MEMBER)));
//
//        scenarios.forEach(scenario -> {
//            User user = scenario.user;
//            LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");
//
//            List<Board> boards =
//                boardService.getBoards(user, new ResourceFilter())
//                    .stream()
//                    .filter(board -> departmentAcceptedBoards.contains(board) || departmentRejectedBoards.contains(board))
//                    .collect(toList());
//            assertThat(boards).hasSize(2);
//
//            verifyAuthorBoards(
//                departmentAccepted,
//                singletonList(boards.get(0)),
//                new Action[]{VIEW, EXTEND}, DRAFT);
//
//            verifyUnprivilegedBoards(
//                departmentRejected,
//                singletonList(boards.get(1)),
//                new Action[]{VIEW});
//        });
//    }
//
//    @SuppressWarnings("SameParameterValue")
//    private void verifyAdministratorBoards(Department department, List<Board> boards, Action[] expectedActions) {
//        verifyBoard(boards.get(0), department, "Career Opportunities", expectedActions);
//        verifyBoard(boards.get(1), department,
//            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
//    }
//
//    private void verifyAuthorBoards(Department department, List<Board> boards, Action[] expectedActions,
//                                    State expectedExtendActionState) {
//        Board board = boards.get(0);
//        verifyBoard(board, department, "Career Opportunities", expectedActions);
//
//        board.getActions()
//            .stream()
//            .filter(action -> action.getAction() == EXTEND)
//            .findFirst()
//            .map(action -> {
//                assertEquals(expectedExtendActionState, action.getState());
//                return action;
//            })
//            .orElseThrow(() -> new Error("Extend action was not found"));
//    }
//
//    private void verifyUnprivilegedBoards(Department department, List<Board> boards, Action[] expectedActions) {
//        verifyBoard(boards.get(0), department, "Career Opportunities", expectedActions);
//    }
//
//    private void verifyCareerBoards(List<Board> boards) {
//        verifyBoard(boards.get(0), departmentAccepted,
//            "Career Opportunities", new Action[]{VIEW, EDIT, EXTEND, REJECT});
//
//        verifyBoard(boards.get(1), departmentRejected,
//            "Career Opportunities", new Action[]{VIEW, EDIT, REJECT});
//    }

}
