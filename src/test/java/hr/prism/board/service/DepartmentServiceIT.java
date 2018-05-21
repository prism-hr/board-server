package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
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
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.ResourceTask.DEPARTMENT_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/departmentService_setUp.sql")
@Sql(scripts = "classpath:data/departmentService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class DepartmentServiceIT {

    private static final Logger LOGGER = getLogger(DepartmentServiceIT.class);

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserService userService;

    @Inject
    private ServiceHelper serviceHelper;

    @SpyBean
    private ResourceDAO resourceDAO;

    @SpyBean
    private UniversityService universityService;

    @SpyBean
    private BoardService boardService;

    @SpyBean
    private ResourceService resourceService;

    @SpyBean
    private ResourceTaskService resourceTaskService;

    @SpyBean
    private UserRoleService userRoleService;

    @SpyBean
    private DocumentService documentService;

    @SpyBean
    private ActionService actionService;

    private LocalDateTime baseline;

    private User administrator;

    private User otherAdministrator;

    private University university;

    private List<Department> departments;

    @Before
    public void setUp() {
        reset(universityService, boardService, resourceService, resourceTaskService, userRoleService, documentService);
    }

    @After
    public void tearDown() {
        reset(universityService, boardService, resourceService, resourceTaskService, userRoleService, documentService);
    }

    @Test
    public void getById_successWhenAcceptedAndDepartmentAdministrator() {
        User administrator = userService.getByEmail("department-administrator@prism.hr");
        University university = (University) resourceService.getByHandle("university");
        Department department = departmentService.getById(administrator, 2L);

        serviceHelper.verifyIdentity(department, university, "department-accepted");
        serviceHelper.verifyActions(department, new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});
        verifyInvocations(administrator, 2L, department);
    }

    @Test
    public void getById_success() {
        Department createdDepartment = serviceHelper.setUpDepartment(administrator, university, "department");
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(createdDepartment, AUTHOR, Role.MEMBER));

        Long createdDepartmentId = createdDepartment.getId();
        Consumer<Scenario> unprivilegedScenario = scenario -> {
            User user = scenario.user;
            Department selectedDepartment = departmentService.getById(user, createdDepartmentId);
            assertEquals(createdDepartment, selectedDepartment);

            verifyDepartment(selectedDepartment, "department", new Action[]{VIEW});
            verifyInvocations(user, createdDepartmentId, createdDepartment);
        };

        verifyGetById(createdDepartment, DRAFT, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE}, unprivilegedScenario);

        verifyGetById(createdDepartment, PENDING, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE}, unprivilegedScenario);

        verifyGetById(createdDepartment, ACCEPTED, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE}, unprivilegedScenario);

        verifyGetById(createdDepartment, REJECTED, scenarios,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            scenario -> {
                User user = scenario.user;
                assertThatThrownBy(() -> departmentService.getById(user, createdDepartmentId))
                    .isExactlyInstanceOf(BoardForbiddenException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

                verifyInvocations(user, createdDepartmentId, createdDepartment);
            });
    }

    @Test
    public void getByHandle_success() {
        Department createdDepartment = serviceHelper.setUpDepartment(administrator, university, "department");
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(createdDepartment, AUTHOR, Role.MEMBER));

        Consumer<Scenario> unprivilegedScenario = scenario -> {
            User user = scenario.user;
            Department selectedDepartment = departmentService.getByHandle(user, "university/department");
            assertEquals(createdDepartment, selectedDepartment);

            verifyDepartment(selectedDepartment, "department", new Action[]{VIEW});
            verifyInvocations(user, "university/department", selectedDepartment);
        };

        verifyGetByHandle(createdDepartment, DRAFT, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE}, unprivilegedScenario);

        verifyGetByHandle(createdDepartment, PENDING, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE}, unprivilegedScenario);

        verifyGetByHandle(createdDepartment, ACCEPTED, scenarios,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE}, unprivilegedScenario);

        verifyGetByHandle(createdDepartment, REJECTED, scenarios,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            scenario -> {
                User user = scenario.user;
                assertThatThrownBy(() -> departmentService.getByHandle(user, "university/department"))
                    .isExactlyInstanceOf(BoardForbiddenException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

                verifyInvocations(user, "university/department", createdDepartment);
            });
    }

    @Test
    public void createDepartment_successWhenDefaultData() {
        Department createdDepartment =
            departmentService.createDepartment(administrator, university.getId(),
                new DepartmentDTO()
                    .setName("department")
                    .setSummary("department summary"));

        Department selectedDepartment = departmentService.getById(administrator, createdDepartment.getId());
        Stream.of(createdDepartment, selectedDepartment).forEach(department ->
            verifyDepartment(department, new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
                university.getDocumentLogo(), Stream.of(MemberCategory.values()).collect(toList())));

        verifyInvocations(createdDepartment, MemberCategory.values());
    }

    @Test
    public void createDepartment_successWhenCustomData() {
        DocumentDTO documentLogoDTO =
            new DocumentDTO()
                .setCloudinaryId("new cloudinary id")
                .setCloudinaryUrl("new cloudinary url")
                .setFileName("new file name");

        Department createdDepartment = departmentService.createDepartment(administrator, university.getId(),
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary")
                .setDocumentLogo(documentLogoDTO)
                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT)));

        Department selectedDepartment = departmentService.getById(administrator, createdDepartment.getId());

        Document expectedDocumentLogo = new Document();
        expectedDocumentLogo.setCloudinaryId("new cloudinary id");

        Stream.of(createdDepartment, selectedDepartment).forEach(department ->
            verifyDepartment(department, new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
                expectedDocumentLogo, ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT)));

        verifyInvocations(createdDepartment,
            new MemberCategory[]{UNDERGRADUATE_STUDENT, MASTER_STUDENT});
        verify(documentService, times(1)).getOrCreateDocument(documentLogoDTO);
    }

    @Test
    public void getDepartments_success() {
        setUpDepartments();
        getDepartments_successWhenAdministrator();
        getDepartments_successWhenAdministratorAndState();
        getDepartments_successWhenAdministratorAndAction();
        getDepartments_successWhenAdministratorAndSearchTerm();
        getDepartments_successWhenAdministratorAndSearchTermTypo();
        getDepartments_successWhenAdministratorAndSearchTermWithoutResults();
        getDepartments_successWhenOtherAdministrator();
        getDepartments_successWhenUnprivileged();
    }

    private void setUpDepartments() {
        otherAdministrator = serviceHelper.setUpUser();

        Department departmentDraft =
            serviceHelper.setUpDepartment(administrator, university, "department DRAFT", DRAFT);

        Department departmentPending =
            serviceHelper.setUpDepartment(administrator, university, "department PENDING", PENDING);
        userRoleService.createUserRole(departmentPending, otherAdministrator, ADMINISTRATOR);

        Department departmentAccepted =
            serviceHelper.setUpDepartment(administrator, university, "department ACCEPTED", ACCEPTED);

        Department departmentRejected =
            serviceHelper.setUpDepartment(administrator, university, "department REJECTED", REJECTED);
        userRoleService.createUserRole(departmentRejected, otherAdministrator, ADMINISTRATOR);

        departments = ImmutableList.of(departmentDraft, departmentPending, departmentAccepted, departmentRejected);
    }

    @SuppressWarnings("Duplicates")
    private void getDepartments_successWhenAdministrator() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter());

        assertThat(departments).hasSize(4);
        verifyDepartment(departments.get(0),
            "department ACCEPTED", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});

        verifyDepartment(departments.get(1),
            "department DRAFT", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(2),
            "department PENDING", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(3), "department REJECTED", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    private void getDepartments_successWhenAdministratorAndState() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setState(ACCEPTED));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0),
            "department ACCEPTED", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});
    }

    private void getDepartments_successWhenAdministratorAndAction() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setAction(EXTEND));

        assertThat(departments).hasSize(3);
        verifyDepartment(departments.get(0),
            "department ACCEPTED", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});

        verifyDepartment(departments.get(1),
            "department DRAFT", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(2),
            "department PENDING", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }

    private void getDepartments_successWhenAdministratorAndSearchTerm() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setSearchTerm("REJECTED"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), "department REJECTED", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    private void getDepartments_successWhenAdministratorAndSearchTermTypo() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setSearchTerm("rIJECT"));

        assertThat(departments).hasSize(1);
        verifyDepartment(departments.get(0), "department REJECTED", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    private void getDepartments_successWhenAdministratorAndSearchTermWithoutResults() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(departments).hasSize(0);
    }

    @SuppressWarnings("Duplicates")
    private void getDepartments_successWhenOtherAdministrator() {
        List<Department> departments =
            departmentService.getDepartments(otherAdministrator, new ResourceFilter());

        assertThat(departments).hasSize(4);
        verifyDepartment(departments.get(0), "department ACCEPTED", new Action[]{VIEW});
        verifyDepartment(departments.get(1), "department DRAFT", new Action[]{VIEW});
        verifyDepartment(departments.get(2),
            "department PENDING", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(3), "department REJECTED", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    private void getDepartments_successWhenUnprivileged() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university);
        departments.forEach(department -> {
            Scenarios departmentScenarios = serviceHelper.setUpUnprivilegedUsers(department, AUTHOR, Role.MEMBER);
            scenarios.scenarios(departmentScenarios);
        });

        scenarios.forEach(scenario -> {
            User user = scenario.user;
            LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

            List<Department> departments =
                departmentService.getDepartments(user, new ResourceFilter())
                    .stream()
                    .filter(this.departments::contains)
                    .collect(toList());

            assertThat(departments).hasSize(3);
            verifyDepartment(departments.get(0), "department ACCEPTED", new Action[]{VIEW});
            verifyDepartment(departments.get(1), "department DRAFT", new Action[]{VIEW});
            verifyDepartment(departments.get(2), "department PENDING", new Action[]{VIEW});
        });
    }

    private void verifyGetById(Department department, State state, Scenarios scenarios,
                               Action[] expectedAdministratorActions, Consumer<Scenario> unprivilegedScenario) {
        resourceService.updateState(department, state);

        Long createdDepartmentId = department.getId();
        Department selectedDepartment = departmentService.getById(administrator, createdDepartmentId);
        assertEquals(department, selectedDepartment);

        verifyDepartment(selectedDepartment, "department", expectedAdministratorActions);
        verifyInvocations(administrator, createdDepartmentId, department);
        scenarios.forEach(unprivilegedScenario);
    }

    private void verifyInvocations(User user, Long departmentId, Department department) {
        verify(resourceService, times(1))
            .getResource(user, DEPARTMENT, departmentId);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(department), eq(VIEW), any(Execution.class));
    }

    private void verifyGetByHandle(Department createdDepartment, State state, Scenarios scenarios,
                                   Action[] expectedAdministratorActions, Consumer<Scenario> unprivilegedScenario) {
        reset(resourceService, actionService);
        resourceService.updateState(createdDepartment, state);

        Department selectedDepartment = departmentService.getByHandle(administrator, "university/department");
        assertEquals(createdDepartment, selectedDepartment);

        verifyDepartment(selectedDepartment, "department", expectedAdministratorActions);
        verifyInvocations(administrator, "university/department", selectedDepartment);
        scenarios.forEach(unprivilegedScenario);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyInvocations(User user, String departmentHandle, Department department) {
        verify(resourceService, atLeastOnce())
            .getResource(user, DEPARTMENT, departmentHandle);

        verify(actionService, atLeastOnce())
            .executeAction(eq(user), eq(department), eq(VIEW), any(Execution.class));
    }

    private void verifyDepartment(Department department, String expectedName, Action[] expectedActions) {

    }

    @SuppressWarnings("SameParameterValue")
    private void verifyDepartment(Department department, University university, String expectedName,
                                  Action[] expectedActions) {
        serviceHelper.verifyIdentity(department, university, expectedName);
        serviceHelper.verifyActions(department, expectedActions);

        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThanOrEqualTo(baseline);
        serviceHelper.verifyTimestamps(department, baseline);
    }

    private void verifyDepartment(Department department, Action[] expectedActions, Document expectedDocumentLogo,
                                  List<MemberCategory> expectedMemberCategories) {
        verifyDepartment(department, "department", expectedActions);
        assertEquals("department summary", department.getSummary());
        assertEquals(expectedDocumentLogo, department.getDocumentLogo());
        assertEquals("university/department", department.getHandle());
        assertEquals(DRAFT, department.getState());
        assertEquals(DRAFT, department.getPreviousState());
        assertEquals(toStrings(expectedMemberCategories), department.getMemberCategoryStrings());
    }

    private void verifyInvocations(Department department, MemberCategory[] memberCategories) {
        verify(universityService, times(1)).getById(university.getId());
        verify(resourceDAO).checkUniqueName(DEPARTMENT, null, university, "department");
        verify(resourceService, times(1)).createHandle(department);

        verify(resourceService, times(1))
            .updateCategories(department, MEMBER, toStrings(asList(memberCategories)));

        verify(resourceService, times(1)).createResourceRelation(university, department);
        verify(resourceService, times(1)).setIndexDataAndQuarter(department);

        verify(resourceService, times(1))
            .createResourceOperation(department, EXTEND, administrator);

        verify(userRoleService, times(1))
            .createUserRole(department, administrator, ADMINISTRATOR);

        Long departmentId = department.getId();
        verify(boardService).createBoard(administrator, departmentId,
            new BoardDTO()
                .setName("Career Opportunities")
                .setPostCategories(ImmutableList.of("Employment", "Internship", "Volunteering")));

        verify(boardService).createBoard(administrator, departmentId,
            new BoardDTO()
                .setName("Research Opportunities")
                .setPostCategories(ImmutableList.of("MRes", "PhD", "Postdoc")));

        verify(resourceTaskService, times(1))
            .createForNewResource(departmentId, administrator.getId(), DEPARTMENT_TASKS);
    }

}
