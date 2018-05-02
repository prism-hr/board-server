package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.repository.UserRepository;
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
import static hr.prism.board.enums.Scope.BOARD;
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
    private UserRepository userRepository;

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

    private User departmentAdministrator;

    private User department2Administrator;

    private Department department;

    private Department department2;

    private List<Board> departmentBoards;

    private List<Board> department2Boards;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
        departmentAdministrator = userRepository.findOne(1L);
        department2Administrator = userRepository.findOne(2L);

        department = serviceHelper.setUpDepartment(departmentAdministrator, 1L, "department");
        department2 = serviceHelper.setUpDepartment(departmentAdministrator, 1L, "department2");

        Board departmentBoard =
            serviceHelper.setUpBoard(departmentAdministrator, department.getId(), "Rejected Opportunities");
        resourceService.updateState(departmentBoard, REJECTED);
        departmentBoards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(department.getId()));

        Board department2Board =
            serviceHelper.setUpBoard(departmentAdministrator, department2.getId(), "Rejected Opportunities");
        resourceService.updateState(department2Board, REJECTED);
        department2Boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(department2.getId()));

        userRoleService.createUserRole(department2, department2Administrator, ADMINISTRATOR);
        reset(actionService, resourceService);
    }

    @After
    public void tearDown() {
        reset(actionService, resourceService);
    }

    @Test
    public void createBoard_success() {
        Board createdBoard = boardService.createBoard(departmentAdministrator, department.getId(),
            new BoardDTO().setName("board").setPostCategories(ImmutableList.of("category1", "category2")));

        Board selectedBoard = boardService.getById(departmentAdministrator, createdBoard.getId());

        String[] postCategories = new String[]{"category1", "category2"};
        Stream.of(createdBoard, selectedBoard).forEach(board ->
            verifyBoard(
                board,
                department,
                "board",
                ACCEPTED,
                ACCEPTED,
                "university/department/board",
                postCategories,
                new Action[]{VIEW, EDIT, EXTEND, REJECT},
                "D163 D163 S560 B630",
                baseline));

        verify(actionService, times(1))
            .executeAction(eq(departmentAdministrator), eq(department), eq(EXTEND), any(Execution.class));

        verify(resourceService, times(1))
            .checkUniqueName(BOARD, null, department, "board");

        verify(resourceService, times(1))
            .createHandle(createdBoard.getParent(), BOARD, createdBoard.getName());

        verify(resourceService, times(1))
            .updateCategories(createdBoard, POST, Stream.of(postCategories).collect(toList()));

        verify(resourceService, times(1)).createResourceRelation(department, createdBoard);

        verify(resourceService, times(1))
            .createResourceOperation(createdBoard, EXTEND, departmentAdministrator);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartments() {
        List<Board> boards = boardService.getBoards(departmentAdministrator, new ResourceFilter());

        assertThat(boards).hasSize(6);
        verifyAdministratorBoards(department,
            boards.stream().filter(board -> board.getParent().equals(department)).collect(toList()));

        verifyAdministratorBoards(department2,
            boards.stream().filter(board -> board.getParent().equals(department2)).collect(toList()));
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartment() {
        List<Board> boards = boardService.getBoards(department2Administrator, new ResourceFilter());
        assertThat(boards).hasSize(5);

        verifyUnprivilegedUserBoards(department,
            boards.stream().filter(board -> board.getParent().equals(department)).collect(toList()));

        verifyAdministratorBoards(department2,
            boards.stream().filter(board -> board.getParent().equals(department2)).collect(toList()));
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartmentAndNonPublicState() {
        List<Board> boards = boardService.getBoards(department2Administrator, new ResourceFilter().setState(REJECTED));
        assertThat(boards).hasSize(1);

        verifyBoard(
            boards.get(0),
            department2,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            "university/department2/rejected-opportunities",
            new String[]{"Employment", "Internship"},
            new Action[]{VIEW, EDIT, RESTORE},
            "D163 D163 S560 R223 O163",
            baseline);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartmentsAndDepartment() {
        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(department.getId()));
        assertThat(boards).hasSize(3);
        verifyAdministratorBoards(department, boards);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartmentsAndAction() {
        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setAction(RESTORE));
        assertThat(boards).hasSize(2);
        verifyRejectedBoards(boards);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartmentsAndSearchTerm() {
        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setSearchTerm("REJECTED"));
        assertThat(boards).hasSize(2);
        verifyRejectedBoards(boards);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartmentsAndSearchTermTypo() {
        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setSearchTerm("rIJECT"));
        assertThat(boards).hasSize(2);
        verifyRejectedBoards(boards);
    }

    @Test
    public void getBoards_failureWhenAdministratorOfMultipleDepartmentsAndSearchTerm() {
        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(boards).hasSize(0);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartmentAndDepartment() {
        List<Board> boards =
            boardService.getBoards(department2Administrator, new ResourceFilter().setParentId(department2.getId()));
        assertThat(boards).hasSize(3);
        verifyAdministratorBoards(department2, boards);
    }

    @Test
    public void getBoards_failureWhenAdministratorOfOneDepartmentAndWrongDepartmentAndNonPublicState() {
        List<Board> boards = boardService.getBoards(
            department2Administrator, new ResourceFilter().setParentId(department.getId()).setState(REJECTED));
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
                        .filter(this.departmentBoards::contains)
                        .collect(toList());

                assertThat(departmentBoards).hasSize(2);
                verifyUnprivilegedUserBoards(department, departmentBoards);

                List<Board> department2Boards =
                    boardService.getBoards(user, new ResourceFilter())
                        .stream()
                        .filter(this.department2Boards::contains)
                        .collect(toList());

                assertThat(department2Boards).hasSize(2);
                verifyUnprivilegedUserBoards(department2, department2Boards);
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
                        .filter(board -> departmentBoards.contains(board) || department2Boards.contains(board))
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
                        .filter(board -> departmentBoards.contains(board) || department2Boards.contains(board))
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyBoard(Board board, Department expectedDepartment, String expectedName, State expectedState,
                             State expectedPreviousState, String expectedHandle, String[] expectedPostCategories,
                             Action[] expectedActions, String expectedIndexData, LocalDateTime baseline) {
        serviceHelper.verifyIdentity(board, expectedDepartment, expectedName);

        assertEquals(expectedState, board.getState());
        assertEquals(expectedPreviousState, board.getPreviousState());
        assertEquals(expectedHandle, board.getHandle());

        assertThat(board.getPostCategoryStrings()).containsExactly(expectedPostCategories);
        serviceHelper.verifyActions(board, expectedActions);

        serviceHelper.verifyIndexDataAndQuarter(board, expectedIndexData);
        serviceHelper.verifyTimestamps(board, baseline);
    }

    private void verifyAdministratorBoards(Department department, List<Board> boards) {
        verifyBoard(
            boards.get(0),
            department,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            "university/" + department.getName() + "/career-opportunities",
            new String[]{"Employment", "Internship", "Volunteering"},
            new Action[]{VIEW, EDIT, EXTEND, REJECT},
            "D163 D163 S560 C660 O163",
            baseline);

        verifyBoard(
            boards.get(1),
            department,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            "university/" + department.getName() + "/rejected-opportunities",
            new String[]{"Employment", "Internship"},
            new Action[]{VIEW, EDIT, RESTORE},
            "D163 D163 S560 R223 O163",
            baseline);

        verifyBoard(
            boards.get(2),
            department,
            "Research Opportunities",
            ACCEPTED,
            ACCEPTED,
            "university/" + department.getName() + "/research-opportunities",
            new String[]{"MRes", "PhD", "Postdoc"},
            new Action[]{VIEW, EDIT, EXTEND, REJECT},
            "D163 D163 S560 R262 O163",
            baseline);
    }

    private void verifyUnprivilegedUserBoards(Department department, List<Board> boards) {
        verifyBoard(
            boards.get(0),
            department,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            "university/" + department.getName() + "/career-opportunities",
            new String[]{"Employment", "Internship", "Volunteering"},
            new Action[]{VIEW, EXTEND},
            "D163 D163 S560 C660 O163",
            baseline);

        verifyBoard(
            boards.get(1),
            department,
            "Research Opportunities",
            ACCEPTED,
            ACCEPTED,
            "university/" + department.getName() + "/research-opportunities",
            new String[]{"MRes", "PhD", "Postdoc"},
            new Action[]{VIEW, EXTEND},
            "D163 D163 S560 R262 O163",
            baseline);
    }

    private void verifyRejectedBoards(List<Board> boards) {
        verifyBoard(
            boards.get(0),
            department,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            "university/" + department.getName() + "/rejected-opportunities",
            new String[]{"Employment", "Internship"},
            new Action[]{VIEW, EDIT, RESTORE},
            "D163 D163 S560 R223 O163",
            baseline);

        verifyBoard(
            boards.get(1),
            department2,
            "Rejected Opportunities",
            REJECTED,
            ACCEPTED,
            "university/" + department2.getName() + "/rejected-opportunities",
            new String[]{"Employment", "Internship"},
            new Action[]{VIEW, EDIT, RESTORE},
            "D163 D163 S560 R223 O163",
            baseline);
    }

}
