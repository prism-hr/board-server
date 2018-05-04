package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
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
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.REJECTED;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
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
    private ActionService actionService;

    @SpyBean
    private ResourceService resourceService;

    private LocalDateTime baseline;

    private User administrator;

    private User otherAdministrator;

    private University university;

    private Department departmentAccepted;

    private Department departmentRejected;

    private List<Board> departmentAcceptedBoards;

    private List<Board> departmentRejectedBoards;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();

        administrator = serviceHelper.setUpUser();
        otherAdministrator = serviceHelper.setUpUser();

        university = serviceHelper.setUpUniversity("university");

        departmentAccepted =
            serviceHelper.setUpDepartment(administrator, university, "department ACCEPTED", ACCEPTED);

        departmentRejected =
            serviceHelper.setUpDepartment(administrator, university, "department REJECTED", REJECTED);
        userRoleService.createUserRole(departmentRejected, otherAdministrator, ADMINISTRATOR);

        departmentAcceptedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
        resourceService.updateState(departmentAcceptedBoards.get(1), REJECTED);

        departmentRejectedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentRejected.getId()));
        resourceService.updateState(departmentRejectedBoards.get(1), REJECTED);

        reset(actionService, resourceService);
    }

    @After
    public void tearDown() {
        reset(actionService, resourceService);
    }

    @Test
    public void createBoard_success() {
        Board createdBoard = serviceHelper.setUpBoard(administrator, departmentAccepted, "board");
        Board selectedBoard = boardService.getById(administrator, createdBoard.getId());

        String[] postCategories = new String[]{"Employment", "Internship"};
        Stream.of(createdBoard, selectedBoard).forEach(board ->
            verifyBoard(
                board,
                departmentAccepted,
                "board",
                ACCEPTED,
                ACCEPTED,
                new Action[]{VIEW, EDIT, EXTEND, REJECT},
                baseline));

        verify(actionService, times(1))
            .executeAction(eq(administrator), eq(departmentAccepted), eq(EXTEND), any(Execution.class));

        verify(resourceService, times(1)).setName(createdBoard, "board");
        verify(resourceService, times(1)).setHandle(createdBoard);

        verify(resourceService, times(1))
            .updateCategories(createdBoard, POST, Stream.of(postCategories).collect(toList()));

        verify(resourceService, times(1))
            .createResourceRelation(departmentAccepted, createdBoard);

        verify(resourceService, times(1)).setIndexDataAndQuarter(createdBoard);

        verify(resourceService, times(1))
            .createResourceOperation(createdBoard, EXTEND, administrator);
    }

    @Test
    public void getBoards_success() {
        getBoards_successWhenAdministrator();
        getBoards_successWhenAdministratorAndState();
        getBoards_successWhenAdministratorAndDepartment();
        getBoards_successWhenAdministratorAndAction();
        getBoards_successWhenAdministratorAndSearchTerm();
        getBoards_successWhenAdministratorAndSearchTermTypo();
        getBoards_successWhenAdministratorAndSearchTermWithoutResults();
        getBoards_successWhenOtherAdministrator();
        getBoards_successWhenUnprivileged();
    }

    private void getBoards_successWhenAdministrator() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter());

        assertThat(boards).hasSize(4);
        verifyAdministratorBoards(departmentAccepted,
            boards.stream().filter(board -> board.getParent().equals(departmentAccepted)).collect(toList()),
            new Action[]{VIEW, EDIT, EXTEND, REJECT});

        verifyAdministratorBoards(departmentRejected,
            boards.stream().filter(board -> board.getParent().equals(departmentRejected)).collect(toList()),
            new Action[]{VIEW, EDIT, REJECT});
    }

    private void getBoards_successWhenAdministratorAndState() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter().setState(REJECTED));
        assertThat(boards).hasSize(2);

        verifyBoard(
            boards.get(0),
            departmentAccepted,
            "Research Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);

        verifyBoard(
            boards.get(1),
            departmentRejected,
            "Research Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);
    }

    private void getBoards_successWhenAdministratorAndDepartment() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
        assertThat(boards).hasSize(2);
        verifyAdministratorBoards(departmentAccepted, boards, new Action[]{VIEW, EDIT, EXTEND, REJECT});
    }

    private void getBoards_successWhenAdministratorAndAction() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setAction(RESTORE));

        assertThat(boards).hasSize(2);
        verifyBoard(
            boards.get(0),
            departmentAccepted,
            "Research Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);

        verifyBoard(
            boards.get(1),
            departmentRejected,
            "Research Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);
    }

    private void getBoards_successWhenAdministratorAndSearchTerm() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("career"));
        assertThat(boards).hasSize(2);
        verifyCareerBoards(boards);
    }

    private void getBoards_successWhenAdministratorAndSearchTermTypo() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("cuREER"));
        assertThat(boards).hasSize(2);
        verifyCareerBoards(boards);
    }

    private void getBoards_successWhenAdministratorAndSearchTermWithoutResults() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(boards).hasSize(0);
    }

    private void getBoards_successWhenOtherAdministrator() {
        List<Board> boards = boardService.getBoards(otherAdministrator, new ResourceFilter());
        assertThat(boards).hasSize(3);

        verifyUnprivilegedUserBoards(departmentAccepted,
            boards.stream().filter(board -> board.getParent().equals(departmentAccepted)).collect(toList()),
            new Action[]{VIEW, EXTEND});

        verifyAdministratorBoards(departmentRejected,
            boards.stream().filter(board -> board.getParent().equals(departmentRejected)).collect(toList()),
            new Action[]{VIEW, EDIT, REJECT});
    }

    private void getBoards_successWhenUnprivileged() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentAccepted, MEMBER)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentRejected, MEMBER)));

        scenarios.forEach(scenario -> {
            User user = scenario.user;
            LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

            List<Board> departmentBoards =
                boardService.getBoards(user, new ResourceFilter())
                    .stream()
                    .filter(this.departmentAcceptedBoards::contains)
                    .collect(toList());

            assertThat(departmentBoards).hasSize(1);
            verifyUnprivilegedUserBoards(departmentAccepted, departmentBoards, new Action[]{VIEW, EXTEND});

            List<Board> department2Boards =
                boardService.getBoards(user, new ResourceFilter())
                    .stream()
                    .filter(this.departmentRejectedBoards::contains)
                    .collect(toList());

            assertThat(department2Boards).hasSize(1);
            verifyUnprivilegedUserBoards(departmentRejected, department2Boards, new Action[]{VIEW});
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyBoard(Board board, Department expectedDepartment, String expectedName, State expectedState,
                             State expectedPreviousState, Action[] expectedActions, LocalDateTime baseline) {
        serviceHelper.verifyIdentity(board, expectedDepartment, expectedName);

        assertEquals(expectedState, board.getState());
        assertEquals(expectedPreviousState, board.getPreviousState());

        serviceHelper.verifyActions(board, expectedActions);
        serviceHelper.verifyTimestamps(board, baseline);
    }

    private void verifyAdministratorBoards(Department department, List<Board> boards, Action[] expectedActions) {
        verifyBoard(
            boards.get(0),
            department,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            expectedActions,
            baseline);

        verifyBoard(
            boards.get(1),
            department,
            "Research Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);
    }

    private void verifyUnprivilegedUserBoards(Department department, List<Board> boards, Action[] expectedActions) {
        verifyBoard(
            boards.get(0),
            department,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            expectedActions,
            baseline);
    }

    private void verifyCareerBoards(List<Board> boards) {
        verifyBoard(
            boards.get(0),
            departmentAccepted,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, REJECT},
            baseline);

        verifyBoard(
            boards.get(1),
            departmentRejected,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, REJECT},
            baseline);
    }

}
