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
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceFilter.ResourceFilterList;
import hr.prism.board.workflow.Execution;
import org.junit.After;
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
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/departmentService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
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

    @After
    public void tearDown() {
        reset(universityService, boardService, resourceService, resourceTaskService, userRoleService, documentService);
    }

    @Test
    public void getById_successWhenDraftAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 2L,
            university, "department-draft", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }

    @Test
    public void getById_successWhenDraftAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 2L,
            university, "department-draft", new Action[]{VIEW});
        verifyGetById((User) null, 2L,
            university, "department-draft", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 5L,
            university, "department-pending", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }

    @Test
    public void getById_successWhenPendingAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 5L,
            university, "department-pending", new Action[]{VIEW});
        verifyGetById((User) null, 5L,
            university, "department-pending", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 8L,
            university, "department-accepted", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});
    }

    @Test
    public void getById_successWhenAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 8L,
            university, "department-accepted", new Action[]{VIEW});
        verifyGetById((User) null, 8L,
            university, "department-accepted", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetById(users, 11L,
            university, "department-rejected", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getById_failureWhenRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = new Department();
        department.setId(11L);

        verifyGetByIdFailure(users, 11L, department);
        verifyGetByIdFailure((User) null, 11L, department);
    }

    @Test
    public void getByHandle_successWhenDraftAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-draft",
            university, "department-draft", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }

    @Test
    public void getByHandle_successWhenDraftAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-draft",
            university, "department-draft", new Action[]{VIEW});
        verifyGetByHandle((User) null, "university/department-draft",
            university, "department-draft", new Action[]{VIEW});
    }

    @Test
    public void getByHandle_successWhenPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-pending",
            university, "department-pending", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }

    @Test
    public void getByHandle_successWhenPendingAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-pending",
            university, "department-pending", new Action[]{VIEW});
        verifyGetByHandle((User) null, "university/department-pending",
            university, "department-pending", new Action[]{VIEW});
    }

    @Test
    public void getByHandle_successWhenAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-accepted",
            university, "department-accepted", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});
    }

    @Test
    public void getByHandle_successWhenAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-accepted",
            university, "department-accepted", new Action[]{VIEW});
        verifyGetByHandle((User) null, "university/department-accepted",
            university, "department-accepted", new Action[]{VIEW});
    }

    @Test
    public void getByHandle_successWhenRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        University university = (University) resourceService.getByHandle("university");
        verifyGetByHandle(users, "university/department-rejected",
            university, "department-rejected", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getByHandle_failureWhenRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-administrator@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = new Department();
        department.setId(11L);

        verifyGetByHandleFailure(users, "university/department-rejected", department);
        verifyGetByHandleFailure((User) null, "university/department-rejected", department);
    }

    @Test
    public void createDepartment_successWhenDefaultData() {
        LocalDateTime baseline = LocalDateTime.now();
        User user = userService.getByEmail("department-administrator@prism.hr");

        Department createdDepartment =
            departmentService.createDepartment(user, 1L,
                new DepartmentDTO()
                    .setName("department")
                    .setSummary("department summary"));

        Document expectedDocumentLogo = new Document();
        expectedDocumentLogo.setCloudinaryId("cloudinary id");

        Department selectedDepartment = departmentService.getById(user, createdDepartment.getId());
        Stream.of(createdDepartment, selectedDepartment).forEach(department ->
            verifyCreateDepartment(department, expectedDocumentLogo,
                Stream.of(MemberCategory.values()).collect(toList()), baseline));

        verifyInvocations(user, 1L, createdDepartment, MemberCategory.values());
    }

    @Test
    public void createDepartment_successWhenCustomData() {
        LocalDateTime baseline = LocalDateTime.now();
        User user = userService.getByEmail("department-administrator@prism.hr");

        DocumentDTO documentLogoDTO =
            new DocumentDTO()
                .setCloudinaryId("new cloudinary id")
                .setCloudinaryUrl("new cloudinary url")
                .setFileName("new file name");

        Department createdDepartment = departmentService.createDepartment(user, 1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary")
                .setDocumentLogo(documentLogoDTO)
                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT)));

        Department selectedDepartment = departmentService.getById(user, createdDepartment.getId());

        Document expectedDocumentLogo = new Document();
        expectedDocumentLogo.setCloudinaryId("new cloudinary id");

        Stream.of(createdDepartment, selectedDepartment).forEach(department ->
            verifyCreateDepartment(department, expectedDocumentLogo,
                ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT), baseline));

        verifyInvocations(user, 1L, createdDepartment,
            new MemberCategory[]{UNDERGRADUATE_STUDENT, MASTER_STUDENT});
        verify(documentService, times(1)).getOrCreateDocument(documentLogoDTO);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void getDepartments_successWhenDepartmentAdministrator() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter());

        assertThat(departments).hasSize(4);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university,
            "department-accepted", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});

        verifyDepartment(departments.get(1), university,
            "department-draft", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(2), university,
            "department-pending", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(3), university,
            "department-rejected", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenDepartmentAdministratorAndState() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter().setState(ResourceFilterList.of(ACCEPTED)));

        assertThat(departments).hasSize(1);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university,
            "department-accepted", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenDepartmentAdministratorAndAction() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter().setAction(EXTEND));

        assertThat(departments).hasSize(3);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university,
            "department-accepted", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE});

        verifyDepartment(departments.get(1), university,
            "department-draft", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        verifyDepartment(departments.get(2), university,
            "department-pending", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenDepartmentAdministratorAndSearchTerm() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter().setSearchTerm("rejected"));

        assertThat(departments).hasSize(1);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university,
            "department-rejected", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenDepartmentAdministratorAndSearchTermTypo() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter().setSearchTerm("rIJECT"));

        assertThat(departments).hasSize(1);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university,
            "department-rejected", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_failureWhenDepartmentAdministratorAndSearchTermWithoutResults() {
        User user = userService.getByEmail("department-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter().setSearchTerm("xyz"));

        assertThat(departments).hasSize(0);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void getDepartments_successWhenOtherDepartmentAdministrator() {
        User user = userService.getByEmail("department-rejected-administrator@prism.hr");

        List<Department> departments =
            departmentService.getDepartments(user, new ResourceFilter());

        assertThat(departments).hasSize(4);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university, "department-accepted", new Action[]{VIEW});
        verifyDepartment(departments.get(1), university, "department-draft", new Action[]{VIEW});
        verifyDepartment(departments.get(2), university, "department-pending", new Action[]{VIEW});
        verifyDepartment(departments.get(3), university,
            "department-rejected", new Action[]{VIEW, EDIT, SUBSCRIBE});
    }

    @Test
    public void getDepartments_successWhenUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-author@prism.hr"),
            userService.getByEmail("department-draft-member-pending@prism.hr"),
            userService.getByEmail("department-draft-member-accepted@prism.hr"),
            userService.getByEmail("department-draft-member-rejected@prism.hr"),
            userService.getByEmail("department-draft-post-administrator@prism.hr"),
            userService.getByEmail("department-pending-author@prism.hr"),
            userService.getByEmail("department-pending-member-pending@prism.hr"),
            userService.getByEmail("department-pending-member-accepted@prism.hr"),
            userService.getByEmail("department-pending-member-rejected@prism.hr"),
            userService.getByEmail("department-pending-post-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        verifyGetDepartments(users);
        verifyGetDepartments((User) null);
    }

    private void verifyGetById(User[] users, Long id, University expectedUniversity, String expectedName,
                               Action[] expectedActions) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetById(user, id, expectedUniversity, expectedName, expectedActions);
        });
    }

    private void verifyGetById(User user, Long id, University expectedUniversity, String expectedName,
                               Action[] expectedActions) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by id: " + id + ": " + userGivenName);

        Department department = departmentService.getById(user, id);

        verifyDepartment(department, expectedUniversity, expectedName, expectedActions);
        verifyInvocations(user, id, department);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByIdFailure(User[] users, Long id, Department department) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByIdFailure(user, id, department);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByIdFailure(User user, Long id, Department department) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by id: " + id + ": " + userGivenName);

        assertThatThrownBy(() -> departmentService.getById(user, id))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

        verifyInvocations(user, id, department);
    }

    private void verifyGetByHandle(User[] users, String handle, University expectedUniversity, String expectedName,
                                   Action[] expectedActions) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByHandle(user, handle, expectedUniversity, expectedName, expectedActions);
        });
    }

    private void verifyGetByHandle(User user, String handle, University expectedUniversity, String expectedName,
                                   Action[] expectedActions) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by handle: " + handle + ": " + userGivenName);

        Department department = departmentService.getByHandle(user, handle);

        verifyDepartment(department, expectedUniversity, expectedName, expectedActions);
        verifyInvocations(user, handle, department);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByHandleFailure(User[] users, String handle, Department department) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByHandleFailure(user, handle, department);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetByHandleFailure(User user, String handle, Department department) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by handle: " + handle + ": " + userGivenName);

        assertThatThrownBy(() -> departmentService.getByHandle(user, handle))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

        verifyInvocations(user, handle, department);
    }

    private void verifyDepartment(Department department, University expectedUniversity, String expectedName, Action[] expectedActions) {
        serviceHelper.verifyIdentity(department, expectedUniversity, expectedName);
        serviceHelper.verifyActions(department, expectedActions);
    }

    private void verifyCreateDepartment(Department department, Document expectedDocumentLogo,
                                        List<MemberCategory> expectedMemberCategories, LocalDateTime baseline) {
        University expectedUniversity = new University();
        expectedUniversity.setId(1L);

        verifyDepartment(department, expectedUniversity,
            "department", new Action[]{VIEW, EDIT, EXTEND, SUBSCRIBE});

        assertEquals("department summary", department.getSummary());
        assertEquals(expectedDocumentLogo, department.getDocumentLogo());
        assertEquals("university/department", department.getHandle());
        assertEquals(DRAFT, department.getState());
        assertEquals(DRAFT, department.getPreviousState());
        assertEquals(toStrings(expectedMemberCategories), department.getMemberCategoryStrings());

        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThanOrEqualTo(baseline);
        serviceHelper.verifyTimestamps(department, baseline);
    }

    private void verifyGetDepartments(User[] users) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetDepartments(user);
        });
    }

    private void verifyGetDepartments(User user) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Getting departments: " + userGivenName);
        List<Department> departments = departmentService.getDepartments(user, new ResourceFilter());

        assertThat(departments).hasSize(3);

        University university = new University();
        university.setId(1L);

        verifyDepartment(departments.get(0), university, "department-accepted", new Action[]{VIEW});
        verifyDepartment(departments.get(1), university, "department-draft", new Action[]{VIEW});
        verifyDepartment(departments.get(2), university, "department-pending", new Action[]{VIEW});
    }

    private void verifyInvocations(User user, Long id, Department department) {
        verify(resourceService, times(1))
            .getResource(user, DEPARTMENT, id);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(department), eq(VIEW), any(Execution.class));
    }

    private void verifyInvocations(User user, String handle, Department department) {
        verify(resourceService, atLeastOnce())
            .getResource(user, DEPARTMENT, handle);

        verify(actionService, atLeastOnce())
            .executeAction(eq(user), eq(department), eq(VIEW), any(Execution.class));
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyInvocations(User user, Long universityId, Department department,
                                   MemberCategory[] memberCategories) {
        University university = new University();
        university.setId(universityId);

        verify(universityService, times(1)).getById(universityId);
        verify(resourceDAO).checkUniqueName(DEPARTMENT, null, university, "department");
        verify(resourceService, times(1)).createHandle(department);

        verify(resourceService, times(1))
            .updateCategories(department, MEMBER, toStrings(asList(memberCategories)));

        verify(resourceService, times(1)).createResourceRelation(university, department);

        verify(resourceService, times(1))
            .createResourceOperation(department, EXTEND, user);

        verify(userRoleService, times(1))
            .createUserRole(department, user, ADMINISTRATOR);

        Long departmentId = department.getId();
        verify(boardService).createBoard(user, departmentId,
            new BoardDTO()
                .setName("Career Opportunities")
                .setPostCategories(ImmutableList.of("Employment", "Internship", "Volunteering")));

        verify(boardService).createBoard(user, departmentId,
            new BoardDTO()
                .setName("Research Opportunities")
                .setPostCategories(ImmutableList.of("MRes", "PhD", "Postdoc")));

        verify(resourceTaskService, times(1))
            .createForNewResource(departmentId, user.getId(), DEPARTMENT_TASKS);
    }

}
