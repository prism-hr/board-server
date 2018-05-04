package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.repository.BoardRepository;
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
    private BoardRepository boardRepository;

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

    private Department departmentAccepted;

    private Department departmentRejected;

    private List<Board> departmentAcceptedBoards;

    private List<Board> departmentRejectedBoards;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();

        administrator = serviceHelper.setUpUser();
        otherAdministrator = serviceHelper.setUpUser();

        University university = serviceHelper.setUpUniversity("university");

        departmentAccepted =
            serviceHelper.setUpDepartment(administrator, university, "department ACCEPTED", ACCEPTED);

        departmentRejected =
            serviceHelper.setUpDepartment(administrator, university, "department REJECTED", REJECTED);
        userRoleService.createUserRole(departmentRejected, otherAdministrator, ADMINISTRATOR);

        serviceHelper.setUpBoard(administrator, departmentAccepted, "Rejected Opportunities", REJECTED);
        departmentAcceptedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));

        serviceHelper.setUpBoard(administrator, departmentRejected, "Rejected Opportunities", REJECTED);
        departmentRejectedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentRejected.getId()));

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
    public void getBoards_successWhenAdministrator() {
        List<Board> boards = boardService.getBoards(administrator, new ResourceFilter());

        assertThat(boards).hasSize(6);
        verifyAdministratorBoards(departmentAccepted,
            boards.stream().filter(board -> board.getParent().equals(departmentAccepted)).collect(toList()),
            new Action[]{VIEW, EDIT, EXTEND, REJECT});

        verifyAdministratorBoards(departmentRejected,
            boards.stream().filter(board -> board.getParent().equals(departmentRejected)).collect(toList()),
            new Action[]{VIEW, EDIT, REJECT});
    }

    @Test
    public void getBoards_successWhenOtherAdministrator() {
        List<Board> boards = boardService.getBoards(otherAdministrator, new ResourceFilter());
        assertThat(boards).hasSize(5);

        verifyUnprivilegedUserBoards(departmentAccepted,
            boards.stream().filter(board -> board.getParent().equals(departmentAccepted)).collect(toList()),
            new Action[]{VIEW, EXTEND});

        verifyAdministratorBoards(departmentRejected,
            boards.stream().filter(board -> board.getParent().equals(departmentRejected)).collect(toList()),
            new Action[]{VIEW, EDIT, REJECT});
    }

    @Test
    public void getBoards_successWhenOtherAdministratorAndRejected() {
        List<Board> boards = boardService.getBoards(otherAdministrator, new ResourceFilter().setState(REJECTED));
        assertThat(boards).hasSize(1);

        verifyBoard(
            boards.get(0),
            departmentRejected,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);
    }

    @Test
    public void getBoards_successWhenAdministratorAndDepartment() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
        assertThat(boards).hasSize(3);
        verifyAdministratorBoards(departmentAccepted, boards, new Action[]{VIEW, EDIT, EXTEND, REJECT});
    }

    @Test
    public void getBoards_successWhenAdministratorAndAction() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setAction(RESTORE));

        assertThat(boards).hasSize(2);
        verifyBoard(
            boards.get(0),
            departmentAccepted,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);

        verifyBoard(
            boards.get(1),
            departmentRejected,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);
    }

    @Test
    public void getBoards_successWhenAdministratorAndSearchTerm() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("career"));
        assertThat(boards).hasSize(2);
        verifyCareerBoards(boards);
    }

    @Test
    public void getBoards_successWhenAdministratorAndSearchTermTypo() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("cuREER"));
        assertThat(boards).hasSize(2);
        verifyCareerBoards(boards);
    }

    @Test
    public void getBoards_failureWhenAdministratorAndSearchTerm() {
        List<Board> boards =
            boardService.getBoards(administrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(boards).hasSize(0);
    }

    @Test
    public void getBoards_successWhenOtherAdministratorAndDepartment() {
        List<Board> boards =
            boardService.getBoards(otherAdministrator, new ResourceFilter().setParentId(departmentRejected.getId()));
        assertThat(boards).hasSize(3);
        verifyAdministratorBoards(departmentRejected, boards, new Action[]{VIEW, EDIT, REJECT});
    }

    @Test
    public void getBoards_failureWhenOtherAdministratorWrongDepartmentAndRejected() {
        List<Board> boards = boardService.getBoards(
            otherAdministrator, new ResourceFilter().setParentId(departmentAccepted.getId()).setState(REJECTED));
        assertThat(boards).hasSize(0);
    }

    @Test
    public void getBoards_successWhenUnprivilegedUser() {
        List<Scenarios> scenariosList =
            boardRepository.findAll()
                .stream()
                .map(serviceHelper::setUpUnprivilegedUsersForBoard)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                List<Board> departmentBoards =
                    boardService.getBoards(user, new ResourceFilter())
                        .stream()
                        .filter(this.departmentAcceptedBoards::contains)
                        .collect(toList());

                assertThat(departmentBoards).hasSize(2);
                verifyUnprivilegedUserBoards(departmentAccepted, departmentBoards, new Action[]{VIEW, EXTEND});

                List<Board> department2Boards =
                    boardService.getBoards(user, new ResourceFilter())
                        .stream()
                        .filter(this.departmentRejectedBoards::contains)
                        .collect(toList());

                assertThat(department2Boards).hasSize(2);
                verifyUnprivilegedUserBoards(departmentRejected, department2Boards, new Action[]{VIEW});
            }));
    }

    @Test
    public void getBoards_failureWhenUnprivilegedUserAndForbiddenState() {
        List<Scenarios> scenariosList =
            boardRepository.findAll()
                .stream()
                .map(serviceHelper::setUpUnprivilegedUsersForBoard)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                List<Board> boards =
                    boardService.getBoards(user, new ResourceFilter().setState(REJECTED))
                        .stream()
                        .filter(board -> departmentAcceptedBoards.contains(board) || departmentRejectedBoards.contains(board))
                        .collect(toList());

                assertThat(boards).hasSize(0);
            }));
    }

    @Test
    public void getBoards_failureWhenUnprivilegedUserAndForbiddenAction() {
        List<Scenarios> scenariosList =
            boardRepository.findAll()
                .stream()
                .map(serviceHelper::setUpUnprivilegedUsersForBoard)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                List<Board> departments =
                    boardService.getBoards(user, new ResourceFilter().setAction(EDIT))
                        .stream()
                        .filter(board -> departmentAcceptedBoards.contains(board) || departmentRejectedBoards.contains(board))
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
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
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            new Action[]{VIEW, EDIT, RESTORE},
            baseline);

        verifyBoard(
            boards.get(2),
            department,
            "Research Opportunities",
            ACCEPTED,
            ACCEPTED,
            expectedActions,
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

        verifyBoard(
            boards.get(1),
            department,
            "Research Opportunities",
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
