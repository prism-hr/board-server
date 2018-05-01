package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.State;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.repository.DocumentRepository;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.service.ServiceDataHelper.Scenarios;
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
import static hr.prism.board.enums.MemberCategory.MASTER_STUDENT;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.State.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
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
    private DocumentRepository documentRepository;

    @Inject
    private UniversityService universityService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ServiceDataHelper serviceDataHelper;

    @Inject
    private ServiceVerificationHelper serviceVerificationHelper;

    private LocalDateTime baseline;

    private User departmentAdministrator;

    private University university;

    private Document documentLogo;

    private List<Department> departments;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
        departmentAdministrator = userRepository.findOne(1L);
        university = universityService.getById(1L);
        documentLogo = documentRepository.findOne(1L);
    }

    @Test
    public void createDepartment_successWhenDefaultData() {
        Department department = departmentService.createDepartment(departmentAdministrator, 1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary"));

        serviceVerificationHelper.verifyDepartment(
            department,
            university,
            "department",
            "department summary",
            DRAFT,
            DRAFT,
            "university/department",
            documentLogo,
            MemberCategory.values(),
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 D163 S560",
            baseline);

        Long departmentId = department.getId();
        Department savedDepartment = departmentService.getById(departmentAdministrator, departmentId);

        serviceVerificationHelper.verifyDepartment(
            savedDepartment,
            university,
            "department",
            "department summary",
            DRAFT,
            DRAFT,
            "university/department",
            documentLogo,
            MemberCategory.values(),
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 D163 S560",
            baseline);

        List<Board> boards =
            boardService.getBoards(departmentAdministrator, new ResourceFilter().setParentId(departmentId));
        assertThat(boards).hasSize(2);

        serviceVerificationHelper.verifyBoard(
            boards.get(0),
            department,
            "Career Opportunities",
            ACCEPTED,
            ACCEPTED,
            "university/department/career-opportunities",
            new String[]{"Employment", "Internship", "Volunteering"},
            new Action[]{VIEW, EDIT, EXTEND, REJECT},
            "D163 D163 S560 C660 O163",
            baseline);

        serviceVerificationHelper.verifyBoard(
            boards.get(1),
            department,
            "Research Opportunities",
            ACCEPTED,
            ACCEPTED,
            "university/department/research-opportunities",
            new String[]{"MRes", "PhD", "Postdoc"},
            new Action[]{VIEW, EDIT, EXTEND, REJECT},
            "D163 D163 S560 R262 O163",
            baseline);
    }

    @Test
    public void createDepartment_successWhenCustomData() {
        Department department = departmentService.createDepartment(departmentAdministrator, 1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary")
                .setDocumentLogo(
                    new DocumentDTO()
                        .setCloudinaryId("new cloudinary id")
                        .setCloudinaryUrl("new cloudinary url")
                        .setFileName("new file name"))
                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT)));

        Document expectedDocumentLogo = new Document();
        expectedDocumentLogo.setCloudinaryId("new cloudinary id");

        serviceVerificationHelper.verifyDepartment(
            department,
            university,
            "department",
            "department summary",
            DRAFT,
            DRAFT,
            "university/department",
            expectedDocumentLogo,
            new MemberCategory[]{UNDERGRADUATE_STUDENT, MASTER_STUDENT},
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 D163 S560",
            baseline);

        Long departmentId = department.getId();
        Department savedDepartment = departmentService.getById(departmentAdministrator, departmentId);

        serviceVerificationHelper.verifyDepartment(
            savedDepartment,
            university,
            "department",
            "department summary",
            DRAFT,
            DRAFT,
            "university/department",
            expectedDocumentLogo,
            new MemberCategory[]{UNDERGRADUATE_STUDENT, MASTER_STUDENT},
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 D163 S560",
            baseline);
    }

    @Test
    public void getDepartments_successWhenAdministrator() {
        setUpDepartments();
        List<Department> departments = departmentService.getDepartments(departmentAdministrator, new ResourceFilter());
        assertThat(departments).hasSize(4);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE},
            "D163 A213 D163 A213 S560");

        verifyDepartment(
            departments.get(1),
            DRAFT,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 D613 D163 D613 S560");

        verifyDepartment(
            departments.get(2),
            PENDING,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 P535 D163 P535 S560");

        verifyDepartment(
            departments.get(3),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndState() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setState(ACCEPTED));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE},
            "D163 A213 D163 A213 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndAction() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setAction(EXTEND));
        assertThat(departments).hasSize(3);

        verifyDepartment(
            departments.get(0),
            ACCEPTED,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE},
            "D163 A213 D163 A213 S560");

        verifyDepartment(
            departments.get(1),
            DRAFT,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 D613 D163 D613 S560");

        verifyDepartment(
            departments.get(2),
            PENDING,
            new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE},
            "D163 P535 D163 P535 S560");
    }


    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("REJECTED"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermCaseInsensitiveMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("rejected"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermPartialMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("REJECT"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermPartialCaseInsensitiveMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("reject"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypoMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("RIJECT"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_successWhenAdministratorAndSearchTermTypoCaseInsensitiveMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("riject"));
        assertThat(departments).hasSize(1);

        verifyDepartment(
            departments.get(0),
            REJECTED,
            new Action[]{VIEW, EDIT, SUBSCRIBE},
            "D163 R223 D163 R223 S560");
    }

    @Test
    public void getDepartments_failureWhenAdministratorAndSearchTermNoMatch() {
        setUpDepartments();
        List<Department> departments =
            departmentService.getDepartments(departmentAdministrator, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(departments).hasSize(0);
    }

    @Test
    public void getDepartments_successWhenUnprivilegedUser() {
        setUpDepartments();
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(serviceDataHelper::setUpUnprivilegedUsersForDepartment)
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
                    new Action[]{VIEW},
                    "D163 A213 D163 A213 S560");

                verifyDepartment(
                    departments.get(1),
                    DRAFT,
                    new Action[]{VIEW},
                    "D163 D613 D163 D613 S560");

                verifyDepartment(
                    departments.get(2),
                    PENDING,
                    new Action[]{VIEW},
                    "D163 P535 D163 P535 S560");
            }));
    }

    @Test
    public void getDepartments_failureWhenUnprivilegedUserAndForbiddenState() {
        setUpDepartments();
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(serviceDataHelper::setUpUnprivilegedUsersForDepartment)
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
        setUpDepartments();
        List<Scenarios> scenariosList =
            departmentRepository.findAll()
                .stream()
                .map(serviceDataHelper::setUpUnprivilegedUsersForDepartment)
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

    private void setUpDepartments() {
        departments = new ArrayList<>();
        Stream.of(DRAFT, PENDING, ACCEPTED, REJECTED).forEach(state -> {
            Department department =
                serviceDataHelper.setUpDepartment(departmentAdministrator, 1L, "department " + state);
            resourceService.updateState(department, state);
            departments.add(department);
        });
    }

    private void verifyDepartment(Department department, State expectedState, Action[] expectedActions,
                                  String expectedIndexData) {
        serviceVerificationHelper.verifyDepartment(
            department,
            university,
            "department " + expectedState,
            "department " + expectedState + " summary",
            expectedState,
            DRAFT,
            "university/department-" + expectedState.name().toLowerCase(),
            documentLogo,
            MemberCategory.values(),
            expectedActions,
            expectedIndexData,
            baseline);
    }

}
