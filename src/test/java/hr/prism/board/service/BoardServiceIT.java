package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.service.ServiceHelper.Scenario;
import hr.prism.board.service.ServiceHelper.Scenarios;
import hr.prism.board.value.ResourceFilter;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
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
    private UserRoleService userRoleService;

    @Inject
    private ServiceHelper serviceHelper;

    @SpyBean
    private ResourceDAO resourceDAO;

    @SpyBean
    private ActionService actionService;

    @SpyBean
    private ResourceService resourceService;

    private LocalDateTime baseline;

    private User administrator;

    private User otherAdministrator;

    private User author;

    private User otherAuthor;

    private University university;

    private Department departmentAccepted;

    private Department departmentRejected;

    private List<Board> departmentAcceptedBoards;

    private List<Board> departmentRejectedBoards;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
        administrator = serviceHelper.setUpUser();
        university = serviceHelper.setUpUniversity("university");

        departmentAccepted =
            serviceHelper.setUpDepartment(administrator, university, "department ACCEPTED", ACCEPTED);
        departmentRejected =
            serviceHelper.setUpDepartment(administrator, university, "department REJECTED", REJECTED);

        reset(actionService, resourceService);
    }

    @After
    public void tearDown() {
        reset(actionService, resourceService);
    }

    @Test
    public void getById_success() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(departmentAccepted, AUTHOR, MEMBER);
        verifyGetById(departmentAccepted, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, REJECT}, new Action[]{VIEW, EXTEND});
        verifyGetById(departmentRejected, scenarios, new Action[]{VIEW, EDIT, REJECT}, new Action[]{VIEW});
    }

    @Test
    public void getByHandle_success() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(departmentAccepted, AUTHOR, MEMBER);
        verifyGetByHandle(departmentAccepted, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, REJECT}, new Action[]{VIEW, EXTEND});
        verifyGetByHandle(departmentRejected, scenarios, new Action[]{VIEW, EDIT, REJECT}, new Action[]{VIEW});
    }

    @Test
    public void createBoard_success() {
        Board createdBoard =
            boardService.createBoard(administrator, departmentAccepted.getId(),
                new BoardDTO()
                    .setName("board")
                    .setPostCategories(ImmutableList.of("Employment", "Internship")));

        Board selectedBoard = boardService.getById(administrator, createdBoard.getId());
        Stream.of(createdBoard, selectedBoard).forEach(board -> {
            verifyBoard(board, departmentAccepted, "board", new Action[]{VIEW, EDIT, EXTEND, REJECT});
            assertEquals("university/department-accepted/board", board.getHandle());
            assertEquals(ACCEPTED, board.getState());
            assertEquals(ACCEPTED, board.getPreviousState());
            assertEquals(ImmutableList.of("Employment", "Internship"), board.getPostCategoryStrings());
        });

        verify(actionService, times(1))
            .executeAction(eq(administrator), eq(departmentAccepted), eq(EXTEND), any(Execution.class));

        verify(resourceDAO, times(1))
            .checkUniqueName(BOARD, null, departmentAccepted, "board");

        verify(resourceService, times(1)).createHandle(createdBoard);

        verify(resourceService, times(1))
            .updateCategories(createdBoard, POST, ImmutableList.of("Employment", "Internship"));

        verify(resourceService, times(1))
            .createResourceRelation(departmentAccepted, createdBoard);

        verify(resourceService, times(1)).setIndexDataAndQuarter(createdBoard);

        verify(resourceService, times(1))
            .createResourceOperation(createdBoard, EXTEND, administrator);
    }

    @Test
    public void getBoards_success() {
        setUpBoards();
        getBoards_successWhenAdministrator();
        getBoards_successWhenAdministratorAndState();
        getBoards_successWhenAdministratorAndDepartment();
        getBoards_successWhenAdministratorAndAction();
        getBoards_successWhenAdministratorAndSearchTerm();
        getBoards_successWhenAdministratorAndSearchTermTypo();
        getBoards_successWhenAdministratorAndSearchTermWithoutResults();
        getBoards_successWhenOtherAdministrator();
        getBoards_successWhenAuthor();
        getBoards_successWhenOtherAuthor();
        getBoards_successWhenUnprivileged();
    }

    private void setUpBoards() {
        otherAdministrator = serviceHelper.setUpUser();
        author = serviceHelper.setUpUser();
        otherAuthor = serviceHelper.setUpUser();

        userRoleService.createUserRole(departmentAccepted, author, AUTHOR);

        userRoleService.createUserRole(departmentRejected, otherAdministrator, ADMINISTRATOR);
        userRoleService.createUserRole(departmentRejected, author, AUTHOR);
        userRoleService.createUserRole(departmentRejected, otherAuthor, AUTHOR);

        departmentAcceptedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
        resourceService.updateState(departmentAcceptedBoards.get(1), REJECTED);

        departmentRejectedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentRejected.getId()));
        resourceService.updateState(departmentRejectedBoards.get(1), REJECTED);
    }

    private void getBoards_successWhenAdministrator() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter());

        assertThat(boards).hasSize(4);
        verifyAdministratorBoards(departmentAccepted,
            ImmutableList.of(boards.get(0), boards.get(2)),
            new Action[]{VIEW, EDIT, EXTEND, REJECT});

        verifyAdministratorBoards(departmentRejected,
            ImmutableList.of(boards.get(1), boards.get(3)),
            new Action[]{VIEW, EDIT, REJECT});
    }

    private void getBoards_successWhenAdministratorAndState() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setState(REJECTED));
        assertThat(boards).hasSize(2);

        verifyBoard(boards.get(0), departmentAccepted,
            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});

        verifyBoard(boards.get(1), departmentRejected,
            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
    }

    private void getBoards_successWhenAdministratorAndDepartment() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
        assertThat(boards).hasSize(2);
        verifyAdministratorBoards(departmentAccepted, boards, new Action[]{VIEW, EDIT, EXTEND, REJECT});
    }

    private void getBoards_successWhenAdministratorAndAction() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setAction(RESTORE));
        assertThat(boards).hasSize(2);

        verifyBoard(boards.get(0), departmentAccepted,
            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});

        verifyBoard(boards.get(1), departmentRejected,
            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
    }

    private void getBoards_successWhenAdministratorAndSearchTerm() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("career"));
        assertThat(boards).hasSize(2);
        verifyCareerBoards(boards);
    }

    private void getBoards_successWhenAdministratorAndSearchTermTypo() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("cuREER"));
        assertThat(boards).hasSize(2);
        verifyCareerBoards(boards);
    }

    private void getBoards_successWhenAdministratorAndSearchTermWithoutResults() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(boards).hasSize(0);
    }

    private void getBoards_successWhenAuthor() {
        List<Board> boards = boardService.getBoards(author, new ResourceFilter());
        assertThat(boards).hasSize(2);

        verifyAuthorBoards(departmentAccepted,
            singletonList(boards.get(0)),
            new Action[]{VIEW, EXTEND}, ACCEPTED);

        verifyUnprivilegedBoards(departmentRejected,
            singletonList(boards.get(1)),
            new Action[]{VIEW});
    }

    private void getBoards_successWhenOtherAdministrator() {
        List<Board> boards = boardService.getBoards(otherAdministrator, new ResourceFilter());
        assertThat(boards).hasSize(3);

        verifyAuthorBoards(departmentAccepted,
            singletonList(boards.get(0)),
            new Action[]{VIEW, EXTEND}, DRAFT);

        verifyAdministratorBoards(departmentRejected,
            ImmutableList.of(boards.get(1), boards.get(2)),
            new Action[]{VIEW, EDIT, REJECT});
    }

    private void getBoards_successWhenOtherAuthor() {
        List<Board> boards = boardService.getBoards(otherAuthor, new ResourceFilter());
        assertThat(boards).hasSize(2);

        verifyAuthorBoards(departmentAccepted,
            singletonList(boards.get(0)),
            new Action[]{VIEW, EXTEND}, DRAFT);

        verifyUnprivilegedBoards(departmentRejected,
            singletonList(boards.get(1)),
            new Action[]{VIEW});
    }

    private void getBoards_successWhenUnprivileged() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentAccepted, MEMBER)
                .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentRejected, MEMBER)));

        scenarios.forEach(scenario -> {
            User user = scenario.user;
            LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

            List<Board> boards =
                boardService.getBoards(user, new ResourceFilter())
                    .stream()
                    .filter(board -> departmentAcceptedBoards.contains(board) || departmentRejectedBoards.contains(board))
                    .collect(toList());
            assertThat(boards).hasSize(2);

            verifyAuthorBoards(
                departmentAccepted,
                singletonList(boards.get(0)),
                new Action[]{VIEW, EXTEND}, DRAFT);

            verifyUnprivilegedBoards(
                departmentRejected,
                singletonList(boards.get(1)),
                new Action[]{VIEW});
        });
    }

    private void verifyGetById(Department department, Scenarios scenarios, Action[] expectedAdministratorActions,
                               Action[] expectedUnprivilegedActions) {
        Board createdBoard = serviceHelper.setUpBoard(administrator, department, "board");
        Long createdBoardId = createdBoard.getId();
        verifyGetById(createdBoard, department, ACCEPTED, scenarios,
            expectedAdministratorActions,
            scenario -> {
                User user = scenario.user;
                reset(resourceService, actionService);

                Board selectedBoard = boardService.getById(user, createdBoardId);
                assertEquals(createdBoard, selectedBoard);

                verifyBoard(selectedBoard, department, "board", expectedUnprivilegedActions);
                verifyInvocations(user, createdBoardId, selectedBoard);
            });

        verifyGetById(createdBoard, department, REJECTED, scenarios,
            new Action[]{VIEW, EDIT, RESTORE},
            scenario -> {
                User user = scenario.user;
                assertThatThrownBy(() -> boardService.getById(user, createdBoardId))
                    .isExactlyInstanceOf(BoardForbiddenException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
            });
    }

    private void verifyGetById(Board createdBoard, Department department, State state, Scenarios scenarios,
                               Action[] expectedAdministratorActions, Consumer<Scenario> unprivilegedScenario) {
        resourceService.updateState(createdBoard, state);
        reset(resourceService, actionService);

        Long createdBoardId = createdBoard.getId();
        Board selectedBoard = boardService.getById(administrator, createdBoardId);
        assertEquals(createdBoard, selectedBoard);

        verifyBoard(selectedBoard, department, "board", expectedAdministratorActions);
        verifyInvocations(administrator, createdBoardId, selectedBoard);
        scenarios.forEach(unprivilegedScenario);
    }

    private void verifyInvocations(User user, Long createdBoardId, Board selectedBoard) {
        verify(resourceService, times(1))
            .getResource(user, BOARD, createdBoardId);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(selectedBoard), eq(VIEW), any(Execution.class));
    }

    private void verifyGetByHandle(Department department, Scenarios scenarios, Action[] expectedAdministratorActions,
                                   Action[] expectedUnprivilegedActions) {
        Board createdBoard = serviceHelper.setUpBoard(administrator, department, "board");
        String createdBoardHandle = createdBoard.getHandle();
        verifyGetByHandle(createdBoard, department, ACCEPTED, scenarios,
            expectedAdministratorActions,
            scenario -> {
                User user = scenario.user;
                reset(resourceService, actionService);

                Board selectedBoard = boardService.getByHandle(user, createdBoardHandle);
                assertEquals(createdBoard, selectedBoard);

                verifyBoard(selectedBoard, department, "board", expectedUnprivilegedActions);
                verifyInvocations(user, createdBoardHandle, selectedBoard);
            });

        verifyGetByHandle(createdBoard, department, REJECTED, scenarios,
            new Action[]{VIEW, EDIT, RESTORE},
            scenario -> {
                User user = scenario.user;
                assertThatThrownBy(() -> boardService.getByHandle(user, createdBoardHandle))
                    .isExactlyInstanceOf(BoardForbiddenException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
            });
    }

    private void verifyGetByHandle(Board createdBoard, Department department, State state, Scenarios scenarios,
                                   Action[] expectedAdministratorActions, Consumer<Scenario> unprivilegedScenario) {
        resourceService.updateState(createdBoard, state);
        reset(resourceService, actionService);

        String createdBoardHandle = createdBoard.getHandle();
        Board selectedBoard = boardService.getByHandle(administrator, createdBoardHandle);
        assertEquals(createdBoard, selectedBoard);

        verifyBoard(selectedBoard, department, "board", expectedAdministratorActions);
        verifyInvocations(administrator, createdBoardHandle, selectedBoard);
        scenarios.forEach(unprivilegedScenario);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyInvocations(User user, String createdBoardHandle, Board selectedBoard) {
        verify(resourceService, times(1))
            .getResource(user, BOARD, createdBoardHandle);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(selectedBoard), eq(VIEW), any(Execution.class));
    }


    @SuppressWarnings("SameParameterValue")
    private void verifyBoard(Board board, Department expectedDepartment, String expectedName,
                             Action[] expectedActions) {
        serviceHelper.verifyIdentity(board, expectedDepartment, expectedName);
        serviceHelper.verifyActions(board, expectedActions);
        serviceHelper.verifyTimestamps(board, baseline);
    }

    private void verifyAdministratorBoards(Department department, List<Board> boards, Action[] expectedActions) {
        verifyBoard(boards.get(0), department, "Career Opportunities", expectedActions);
        verifyBoard(boards.get(1), department,
            "Research Opportunities", new Action[]{VIEW, EDIT, RESTORE});
    }

    private void verifyAuthorBoards(Department department, List<Board> boards, Action[] expectedActions,
                                    State expectedExtendActionState) {
        Board board = boards.get(0);
        verifyBoard(board, department, "Career Opportunities", expectedActions);

        board.getActions()
            .stream()
            .filter(action -> action.getAction() == EXTEND)
            .findFirst()
            .map(action -> {
                assertEquals(expectedExtendActionState, action.getState());
                return action;
            })
            .orElseThrow(() -> new Error("Extend action was not found"));
    }

    private void verifyUnprivilegedBoards(Department department, List<Board> boards, Action[] expectedActions) {
        verifyBoard(boards.get(0), department, "Career Opportunities", expectedActions);
    }

    private void verifyCareerBoards(List<Board> boards) {
        verifyBoard(boards.get(0), departmentAccepted,
            "Career Opportunities", new Action[]{VIEW, EDIT, EXTEND, REJECT});

        verifyBoard(boards.get(1), departmentRejected,
            "Career Opportunities", new Action[]{VIEW, EDIT, REJECT});
    }

}
