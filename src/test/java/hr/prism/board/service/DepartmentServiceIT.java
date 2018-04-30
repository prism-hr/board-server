package hr.prism.board.service;

import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.MemberCategory;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.State.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/departmentService_setUp.sql")
@Sql(scripts = "classpath:data/departmentService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class DepartmentServiceIT {

    private static final Logger LOGGER = getLogger(DepartmentServiceIT.class);

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

    private User departmentAdministrator;

    private List<Department> departments;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
        departmentAdministrator = userRepository.findOne(1L);
    }

    @Test
    public void createDepartment_success() {
        getContext().setAuthentication(new AuthenticationToken(departmentAdministrator));
        Department department = departmentService.createDepartment(1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary"));

        verifyDepartment(
            department,
            "department",
            "department summary",
            DRAFT,
            DRAFT,
            "university/department",
            MemberCategory.values(),
            VIEW, EDIT, EXTEND, SUBSCRIBE);

        Department persistedDepartment = departmentService.getById(department.getId());

        verifyDepartment(
            persistedDepartment,
            "department",
            "department summary",
            DRAFT,
            DRAFT,
            "university/department",
            MemberCategory.values(),
            VIEW, EDIT, EXTEND, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministrator() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter());

        assertThat(departments).hasSize(4);
        verifyDepartment(departments.get(0), ACCEPTED, VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
        verifyDepartment(departments.get(1), DRAFT, VIEW, EDIT, EXTEND, SUBSCRIBE);
        verifyDepartment(departments.get(2), PENDING, VIEW, EDIT, EXTEND, SUBSCRIBE);
        verifyDepartment(departments.get(3), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndState() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setState(ACCEPTED));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), ACCEPTED, VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndAction() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setAction(EXTEND));

        assertThat(departments).hasSize(3);
        verifyDepartment(departments.get(0), ACCEPTED, VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
        verifyDepartment(departments.get(1), DRAFT, VIEW, EDIT, EXTEND, SUBSCRIBE);
        verifyDepartment(departments.get(2), PENDING, VIEW, EDIT, EXTEND, SUBSCRIBE);
    }


    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("REJECTED"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermCaseInsensitiveMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("rejected"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermPartialMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("REJECT"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermPartialCaseInsensitiveMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("reject"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypoMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("RIJECT"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_failureWhenAdministratorAndSearchTermNoMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("xyz"));

        assertThat(departments).hasSize(0);
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypoCaseInsensitiveMatch() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(new ResourceFilter().setSearchTerm("riject"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), REJECTED, VIEW, EDIT, SUBSCRIBE);
    }

    @Test
    public void getDepartments_successWhenUnprivilegedUser() {
        setUpDepartments();
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
                        .filter(this.departments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(3);
                verifyDepartment(departments.get(0), ACCEPTED, VIEW);
                verifyDepartment(departments.get(1), DRAFT, VIEW);
                verifyDepartment(departments.get(2), PENDING, VIEW);
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenState() {
        setUpDepartments();
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
                        .filter(this.departments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenAction() {
        setUpDepartments();
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
                        .filter(this.departments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    private void setUpDepartments() {
        departments = new ArrayList<>();
        Stream.of(DRAFT, PENDING, ACCEPTED, REJECTED).forEach(state -> {
            Department department =
                dataHelper.setUpDepartment(departmentAdministrator, 1L, "department " + state);
            resourceService.updateState(department, state);
            departments.add(department);
        });
    }

    private void verifyDepartment(Department department, State expectedState, Action... expectedActions) {
        verifyDepartment(
            department,
            "department " + expectedState,
            "department " + expectedState + " summary",
            expectedState,
            DRAFT,
            "university/department-" + expectedState.name().toLowerCase(),
            MemberCategory.values(),
            expectedActions);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyDepartment(Department department, String expectedName, String expectedSummary,
                                  State expectedState, State expectedPreviousState, String expectedHandle,
                                  MemberCategory[] expectedMemberCategories, Action... expectedActions) {
        University university = new University();
        university.setId(1L);

        assertNotNull(department.getId());
        assertEquals(university, department.getParent());

        assertEquals(expectedName, department.getName());
        assertEquals(expectedSummary, department.getSummary());

        assertEquals(expectedState, department.getState());
        assertEquals(expectedPreviousState, department.getPreviousState());
        assertEquals(expectedHandle, department.getHandle());

        assertNull(department.getDocumentLogo());
        assertNull(department.getLocation());

        assertThat(
            department.getMemberCategories()
                .stream()
                .map(ResourceCategory::getName)
                .collect(toList()))
            .containsExactly(
                Stream.of(expectedMemberCategories)
                    .map(MemberCategory::name)
                    .toArray(String[]::new));

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
