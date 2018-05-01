package hr.prism.board.service;

import hr.prism.board.DbTestContext;
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
    private BoardService boardService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private ServiceDataHelper serviceDataHelper;

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

        department = serviceDataHelper.setUpDepartment(departmentAdministrator, 1L, "department");
        department2 = serviceDataHelper.setUpDepartment(departmentAdministrator, 1L, "department2");

        Board departmentBoard =
            serviceDataHelper.setUpBoard(departmentAdministrator, department.getId(), "department-board");
        resourceService.updateState(departmentBoard, REJECTED);
        departmentBoards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(department.getId()));

        Board department2Board =
            serviceDataHelper.setUpBoard(departmentAdministrator, department2.getId(), "department2-board");
        resourceService.updateState(department2Board, REJECTED);
        department2Boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(department2.getId()));

        userRoleService.createUserRole(department2, department2Administrator, ADMINISTRATOR);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartments() {
        List<Board> boards = boardService.getBoards(departmentAdministrator, new ResourceFilter());

        assertThat(boards).hasSize(6);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartment() {
        List<Board> boards = boardService.getBoards(department2Administrator, new ResourceFilter());

        assertThat(boards).hasSize(5);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartmentFilterByNonPublicState() {
        List<Board> boards = boardService.getBoards(department2Administrator, new ResourceFilter().setState(REJECTED));

        assertThat(boards).hasSize(1);
    }

    @Test
    public void getBoards_successWhenAdministratorOfMultipleDepartmentsFilterByDepartment() {
        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(department.getId()));

        assertThat(boards).hasSize(3);
    }

    @Test
    public void getBoards_successWhenAdministratorOfOneDepartmentFilterByDepartment() {
        List<Board> boards =
            boardService.getBoards(department2Administrator, new ResourceFilter().setParentId(department2.getId()));

        assertThat(boards).hasSize(3);
    }

    @Test
    public void getBoards_failureWhenAdministratorOfOneDepartmentFilterByWrongDepartmentAndNonPublicState() {
        List<Board> boards = boardService.getBoards(
            department2Administrator, new ResourceFilter().setParentId(department.getId()).setState(REJECTED));

        assertThat(boards).hasSize(0);
    }

}
