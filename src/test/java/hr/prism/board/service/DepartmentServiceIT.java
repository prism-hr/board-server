package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.State;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.service.ServiceHelper.Scenarios;
import hr.prism.board.value.ResourceFilter;
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
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.ResourceTask.DEPARTMENT_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.State.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
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
    private DepartmentRepository departmentRepository;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ServiceHelper serviceHelper;

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

    private LocalDateTime baseline;

    private User administrator;

    private User otherAdministrator;

    private University university;

    private List<Department> departments;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();

        administrator = serviceHelper.setUpUser();
        otherAdministrator = serviceHelper.setUpUser();

        university = serviceHelper.setUpUniversity("university");

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
        reset(universityService, boardService, resourceService, resourceTaskService, userRoleService, documentService);
    }

    @After
    public void tearDown() {
        reset(universityService, boardService, resourceService, resourceTaskService, userRoleService, documentService);
    }

    @Test
    public void createDepartment_successWhenDefaultData() {
        Department createdDepartment =
            serviceHelper.setUpDepartment(administrator, university, "department");
        Department selectedDepartment = departmentService.getById(administrator, createdDepartment.getId());

        MemberCategory[] memberCategories = MemberCategory.values();
        Stream.of(createdDepartment, selectedDepartment).forEach(department ->
            verifyDepartment(
                department,
                university,
                "department",
                "department summary",
                DRAFT,
                DRAFT,
                university.getDocumentLogo(),
                new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
                baseline));

        verifyCreateDepartmentInvocations(createdDepartment, memberCategories);
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

        MemberCategory[] memberCategories = new MemberCategory[]{UNDERGRADUATE_STUDENT, MASTER_STUDENT};
        Stream.of(createdDepartment, selectedDepartment).forEach(department ->
            verifyDepartment(
                createdDepartment,
                university,
                "department",
                "department summary",
                DRAFT,
                DRAFT,
                expectedDocumentLogo,
                new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
                baseline));

        verifyCreateDepartmentInvocations(createdDepartment, memberCategories);
        verify(documentService, times(1)).getOrCreateDocument(documentLogoDTO);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void getDepartments_successWhenAdministrator() {
        List<Department> departments = departmentService.getDepartments(administrator, new ResourceFilter());
        assertThat(departments).hasSize(4);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});

        verifyDepartment(
            departments.get(1),
            DRAFT,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(
            departments.get(2),
            PENDING,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(
            departments.get(3),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void getDepartments_successWhenOtherAdministrator() {
        List<Department> departments = departmentService.getDepartments(otherAdministrator, new ResourceFilter());
        assertThat(departments).hasSize(4);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW});

        verifyDepartment(
            departments.get(1),
            DRAFT,
            new Action[]{VIEW});

        verifyDepartment(
            departments.get(2),
            PENDING,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(
            departments.get(3),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenAdministratorAndState() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setState(ACCEPTED));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenAdministratorAndAction() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setAction(EXTEND));
        assertThat(departments).hasSize(3);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});

        verifyDepartment(
            departments.get(1),
            DRAFT,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(
            departments.get(2),
            PENDING,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }


    @Test
    public void getDepartments_successWhenAdministratorAndSearchTerm() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setSearchTerm("REJECTED"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypo() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setSearchTerm("rIJECT"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_failureWhenAdministratorAndSearchTerm() {
        List<Department> departments =
            departmentService.getDepartments(administrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(departments).hasSize(0);
    }

    @Test
    public void getDepartments_successWhenUnprivilegedUser() {
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(serviceHelper::setUpUnprivilegedUsersForDepartment)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                List<Department> departments =
                    departmentService.getDepartments(user, new ResourceFilter())
                        .stream()
                        .filter(this.departments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(3);

                verifyDepartment(
                    departments.get(0),
                    ACCEPTED,
                    new Action[]{VIEW});

                verifyDepartment(
                    departments.get(1),
                    DRAFT,
                    new Action[]{VIEW});

                verifyDepartment(
                    departments.get(2),
                    PENDING,
                    new Action[]{VIEW});
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenState() {
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(serviceHelper::setUpUnprivilegedUsersForDepartment)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                List<Department> departments =
                    departmentService.getDepartments(user, new ResourceFilter().setState(REJECTED))
                        .stream()
                        .filter(this.departments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenAction() {
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(serviceHelper::setUpUnprivilegedUsersForDepartment)
                .collect(toList());

        scenariosList.forEach(scenarios ->
            scenarios.forEach(scenario -> {
                User user = scenario.user;
                LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");

                List<Department> departments =
                    departmentService.getDepartments(user, new ResourceFilter().setAction(EXTEND))
                        .stream()
                        .filter(this.departments::contains)
                        .collect(toList());

                assertThat(departments).hasSize(0);
            }));
    }

    private void verifyDepartment(Department department, State expectedState, Action[] expectedActions) {
        verifyDepartment(
            department,
            university,
            "department " + expectedState,
            "department " + expectedState + " summary",
            expectedState,
            DRAFT,
            university.getDocumentLogo(),
            expectedActions,
            baseline);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyDepartment(Department department, University expectedUniversity, String expectedName,
                                  String expectedSummary, State expectedState, State expectedPreviousState,
                                  Document expectedDocumentLogo, Action[] expectedActions, LocalDateTime baseline) {
        serviceHelper.verifyIdentity(department, expectedUniversity, expectedName);
        assertEquals(expectedSummary, department.getSummary());
        assertEquals(expectedDocumentLogo, department.getDocumentLogo());

        assertEquals(expectedState, department.getState());
        assertEquals(expectedPreviousState, department.getPreviousState());
        serviceHelper.verifyActions(department, expectedActions);

        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThanOrEqualTo(baseline);
        serviceHelper.verifyTimestamps(department, baseline);
    }

    private void verifyCreateDepartmentInvocations(Department department, MemberCategory[] memberCategories) {
        verify(universityService, times(1)).getById(university.getId());
        verify(resourceService, times(1)).setName(department, "department");
        verify(resourceService, times(1)).setHandle(department);

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
