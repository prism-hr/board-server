package hr.prism.board.service;

import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.service.DataHelper.Scenarios;
import hr.prism.board.value.ResourceFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.State.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/departmentService_setUp.sql")
@Sql(scripts = "classpath:data/departmentService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class DepartmentServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentServiceIT.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DataHelper dataHelper;

    private LocalDateTime baseline;

    private List<Department> setUpDepartments;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
        User user = userRepository.findOne(1L);

        setUpDepartments = new ArrayList<>();
        Stream.of(DRAFT, PENDING, ACCEPTED, REJECTED).forEach(state -> {
            Department department = dataHelper.setUpDepartment(user, 1L, "department " + state);
            resourceService.updateState(department, state);
            setUpDepartments.add(department);
        });

        getContext().setAuthentication(null);
    }

    @Test
    public void getDepartments_successWhenAdministrator() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter());

        assertThat(departments).hasSize(4);
        verifyDepartment(departments.get(0), ACCEPTED, VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
        verifyDepartment(departments.get(1), DRAFT, VIEW, EDIT, EXTEND, SUBSCRIBE);
        verifyDepartment(departments.get(2), PENDING, VIEW, EDIT, EXTEND, SUBSCRIBE);
        verifyDepartment(departments.get(3), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndState() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setState(ACCEPTED));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), ACCEPTED, VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndAction() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setAction(EXTEND));

        assertThat(departments).hasSize(3);
        verifyDepartment(departments.get(0), ACCEPTED, VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
        verifyDepartment(departments.get(1), DRAFT, VIEW, EDIT, EXTEND, SUBSCRIBE);
        verifyDepartment(departments.get(2), PENDING, VIEW, EDIT, EXTEND, SUBSCRIBE);
    }


    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("REJECTED"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermCaseInsensitiveMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("rejected"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermPartialMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("REJECT"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermPartialCaseInsensitiveMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("reject"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypoMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("RIJECT"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_failureWhenAdministratorAndSearchTermNoMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("xyz"));

        assertThat(departments).hasSize(0);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypoCaseInsensitiveMatch() {
        authenticateAsDepartmentAdministrator();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("riject"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenUnprivilegedUser() {
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(dataHelper::setUpUnprivilegedUsersForDepartment)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                getContext().setAuthentication(new AuthenticationToken(user));
                List<Department> departments =
                    departmentService.getDepartments(new ResourceFilter())
                        .stream()
                        .filter(setUpDepartments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(3);
                verifyDepartment(departments.get(0), ACCEPTED, VIEW);
                verifyDepartment(departments.get(1), DRAFT, VIEW);
                verifyDepartment(departments.get(2), PENDING, VIEW);
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenState() {
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(dataHelper::setUpUnprivilegedUsersForDepartment)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                getContext().setAuthentication(new AuthenticationToken(user));
                List<Department> departments =
                    departmentService.getDepartments(new ResourceFilter().setState(REJECTED))
                        .stream()
                        .filter(setUpDepartments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenAction() {
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(dataHelper::setUpUnprivilegedUsersForDepartment)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                getContext().setAuthentication(new AuthenticationToken(user));
                List<Department> departments =
                    departmentService.getDepartments(new ResourceFilter().setAction(EXTEND))
                        .stream()
                        .filter(setUpDepartments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    private void authenticateAsDepartmentAdministrator() {
        User user = userRepository.findOne(1L);
        getContext().setAuthentication(new AuthenticationToken(user));
    }

    private void verifyDepartment(Department department, State expectedState, Action... expectedActions) {
        University university = new University();
        university.setId(1L);

        assertNotNull(department.getId());
        assertEquals(university, department.getParent());

        assertEquals("department " + expectedState, department.getName());
        assertEquals("department " + expectedState + " summary", department.getSummary());

        assertEquals(expectedState, department.getState());
        assertEquals(DRAFT, department.getPreviousState());
        assertEquals("university/department-" + expectedState.name().toLowerCase(), department.getHandle());

        assertNull(department.getDocumentLogo());
        assertNull(department.getLocation());

        assertThat(
            department.getMemberCategories()
                .stream()
                .map(ResourceCategory::getName)
                .collect(toList()))
            .containsExactly(
                UNDERGRADUATE_STUDENT.name(), MASTER_STUDENT.name(), RESEARCH_STUDENT.name(), RESEARCH_STAFF.name());

        assertThat(
            department.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(expectedActions);

        assertNotNull(department.getIndexData());
        assertNotNull(department.getQuarter());

        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThan(baseline);
        assertThat(department.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(department.getUpdatedTimestamp()).isGreaterThan(baseline);
    }

}
