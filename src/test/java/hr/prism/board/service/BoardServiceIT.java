package hr.prism.board.service;

import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.value.ResourceFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.State.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/boardService_setUp.sql")
@Sql(scripts = "classpath:data/boardService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class BoardServiceIT {

    private static final Logger LOGGER = getLogger(BoardServiceIT.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private BoardService boardService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private DataHelper dataHelper;

    private User departmentAdministrator;

    private User department2Administrator;

    private Department department;

    private Department department2;

    private List<Board> departmentBoards;

    private List<Board> department2Boards;

    @Before
    public void setUp() {
        departmentAdministrator = userRepository.findOne(1L);
        department2Administrator = userRepository.findOne(2L);

        department = dataHelper.setUpDepartment(departmentAdministrator, 1L, "department");
        department2 = dataHelper.setUpDepartment(departmentAdministrator, 1L, "department2");

        Board departmentBoard =
            dataHelper.setUpBoard(departmentAdministrator, department.getId(), "department-board");
        resourceService.updateState(departmentBoard, REJECTED);
        departmentBoards = boardService.getBoards(new ResourceFilter().setParentId(department.getId()));

        Board department2Board =
            dataHelper.setUpBoard(departmentAdministrator, department2.getId(), "department2-board");
        resourceService.updateState(department2Board, REJECTED);
        department2Boards = boardService.getBoards(new ResourceFilter().setParentId(department2.getId()));

        userRoleService.createUserRole(department2, department2Administrator, ADMINISTRATOR);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartments() {
        getContext().setAuthentication(new AuthenticationToken(departmentAdministrator));
        List<Board> boards = boardService.getBoards(new ResourceFilter());

        assertThat(boards).hasSize(6);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartment() {
        getContext().setAuthentication(new AuthenticationToken(department2Administrator));
        List<Board> boards = boardService.getBoards(new ResourceFilter());

        assertThat(boards).hasSize(5);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartmentFilterByNonPublicState() {
        getContext().setAuthentication(new AuthenticationToken(department2Administrator));
        List<Board> boards = boardService.getBoards(new ResourceFilter().setState(REJECTED));

        assertThat(boards).hasSize(1);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartmentsFilterByDepartment() {
        getContext().setAuthentication(new AuthenticationToken(departmentAdministrator));
        List<Board> boards = boardService.getBoards(new ResourceFilter().setParentId(department.getId()));

        assertThat(boards).hasSize(3);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartmentFilterByDepartment() {
        getContext().setAuthentication(new AuthenticationToken(department2Administrator));
        List<Board> boards = boardService.getBoards(new ResourceFilter().setParentId(department2.getId()));

        assertThat(boards).hasSize(3);
    }

    @Test
    public void getBoards_failureWhenAdministratorOfOneDepartmentFilterByWrongDepartmentAndNonPublicState() {
        getContext().setAuthentication(new AuthenticationToken(department2Administrator));
        List<Board> boards = boardService.getBoards(
            new ResourceFilter().setParentId(department.getId()).setState(REJECTED));

        assertThat(boards).hasSize(0);
    }

}
