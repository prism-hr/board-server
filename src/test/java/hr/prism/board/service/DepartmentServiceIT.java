package hr.prism.board.service;

import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.value.ResourceFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.State.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/departmentService_setUp.sql")
@Sql(scripts = "classpath:data/departmentService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class DepartmentServiceIT {

    @Inject
    private UserRepository userRepository;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ResourceService resourceService;

    @Before
    public void setUp() {
        User user = userRepository.findOne(1L);
        getContext().setAuthentication(new AuthenticationToken(user));

        Stream.of(DRAFT, PENDING, ACCEPTED, REJECTED).forEach(state -> {
            Department department = departmentService.createDepartment(1L,
                new DepartmentDTO()
                    .setName("department " + state)
                    .setSummary("department " + state + " summary"));
            resourceService.updateState(department, state);
        });
    }

    @After
    public void tearDown() {
        getContext().setAuthentication(null);
    }

    @Test
    public void getDepartments_successWhenAdministrator() {
        List<Department> departments = departmentService.getDepartments(new ResourceFilter());
        assertThat(departments).hasSize(4);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndState() {
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setState(ACCEPTED));
        assertThat(departments).hasSize(1);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndAction() {
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setAction(EXTEND));
        assertThat(departments).hasSize(3);
    }

}
