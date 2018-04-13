package hr.prism.board.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.enums.Activity;
import hr.prism.board.exception.*;
import hr.prism.board.repository.DocumentRepository;
import hr.prism.board.repository.ResourceTaskRepository;
import hr.prism.board.representation.*;
import hr.prism.board.service.DepartmentPaymentService;
import hr.prism.board.service.ScheduledService;
import hr.prism.board.service.TestActivityService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.ObjectUtils;
import hr.prism.board.utils.BoardUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hr.prism.board.TestHelper.mockHttpServletResponse;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.exception.ExceptionCode.*;

@TestContext
@RunWith(SpringRunner.class)
public class DepartmentApiIT extends AbstractIT {

    private static LinkedHashMultimap<State, Action> ADMIN_ACTIONS = LinkedHashMultimap.create();
    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.EXTEND, Action.SUBSCRIBE));
        PUBLIC_ACTIONS.putAll(State.ACCEPTED, Collections.singletonList(Action.VIEW));
    }

    @Value("${resource.task.notification.interval1.seconds}")
    private Long resourceTaskNotificationInterval1Seconds;

    @Value("${resource.task.notification.interval2.seconds}")
    private Long resourceTaskNotificationInterval2Seconds;

    @Value("${resource.task.notification.interval3.seconds}")
    private Long resourceTaskNotificationInterval3Seconds;

    @Value("${department.draft.expiry.seconds}")
    private Long departmentDraftExpirySeconds;

    @Value("${department.pending.expiry.seconds}")
    private Long departmentPendingExpirySeconds;

    @Value("${department.pending.notification.interval1.seconds}")
    private Long departmentPendingNotificationInterval1Seconds;

    @Value("${department.pending.notification.interval2.seconds}")
    private Long departmentPendingNotificationInterval2Seconds;

    @Inject
    private DocumentRepository documentRepository;

    @Inject
    private ResourceTaskRepository resourceTaskRepository;

    @Inject
    private DepartmentPaymentService departmentPaymentService;

    @Inject
    private ScheduledService scheduledService;

    @Test
    public void shouldCreateDepartment() {
        testUserService.authenticate();
        University university = universityService.getOrCreateUniversity("University College London", "ucl");
        Long universityId = university.getId();

        Document documentLogo = new Document();
        documentLogo.setCloudinaryId("c");
        documentLogo.setCloudinaryUrl("u");
        documentLogo.setFileName("f");

        documentRepository.save(documentLogo);
        university.setDocumentLogo(documentLogo);
        universityRepository.update(university);

        DepartmentDTO department =
            new DepartmentDTO()
                .setName("department")
                .setSummary("summary");

        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, department);
        Long departmentId = departmentR.getId();

        String departmentName = department.getName();
        Assert.assertEquals(departmentName, departmentR.getName());
        Assert.assertEquals(departmentName, departmentR.getHandle());
        Assert.assertEquals("summary", departmentR.getSummary());
        Assert.assertEquals(Stream.of(MemberCategory.values()).collect(Collectors.toList()), departmentR.getMemberCategories());
        Assert.assertEquals(State.DRAFT, departmentR.getState());

        DocumentRepresentation documentR = departmentR.getDocumentLogo();
        Assert.assertEquals("c", documentR.getCloudinaryId());
        Assert.assertEquals("u", documentR.getCloudinaryUrl());
        Assert.assertEquals("f", documentR.getFileName());

        verifyNewDepartmentBoards(departmentId);
        ExceptionUtils.verifyDuplicateException(() -> departmentApi.createDepartment(universityId, department), ExceptionCode.DUPLICATE_DEPARTMENT, departmentId, null);
    }

    @Test
    public void shouldCreateDepartmentOverridingDefaults() {
        testUserService.authenticate();
        University university = universityService.getOrCreateUniversity("University College London", "ucl");
        Long universityId = university.getId();

        DepartmentDTO department =
            new DepartmentDTO()
                .setName("department")
                .setSummary("summary")
                .setDocumentLogo(
                    new DocumentDTO()
                        .setCloudinaryId("d")
                        .setCloudinaryUrl("v")
                        .setFileName("g"))
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT));

        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, department);
        Long departmentId = departmentR.getId();

        String departmentName = department.getName();
        Assert.assertEquals(departmentName, departmentR.getName());
        Assert.assertEquals(departmentName, departmentR.getHandle());
        Assert.assertEquals("summary", departmentR.getSummary());
        Assert.assertEquals(Collections.singletonList(UNDERGRADUATE_STUDENT), departmentR.getMemberCategories());
        Assert.assertEquals(State.DRAFT, departmentR.getState());

        DocumentRepresentation documentR = departmentR.getDocumentLogo();
        Assert.assertEquals("d", documentR.getCloudinaryId());
        Assert.assertEquals("v", documentR.getCloudinaryUrl());
        Assert.assertEquals("g", documentR.getFileName());

        verifyNewDepartmentBoards(departmentId);
    }

    @Test
    public void shouldCreateAndListDepartments() {
        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        User user1 = testUserService.authenticate();
        DepartmentDTO departmentDTO1 = new DepartmentDTO().setName("department1").setSummary("department summary");
        DepartmentRepresentation departmentR1 = verifyPostDepartment(universityId, departmentDTO1, "department1");

        Long departmentId1 = departmentR1.getId();
        Long boardId1 = boardApi.getBoards(departmentId1, null, null, null, null).get(0).getId();
        unprivilegedUsers.put("department1", makeUnprivilegedUsers(boardId1, 10,
            TestHelper.samplePost().setPostCategories(Collections.singletonList("Employment"))));

        testUserService.setAuthentication(user1.getId());
        DepartmentDTO departmentDTO2 = new DepartmentDTO().setName("department2").setSummary("department summary");
        DepartmentRepresentation departmentR2 = verifyPostDepartment(universityId, departmentDTO2, "department2");

        Long departmentId2 = departmentR2.getId();
        Long boardId2 = boardApi.getBoards(departmentId2, null, null, null, null).get(0).getId();
        unprivilegedUsers.put("department2", makeUnprivilegedUsers(boardId2, 20,
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("Employment"))
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT))));

        User user2 = testUserService.authenticate();
        DepartmentDTO departmentDTO3 = new DepartmentDTO().setName("department3").setSummary("department summary");
        DepartmentRepresentation departmentR3 = verifyPostDepartment(universityId, departmentDTO3, "department3");

        Long departmentId3 = departmentR3.getId();
        Long boardId3 = boardApi.getBoards(departmentId3, null, null, null, null).get(0).getId();
        unprivilegedUsers.put("department3", makeUnprivilegedUsers(boardId3, 30,
            TestHelper.samplePost().setPostCategories(Collections.singletonList("Employment"))));

        testUserService.setAuthentication(user2.getId());
        DepartmentDTO departmentDTO4 = new DepartmentDTO().setName("department4").setSummary("department summary");
        DepartmentRepresentation departmentR4 = verifyPostDepartment(universityId, departmentDTO4, "department4");

        Long departmentId4 = departmentR4.getId();
        Long boardId4 = boardApi.getBoards(departmentId4, null, null, null, null).get(0).getId();
        unprivilegedUsers.put("department4", makeUnprivilegedUsers(boardId4, 40,
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("Employment"))
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT))));

        List<String> departmentNames = Arrays.asList(
            "department1", "department10", "department2", "department20", "department3", "department30", "department4", "department40");

        testUserService.unauthenticate();
        verifyUnprivilegedDepartmentUser(departmentNames);

        for (String departmentName : unprivilegedUsers.keySet()) {
            Map<Scope, User> unprivilegedUserMap = unprivilegedUsers.get(departmentName);
            for (Scope scope : unprivilegedUserMap.keySet()) {
                testUserService.setAuthentication(unprivilegedUserMap.get(scope).getId());
                if (scope == Scope.DEPARTMENT) {
                    verifyPrivilegedDepartmentUser(departmentNames, Collections.singletonList(departmentName + "0"));
                } else {
                    verifyUnprivilegedDepartmentUser(departmentNames);
                }
            }
        }

        testUserService.setAuthentication(user1.getId());
        verifyPrivilegedDepartmentUser(departmentNames, Arrays.asList("department1", "department2"));

        testUserService.setAuthentication(user2.getId());
        verifyPrivilegedDepartmentUser(departmentNames, Arrays.asList("department3", "department4"));
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentHandle() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        verifyPostDepartment(universityId,
            new DepartmentDTO().setName("new department with long name").setSummary("department summary"), "new-department-with-long");

        Long departmentId = verifyPostDepartment(universityId,
            new DepartmentDTO().setName("new department with long name too").setSummary("department summary"), "new-department-with-long-2").getId();

        DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId,
            new DepartmentPatchDTO()
                .setHandle(Optional.of("new-department-with-longer")));
        Assert.assertEquals("new-department-with-longer", departmentR.getHandle());

        verifyPostDepartment(universityId,
            new DepartmentDTO().setName("new department with long name also").setSummary("department summary"), "new-department-with-long-2");
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        ExceptionUtils.verifyDuplicateException(
            () -> departmentApi.updateDepartment(departmentRs.getKey().getId(),
                new DepartmentPatchDTO()
                    .setName(Optional.of(departmentRs.getValue().getName()))),
            ExceptionCode.DUPLICATE_DEPARTMENT, departmentRs.getValue().getId());
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentHandlesByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        ExceptionUtils.verifyException(
            BoardDuplicateException.class,
            () -> departmentApi.updateDepartment(departmentRs.getKey().getId(),
                new DepartmentPatchDTO()
                    .setHandle(Optional.of(departmentRs.getValue().getHandle()))),
            ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
    }

    @Test
    public void shouldSupportDepartmentActionsAndPermissions() {
        // Create department and board
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
        Long departmentId = departmentR.getId();
        Long boardId = boardApi.getBoards(departmentId, null, null, null, null).get(0).getId();

        // Create post
        User postUser = testUserService.authenticate();
        postApi.postPost(boardId,
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("Employment"))
                .setMemberCategories(Collections.singletonList(MASTER_STUDENT)));

        // Create unprivileged users
        List<User> unprivilegedUsers = Lists.newArrayList(
            makeUnprivilegedUsers(boardId, 2,
                TestHelper.smallSamplePost()
                    .setPostCategories(Collections.singletonList("Employment"))
                    .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT)))
                .values());

        unprivilegedUsers.add(postUser);

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.EDIT, () -> departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO()))
            .build();

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we do not audit viewing
        departmentApi.getDepartment(departmentId);

        // Check that we can make changes and leave nullable values null
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 2"))
                .setHandle(Optional.of("department-2")),
            State.DRAFT);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and set nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 3"))
                .setSummary(Optional.of("department 3 summary"))
                .setHandle(Optional.of("department-3"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
                .setMemberCategories(Optional.of(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))),
            State.DRAFT);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and change nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 4"))
                .setSummary(Optional.of("department 4 summary"))
                .setHandle(Optional.of("department-4"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                .setMemberCategories(Optional.of(ImmutableList.of(MASTER_STUDENT, UNDERGRADUATE_STUDENT))),
            State.DRAFT);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can clear nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setDocumentLogo(Optional.empty())
                .setMemberCategories(Optional.empty()),
            State.DRAFT);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        testNotificationService.record();
        testUserService.setAuthentication(departmentUser.getId());
        Long departmentUser2Id =
            departmentUserApi.createUserRole(departmentId,
                new UserRoleDTO()
                    .setUser(new UserDTO()
                        .setGivenName("admin1")
                        .setSurname("admin1")
                        .setEmail("admin1@admin1.com"))
                    .setRole(Role.ADMINISTRATOR)).getUser().getId();

        User departmentUser2 = userCacheService.findOne(departmentUser2Id);
        UserRole department2UserRole = userRoleService.findByResourceAndUserAndRole(resourceService.findOne(departmentId), departmentUser2, Role.ADMINISTRATOR);
        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_NOTIFICATION, userCacheService.findOne(departmentUser2Id),
            ImmutableMap.<String, String>builder()
                .put("recipient", "admin1")
                .put("department", "department 4")
                .put("resourceRedirect", serverUrl + "/redirect?resource=" + departmentId)
                .put("invitationUuid", department2UserRole.getUuid())
                .build()));

        testUserService.setAuthentication(departmentUser.getId());
        departmentUserApi.updateUserRole(departmentId, departmentUser2Id, new UserRoleDTO().setRole(Role.AUTHOR));

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        testNotificationService.verify();
        testNotificationService.stop();

        testUserService.setAuthentication(departmentUser.getId());
        List<ResourceOperationRepresentation> resourceOperationRs = departmentApi.getDepartmentOperations(departmentId);
        Assert.assertEquals(5, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, departmentUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("name", "department", "department 2")
                .put("handle", "department", "department-2"));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("name", "department 2", "department 3")
                .put("summary", "department summary", "department 3 summary")
                .put("handle", "department-2", "department-3")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories",
                    Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT", "RESEARCH_STUDENT", "RESEARCH_STAFF"),
                    Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("name", "department 3", "department 4")
                .put("summary", "department 3 summary", "department 4 summary")
                .put("handle", "department-3", "department-4")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"),
                    ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
                .put("memberCategories",
                    Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT"),
                    Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("documentLogo", ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"), null)
                .put("memberCategories", Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT"), null));
    }

    @Test
    public void shouldSupportDepartmentTasks() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentUserId = departmentUser.getId();

        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();
        listenForActivities(departmentUserId);

        LocalDateTime resourceTaskCreatedTimestamp =
            resourceTaskRepository.findByResourceId(departmentId).iterator().next().getCreatedTimestamp();

        String recipient = departmentUser.getGivenName();
        String resourceTaskRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=tasks";

        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval1Seconds + 1));
        scheduledService.notifyDepartmentTasks(LocalDateTime.now());

        testActivityService.verify(departmentUserId,
            new TestActivityService.ActivityInstance(departmentId, Activity.CREATE_TASK_ACTIVITY));

        Department department0 = (Department) resourceService.findOne(departmentId);
        String departmentAdminRole1Uuid = userRoleService.findByResourceAndUserAndRole(department0, departmentUser, Role.ADMINISTRATOR).getUuid();

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", recipient)
                    .put("department", "department")
                    .put("resourceTask",
                        "<ul><li>Ready to get started - visit the user management area to build your student list.</li>" +
                            "<li>Got something to share - create some posts and start sending notifications.</li>" +
                            "<li>Time to tell the world - go to the badges section to learn about promoting your board on your website.</li></ul>")
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole1Uuid)
                    .build()));

        DepartmentDashboardRepresentation dashboard1 = departmentApi.getDepartmentDashboard(departmentId);
        Assert.assertNull(dashboard1.getTasks().get(0).getCompleted());
        Assert.assertNull(dashboard1.getTasks().get(1).getCompleted());
        Assert.assertNull(dashboard1.getTasks().get(2).getCompleted());

        departmentUserApi.createMembers(departmentId,
            Collections.singletonList(
                new UserRoleDTO()
                    .setUser(
                        new UserDTO()
                            .setGivenName("member")
                            .setSurname("member")
                            .setEmail("member@member.com"))
                    .setEmail("member@member.com")
                    .setRole(Role.MEMBER)
                    .setMemberCategory(UNDERGRADUATE_STUDENT)
                    .setMemberProgram("program")
                    .setMemberYear(1)
                    .setExpiryDate(LocalDate.now().plusYears(3))));

        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval2Seconds + 1));

        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", recipient)
                    .put("department", "department")
                    .put("resourceTask",
                        "<ul><li>Got something to share - create some posts and start sending notifications.</li>" +
                            "<li>Time to tell the world - go to the badges section to learn about promoting your board on your website.</li></ul>")
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole1Uuid)
                    .build()));

        DepartmentDashboardRepresentation dashboard2 = departmentApi.getDepartmentDashboard(departmentId);
        Assert.assertTrue(dashboard2.getTasks().get(0).getCompleted());
        Assert.assertNull(dashboard2.getTasks().get(1).getCompleted());
        Assert.assertNull(dashboard2.getTasks().get(2).getCompleted());

        Long boardId = boardApi.getBoards(departmentId, true, null, null, null).get(0).getId();
        postApi.postPost(boardId,
            new PostDTO()
                .setName("name")
                .setSummary("summary")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("GB")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("Employment"))
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT))
                .setApplyWebsite("http://www.google.co.uk"));

        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval3Seconds + 1));

        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", recipient)
                    .put("department", "department")
                    .put("resourceTask",
                        "<ul><li>Time to tell the world - go to the badges section to learn about promoting your board on your website.</li></ul>")
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole1Uuid)
                    .build()));

        DepartmentDashboardRepresentation dashboard3 = departmentApi.getDepartmentDashboard(departmentId);
        Assert.assertTrue(dashboard3.getTasks().get(0).getCompleted());
        Assert.assertTrue(dashboard3.getTasks().get(1).getCompleted());
        Assert.assertNull(dashboard3.getTasks().get(2).getCompleted());

        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
        testNotificationService.verify();

        WidgetOptionsDTO optionsDTO = new WidgetOptionsDTO();
        try {
            departmentApi.getDepartmentBadge(departmentId, objectMapper.writeValueAsString(optionsDTO), mockHttpServletResponse());
        } catch (IOException e) {
            throw new Error(e);
        }

        testActivityService.verify(departmentUserId);
        DepartmentDashboardRepresentation dashboard4 = departmentApi.getDepartmentDashboard(departmentId);
        Assert.assertTrue(dashboard4.getTasks().get(0).getCompleted());
        Assert.assertTrue(dashboard4.getTasks().get(1).getCompleted());
        Assert.assertTrue(dashboard4.getTasks().get(2).getCompleted());

        LocalDateTime baseline = scheduledService.getBaseline();
        LocalDateTime baseline1 = baseline.minusMonths(1).minusDays(1);
        Department department1 = departmentService.getDepartment(departmentId);
        department1.setCreatedTimestamp(baseline1);
        department1.setLastMemberTimestamp(baseline1);
        department1.setLastTaskCreationTimestamp(baseline.minusYears(1));
        resourceRepository.update(department1);

        scheduledService.updateDepartmentTasks(scheduledService.getBaseline());
        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval1Seconds + 1));
        scheduledService.notifyDepartmentTasks(LocalDateTime.now());

        testActivityService.verify(departmentUserId,
            new TestActivityService.ActivityInstance(departmentId, Activity.UPDATE_TASK_ACTIVITY));

        String resourceUpdateTask =
            "<ul><li>New students arriving - visit the user management area to update your student list.</li></ul>";
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.UPDATE_TASK_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", recipient)
                    .put("department", "department")
                    .put("resourceTask", resourceUpdateTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole1Uuid)
                    .build()));

        testActivityService.stop();
        testNotificationService.stop();
    }

    @Test
    public void shouldSupportDepartmentSubscriptions() throws IOException {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentUserId = departmentUser.getId();

        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
        Assert.assertEquals(State.DRAFT, departmentR.getState());
        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.SUBSCRIBE);

        // Check notifications not fired for draft state
        LocalDateTime baseline = LocalDateTime.now();
        scheduledService.updateDepartmentSubscriptions(baseline);

        Long departmentId = departmentR.getId();
        Department department = (Department) resourceRepository.findOne(departmentId);

        // Simulate ending the draft stage
        resourceRepository.updateStateChangeTimestampById(departmentId,
            department.getStateChangeTimestamp().minusSeconds(departmentDraftExpirySeconds + 1));
        departmentService.updateSubscriptions(LocalDateTime.now());

        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.PENDING, departmentR.getState());
        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.SUBSCRIBE);

        testActivityService.record();
        testNotificationService.record();
        listenForActivities(departmentUserId);

        String recipient = departmentUser.getGivenName();
        department = (Department) resourceService.findOne(departmentId);
        Assert.assertNull(department.getNotifiedCount());

        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(department, departmentUser, Role.ADMINISTRATOR).getUuid();
        String pendingExpiryDeadline = department.getStateChangeTimestamp()
            .plusSeconds(departmentPendingExpirySeconds).toLocalDate().format(BoardUtils.DATETIME_FORMATTER);
        String accountRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=account";

        // Update subscriptions - check notifications only fired once
        scheduledService.updateDepartmentSubscriptions(baseline);
        scheduledService.updateDepartmentSubscriptions(baseline);

        testActivityService.verify(departmentUserId, new TestActivityService.ActivityInstance(departmentId, Activity.SUBSCRIBE_DEPARTMENT_ACTIVITY));
        userApi.dismissActivity(userApi.getActivities().iterator().next().getId());
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUBSCRIBE_DEPARTMENT_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", recipient)
                .put("pendingExpiryDeadline", pendingExpiryDeadline)
                .put("department", "department")
                .put("accountRedirect", accountRedirect)
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        department = (Department) resourceService.findOne(departmentId);
        Assert.assertEquals(new Integer(1), department.getNotifiedCount());
        LocalDateTime pendingCommencedTimestamp = department.getStateChangeTimestamp();

        // Simulate end of first notification period
        resourceRepository.updateStateChangeTimestampById(departmentId, pendingCommencedTimestamp.minusSeconds(departmentPendingNotificationInterval1Seconds + 1));
        departmentService.updateSubscriptions(LocalDateTime.now());
        department = (Department) resourceService.findOne(departmentId);

        pendingExpiryDeadline = department.getStateChangeTimestamp()
            .plusSeconds(departmentPendingExpirySeconds).toLocalDate().format(BoardUtils.DATETIME_FORMATTER);

        // Update subscriptions - check notifications only fired once
        scheduledService.updateDepartmentSubscriptions(baseline);
        scheduledService.updateDepartmentSubscriptions(baseline);

        verifyActivitiesEmpty(departmentUserId);
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUBSCRIBE_DEPARTMENT_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", recipient)
                .put("pendingExpiryDeadline", pendingExpiryDeadline)
                .put("department", "department")
                .put("accountRedirect", accountRedirect)
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        // Simulate end of second notification period
        resourceRepository.updateStateChangeTimestampById(departmentId, pendingCommencedTimestamp.minusSeconds(departmentPendingNotificationInterval2Seconds + 1));
        departmentService.updateSubscriptions(LocalDateTime.now());
        department = (Department) resourceService.findOne(departmentId);

        pendingExpiryDeadline = department.getStateChangeTimestamp()
            .plusSeconds(departmentPendingExpirySeconds).toLocalDate().format(BoardUtils.DATETIME_FORMATTER);

        // Update subscriptions - check notifications only fired once
        scheduledService.updateDepartmentSubscriptions(baseline);
        scheduledService.updateDepartmentSubscriptions(baseline);

        verifyActivitiesEmpty(departmentUserId);
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUBSCRIBE_DEPARTMENT_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", recipient)
                .put("pendingExpiryDeadline", pendingExpiryDeadline)
                .put("department", "department")
                .put("accountRedirect", accountRedirect)
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        testNotificationService.record();
        testActivityService.stop();

        JsonNode customer = departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source");
        Assert.assertNotNull(customer);
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());
        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.SUBSCRIBE, Action.UNSUBSCRIBE);
        Assert.assertEquals("id", departmentR.getCustomerId());
    }

    @Test
    public void shouldRejectDepartmentWhenTrialPeriodEnds() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        // Verify department actions
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
        Assert.assertEquals(State.DRAFT, departmentR.getState());
        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.SUBSCRIBE);

        Long departmentId = departmentR.getId();
        Department department = (Department) resourceRepository.findOne(departmentId);

        // Verify board actions
        List<BoardRepresentation> boardRs = boardApi.getBoards(departmentId, null, null, null, null);
        boardRs.forEach(boardR -> {
            Assert.assertEquals(State.ACCEPTED, boardR.getState());
            Assertions.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.REJECT);
        });

        // Create post
        PostRepresentation postR = postApi.postPost(boardRs.get(0).getId(),
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("Employment"))
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT)));
        Assert.assertEquals(State.PENDING, postR.getState());
        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsOnly(Action.VIEW, Action.EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
        Long postId = postR.getId();

        // Verify post actions
        postService.publishAndRetirePosts(LocalDateTime.now());
        postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
        Assert.assertEquals(State.ACCEPTED, postR.getState());
        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsOnly(Action.VIEW, Action.PURSUE, Action.EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);

        // Simulate ending the draft stage
        resourceRepository.updateStateChangeTimestampById(departmentId,
            department.getStateChangeTimestamp().minusSeconds(departmentDraftExpirySeconds + 1));
        departmentService.updateSubscriptions(LocalDateTime.now());

        // Verify department actions
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.PENDING, departmentR.getState());
        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.SUBSCRIBE);

        // Verify board actions
        boardRs = boardApi.getBoards(departmentId, null, null, null, null);
        boardRs.forEach(boardR -> {
            Assert.assertEquals(State.ACCEPTED, boardR.getState());
            Assertions.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND, Action.REJECT);
        });

        // Verify post actions
        postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
        Assert.assertEquals(State.ACCEPTED, postR.getState());
        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsOnly(Action.VIEW, Action.PURSUE, Action.EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);

        // Create post
        PostRepresentation post2R = postApi.postPost(boardRs.get(0).getId(),
            TestHelper.smallSamplePost()
                .setName("post2")
                .setPostCategories(Collections.singletonList("Employment"))
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT)));
        Long post2Id = post2R.getId();
        Assert.assertEquals(State.PENDING, post2R.getState());

        // Simulate ending the pending stage
        resourceRepository.updateStateChangeTimestampById(departmentId,
            department.getStateChangeTimestamp().minusSeconds(departmentPendingExpirySeconds + 1));
        departmentService.updateSubscriptions(LocalDateTime.now());

        // Verify department actions
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.REJECTED, departmentR.getState());
        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.SUBSCRIBE);

        // Verify board actions
        boardRs = boardApi.getBoards(departmentId, null, null, null, null);
        boardRs.forEach(boardR -> {
            Assert.assertEquals(State.ACCEPTED, boardR.getState());
            Assertions.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(Action.VIEW, Action.EDIT, Action.REJECT);
        });

        // Verify post actions
        postService.publishAndRetirePosts(LocalDateTime.now());
        postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
        Assert.assertEquals(State.ACCEPTED, postR.getState());
        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsOnly(Action.VIEW, Action.EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);

        // Verify post 2 actions
        post2R = postApi.getPost(post2Id, TestHelper.mockHttpServletRequest("address"));
        Assert.assertEquals(State.PENDING, post2R.getState());
        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()))
            .containsOnly(Action.VIEW, Action.EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
    }

    @Test
    public void shouldSuspendDepartmentWhenPaymentFails() throws IOException {
        User departmentUser = testUserService.authenticate();
        Long departmentUserId = departmentUser.getId();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
        Assert.assertEquals(State.DRAFT, departmentR.getState());
        Long departmentId = departmentR.getId();

        departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        testActivityService.record();
        testNotificationService.record();
        listenForActivities(departmentUserId);

        String recipient = departmentUser.getGivenName();
        Department department = (Department) resourceService.findOne(departmentId);
        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(department, departmentUser, Role.ADMINISTRATOR).getUuid();
        String accountRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=account";

        departmentPaymentService.processSubscriptionSuspension("id");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        testActivityService.verify(departmentUserId, new TestActivityService.ActivityInstance(departmentId, Activity.SUSPEND_DEPARTMENT_ACTIVITY));
        userApi.dismissActivity(userApi.getActivities().iterator().next().getId());
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_DEPARTMENT_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", recipient)
                .put("department", "department")
                .put("accountRedirect", accountRedirect)
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        departmentPaymentService.processSubscriptionSuspension("id");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        // Second failed payment event should not result in another activity
        Assertions.assertThat(userApi.getActivities()).isEmpty();
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_DEPARTMENT_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", recipient)
                .put("department", "department")
                .put("accountRedirect", accountRedirect)
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        testActivityService.stop();
        testNotificationService.stop();

        departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source2");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        departmentPaymentApi.setPaymentSourceAsDefault(departmentId, "source2");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        departmentPaymentService.processSubscriptionCancellation("id");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.REJECTED, departmentR.getState());
    }

    @Test
    public void shouldLeaveDepartmentInSameStateWhenManuallyUnsubscribing() throws IOException {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
        Assert.assertEquals(State.DRAFT, departmentR.getState());
        Long departmentId = departmentR.getId();

        departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        departmentPaymentApi.cancelSubscription(departmentId);
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());
    }

    @Test
    @Sql("classpath:data/department_autosuggest_setup.sql")
    public void shouldSuggestDepartments() {
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        List<DepartmentRepresentation> departmentRs = departmentApi.findDepartments(universityId, "Computer");
        Assert.assertEquals(3, departmentRs.size());

        verifySuggestedDepartment("Computer Science Department", departmentRs.get(0));
        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(1));
        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(2));

        departmentRs = departmentApi.findDepartments(universityId, "Computer Science Laboratory");
        Assert.assertEquals(3, departmentRs.size());

        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(0));
        verifySuggestedDepartment("Computer Science Department", departmentRs.get(1));
        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(2));

        departmentRs = departmentApi.findDepartments(universityId, "School of Informatics");
        Assert.assertEquals(1, departmentRs.size());

        verifySuggestedDepartment("School of Informatics", departmentRs.get(0));

        departmentRs = departmentApi.findDepartments(universityId, "Physics");
        Assert.assertEquals(0, departmentRs.size());

        departmentRs = departmentApi.findDepartments(universityId, "Mathematics");
        Assert.assertEquals(0, departmentRs.size());
    }

    @Test
    public void shouldPostMembers() {
        User user = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");

        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
        List<UserRoleRepresentation> users = departmentUserApi.getUserRoles(departmentR.getId(), null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // add 200 members
        List<UserRoleDTO> userRoleDTOs1 = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            userRoleDTOs1.add(
                new UserRoleDTO()
                    .setUser(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"))
                    .setRole(Role.MEMBER).setMemberCategory(MASTER_STUDENT));
        }

        departmentUserApi.createMembers(departmentR.getId(), userRoleDTOs1);
        UserRolesRepresentation response = departmentUserApi.getUserRoles(departmentR.getId(), null);
        Assert.assertEquals(1, response.getUsers().size());
        Assert.assertEquals(200, response.getMembers().size());
        Assert.assertNull(response.getMemberToBeUploadedCount());

        List<UserRoleDTO> userRoleDTOs2 = new ArrayList<>();
        for (int i = 101; i <= 300; i++) {
            userRoleDTOs2.add(
                new UserRoleDTO()
                    .setUser(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"))
                    .setRole(Role.MEMBER).setMemberCategory(MASTER_STUDENT));
        }

        departmentUserApi.createMembers(departmentR.getId(), userRoleDTOs2);
        response = departmentUserApi.getUserRoles(departmentR.getId(), null);
        Assert.assertEquals(300, response.getMembers().size());
        Assert.assertNull(response.getMemberToBeUploadedCount());

        List<UserRoleDTO> userRoleDTOs3 = new ArrayList<>();
        userRoleDTOs3.add(
            new UserRoleDTO()
                .setUser(new UserDTO().setEmail("bulk301@mail.com").setGivenName("Bulk301").setSurname("User"))
                .setRole(Role.AUTHOR));
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.createMembers(departmentR.getId(), userRoleDTOs3), ExceptionCode.INVALID_RESOURCE_USER, null);
    }

    @Test
    public void shouldUpdateMembersByPosting() {
        User user = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");

        Long departmentId = departmentApi.createDepartment(universityId, departmentDTO).getId();
        List<UserRoleRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        List<UserRoleDTO> userRoleDTOs = new ArrayList<>();
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@knowles.com"))
            .setRole(Role.MEMBER).setMemberCategory(UNDERGRADUATE_STUDENT));
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("jakub").setSurname("fibinger").setEmail("jakub@fibinger.com"))
            .setRole(Role.MEMBER).setMemberCategory(UNDERGRADUATE_STUDENT));

        departmentUserApi.createMembers(departmentId, userRoleDTOs);
        List<UserRoleRepresentation> members = departmentUserApi.getUserRoles(departmentId, null).getMembers();

        verifyMember("jakub@fibinger.com", null, UNDERGRADUATE_STUDENT, members.get(0));
        verifyMember("alastair@knowles.com", null, UNDERGRADUATE_STUDENT, members.get(1));

        Long memberId = userRepository.findByEmail("alastair@knowles.com").getId();
        testUserService.setAuthentication(memberId);

        userApi.updateUser(new UserPatchDTO().setEmail(Optional.of("alastair@alastair.com")));

        testUserService.setAuthentication(user.getId());
        userRoleDTOs = new ArrayList<>();
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@knowles.com"))
            .setRole(Role.MEMBER).setExpiryDate(LocalDate.of(2018, 7, 1)).setMemberCategory(MASTER_STUDENT));

        departmentUserApi.createMembers(departmentId, userRoleDTOs);
        members = departmentUserApi.getUserRoles(departmentId, null).getMembers();

        verifyMember("jakub@fibinger.com", null, UNDERGRADUATE_STUDENT, members.get(0));
        verifyMember("alastair@alastair.com", LocalDate.of(2018, 7, 1), MASTER_STUDENT, members.get(1));
    }

    @Test
    public void shouldRequestAndAcceptMembership() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        User boardMember = testUserService.authenticate();
        departmentUserApi.createMembershipRequest(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.SIXTYFIVE_PLUS).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberYear(3)
                .setExpiryDate(LocalDate.now().plusYears(2)));

        Long boardMemberId = boardMember.getId();
        testActivityService.verify(departmentUserId,
            new TestActivityService.ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));

        Resource department = resourceService.findOne(departmentId);
        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(department, departmentUser, Role.ADMINISTRATOR).getUuid();

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", departmentUser.getGivenName())
                .put("department", departmentR.getName())
                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        testUserService.setAuthentication(boardMemberId);
        ExceptionUtils.verifyException(
            BoardForbiddenException.class,
            () -> departmentUserApi.reviewMembershipRequest(departmentId, boardMemberId, "accepted"),
            ExceptionCode.FORBIDDEN_ACTION);

        testUserService.setAuthentication(departmentUserId);
        departmentUserApi.reviewMembershipRequest(departmentId, boardMemberId, "accepted");

        verifyActivitiesEmpty(departmentUserId);
        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER);
        Assert.assertEquals(State.ACCEPTED, userRole.getState());

        testActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(boardMemberId);

        ExceptionUtils.verifyException(
            BoardException.class,
            () -> departmentUserApi.createMembershipRequest(departmentId,
                new UserRoleDTO().setMemberCategory(UNDERGRADUATE_STUDENT).setExpiryDate(LocalDate.now().plusYears(2))),
            ExceptionCode.DUPLICATE_PERMISSION);
    }

    @Test
    public void shouldRequestAndRejectMembership() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");

        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        User boardMember = testUserService.authenticate();
        departmentUserApi.createMembershipRequest(departmentId, new UserRoleDTO()
            .setUser(new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.FIFTY_SIXTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
            .setMemberCategory(UNDERGRADUATE_STUDENT)
            .setMemberProgram("program")
            .setMemberYear(2)
            .setExpiryDate(LocalDate.now().plusYears(2)));

        Long boardMemberId = boardMember.getId();
        testActivityService.verify(departmentUserId,
            new TestActivityService.ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));

        Resource department = resourceService.findOne(departmentId);
        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(department, departmentUser, Role.ADMINISTRATOR).getUuid();

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", departmentUser.getGivenName())
                .put("department", departmentR.getName())
                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        testUserService.setAuthentication(departmentUserId);
        departmentUserApi.reviewMembershipRequest(departmentId, boardMemberId, "rejected");

        verifyActivitiesEmpty(departmentUserId);
        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER);
        Assert.assertEquals(State.REJECTED, userRole.getState());

        testActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(boardMemberId);

        ExceptionUtils.verifyException(
            BoardForbiddenException.class,
            () -> departmentUserApi.createMembershipRequest(departmentId,
                new UserRoleDTO().setMemberCategory(UNDERGRADUATE_STUDENT).setExpiryDate(LocalDate.now().plusYears(2))),
            ExceptionCode.FORBIDDEN_PERMISSION);
    }

    @Test
    public void shouldRequestAndDismissMembership() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        User boardMember = testUserService.authenticate();
        departmentUserApi.createMembershipRequest(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.FIFTY_SIXTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberYear(1)
                .setExpiryDate(LocalDate.now().plusYears(2)));

        Long boardMemberId = boardMember.getId();
        testActivityService.verify(departmentUserId,
            new TestActivityService.ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));

        Resource department = resourceService.findOne(departmentId);
        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(department, departmentUser, Role.ADMINISTRATOR).getUuid();

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, departmentUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", departmentUser.getGivenName())
                .put("department", departmentR.getName())
                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
                .put("invitationUuid", departmentAdminRoleUuid)
                .build()));

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER);
        Long activityId = activityService.findByUserRoleAndActivity(userRole, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY).getId();

        testUserService.setAuthentication(departmentUserId);
        userApi.dismissActivity(activityId);

        verifyActivitiesEmpty(departmentUserId);
        testActivityService.stop();
        testNotificationService.stop();
    }

    @Test
    public void shouldSupportDepartmentDashboard() {
        Long departmentUserId = testUserService.authenticate().getId();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        Long departmentId1 = departmentApi.createDepartment(universityId,
            new DepartmentDTO().setName("department 1").setSummary("department summary")).getId();
        Long departmentId2 = departmentApi.createDepartment(universityId,
            new DepartmentDTO().setName("department 2").setSummary("department summary")).getId();

        StatisticsRepresentation emptyMemberStatistics =
            new StatisticsRepresentation<>()
                .setCountLive(0L)
                .setCountThisYear(0L)
                .setCountAllTime(0L)
                .setMostRecent(null);

        PostStatisticsRepresentation emptyPostStatistics =
            new PostStatisticsRepresentation()
                .setCountLive(0L)
                .setCountThisYear(0L)
                .setCountAllTime(0L)
                .setMostRecent(null)
                .setViewCountLive(0L)
                .setViewCountThisYear(0L)
                .setViewCountAllTime(0L)
                .setMostRecentView(null)
                .setReferralCountLive(0L)
                .setReferralCountThisYear(0L)
                .setReferralCountAllTime(0L)
                .setMostRecentReferral(null)
                .setResponseCountLive(0L)
                .setResponseCountThisYear(0L)
                .setResponseCountAllTime(0L)
                .setMostRecentResponse(null);

        DepartmentDashboardRepresentation department1Dashboard1 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(emptyMemberStatistics, department1Dashboard1.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department1Dashboard1.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard1 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard1.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard1.getPostStatistics());

        UserRoleRepresentation member1 = departmentUserApi.createUserRole(departmentId1,
            new UserRoleDTO()
                .setUser(new UserDTO().setGivenName("one").setSurname("one").setEmail("one@one.com"))
                .setRole(Role.MEMBER));

        DepartmentDashboardRepresentation department1Dashboard2 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(1L)
                .setCountThisYear(1L)
                .setCountAllTime(1L)
                .setMostRecent(member1.getCreatedTimestamp()),
            department1Dashboard2.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department1Dashboard2.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard2 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard2.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard2.getPostStatistics());

        UserRoleRepresentation member2 = departmentUserApi.createUserRole(departmentId1,
            new UserRoleDTO()
                .setUser(new UserDTO().setGivenName("two").setSurname("two").setEmail("two@two.com"))
                .setRole(Role.MEMBER));

        DepartmentDashboardRepresentation department1Dashboard3 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(2L)
                .setCountThisYear(2L)
                .setCountAllTime(2L)
                .setMostRecent(member2.getCreatedTimestamp()),
            department1Dashboard3.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department1Dashboard3.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard3 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard3.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard3.getPostStatistics());

        Long memberId3 = testUserService.authenticate().getId();
        departmentUserApi.createMembershipRequest(departmentId1,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2015));

        Long memberId4 = testUserService.authenticate().getId();
        departmentUserApi.createMembershipRequest(departmentId1,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MASTER_STUDENT).setMemberProgram("program").setMemberYear(2016));

        testUserService.setAuthentication(departmentUserId);
        UserRolesRepresentation userRoles = departmentUserApi.getUserRoles(departmentId1, null);
        LocalDateTime member3CreatedTimestamp = userRoles.getMemberRequests().stream()
            .filter(userRole -> userRole.getUser().getId().equals(memberId3))
            .findFirst().get().getCreatedTimestamp();

        LocalDateTime member4CreatedTimestamp = userRoles.getMemberRequests().stream()
            .filter(userRole -> userRole.getUser().getId().equals(memberId4))
            .findFirst().get().getCreatedTimestamp();

        DepartmentDashboardRepresentation department1Dashboard4 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(4L)
                .setCountThisYear(4L)
                .setCountAllTime(4L)
                .setMostRecent(member4CreatedTimestamp),
            department1Dashboard4.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department1Dashboard4.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard4 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard4.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard4.getPostStatistics());

        testUserService.setAuthentication(departmentUserId);
        departmentUserApi.reviewMembershipRequest(departmentId1, memberId3, "accepted");
        departmentUserApi.reviewMembershipRequest(departmentId1, memberId4, "rejected");

        DepartmentDashboardRepresentation department1Dashboard5 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(3L)
                .setCountThisYear(3L)
                .setCountAllTime(3L)
                .setMostRecent(member3CreatedTimestamp),
            department1Dashboard5.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department1Dashboard5.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard5 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard5.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard5.getPostStatistics());

        testUserService.authenticate();
        PostRepresentation post = postApi.postPost(department1Dashboard1.getBoards().get(0).getId(),
            TestHelper.smallSamplePost()
                .setMemberCategories(Collections.singletonList(UNDERGRADUATE_STUDENT))
                .setPostCategories(Collections.singletonList("Employment")));
        Long postId = post.getId();

        testUserService.setAuthentication(departmentUserId);
        DepartmentDashboardRepresentation department1Dashboard6 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(3L)
                .setCountThisYear(3L)
                .setCountAllTime(3L)
                .setMostRecent(member3CreatedTimestamp),
            department1Dashboard6.getMemberStatistics());

        Assert.assertEquals(
            new PostStatisticsRepresentation()
                .setCountLive(0L)
                .setCountThisYear(1L)
                .setCountAllTime(1L)
                .setMostRecent(post.getCreatedTimestamp())
                .setViewCountLive(0L)
                .setViewCountThisYear(0L)
                .setViewCountAllTime(0L)
                .setMostRecentView(null)
                .setReferralCountLive(0L)
                .setReferralCountThisYear(0L)
                .setReferralCountAllTime(0L)
                .setMostRecentReferral(null)
                .setResponseCountLive(0L)
                .setResponseCountThisYear(0L)
                .setResponseCountAllTime(0L)
                .setMostRecentResponse(null),
            department1Dashboard6.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard6 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard6.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard6.getPostStatistics());

        testUserService.setAuthentication(departmentUserId);
        postApi.executeActionOnPost(postId, "accept", new PostPatchDTO());
        postService.publishAndRetirePosts(LocalDateTime.now());

        DepartmentDashboardRepresentation department1Dashboard7 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(3L)
                .setCountThisYear(3L)
                .setCountAllTime(3L)
                .setMostRecent(member3CreatedTimestamp),
            department1Dashboard7.getMemberStatistics());

        Assert.assertEquals(
            new PostStatisticsRepresentation()
                .setCountLive(1L)
                .setCountThisYear(1L)
                .setCountAllTime(1L)
                .setMostRecent(post.getCreatedTimestamp())
                .setViewCountLive(0L)
                .setViewCountThisYear(0L)
                .setViewCountAllTime(0L)
                .setMostRecentView(null)
                .setReferralCountLive(0L)
                .setReferralCountThisYear(0L)
                .setReferralCountAllTime(0L)
                .setMostRecentReferral(null)
                .setResponseCountLive(0L)
                .setResponseCountThisYear(0L)
                .setResponseCountAllTime(0L)
                .setMostRecentResponse(null),
            department1Dashboard7.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard7 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard7.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard7.getPostStatistics());

        postApi.executeActionOnPost(postId, "reject", new PostPatchDTO().setComment("Not acceptable"));

        DepartmentDashboardRepresentation department1Dashboard8 = departmentApi.getDepartmentDashboard(departmentId1);
        Assert.assertEquals(
            new StatisticsRepresentation<>()
                .setCountLive(3L)
                .setCountThisYear(3L)
                .setCountAllTime(3L)
                .setMostRecent(member3CreatedTimestamp),
            department1Dashboard8.getMemberStatistics());

        Assert.assertEquals(
            new PostStatisticsRepresentation()
                .setCountLive(0L)
                .setCountThisYear(1L)
                .setCountAllTime(1L)
                .setMostRecent(post.getCreatedTimestamp())
                .setViewCountLive(0L)
                .setViewCountThisYear(0L)
                .setViewCountAllTime(0L)
                .setMostRecentView(null)
                .setReferralCountLive(0L)
                .setReferralCountThisYear(0L)
                .setReferralCountAllTime(0L)
                .setMostRecentReferral(null)
                .setResponseCountLive(0L)
                .setResponseCountThisYear(0L)
                .setResponseCountAllTime(0L)
                .setMostRecentResponse(null),
            department1Dashboard8.getPostStatistics());

        DepartmentDashboardRepresentation department2Dashboard8 = departmentApi.getDepartmentDashboard(departmentId2);
        Assert.assertEquals(emptyMemberStatistics, department2Dashboard8.getMemberStatistics());
        Assert.assertEquals(emptyPostStatistics, department2Dashboard8.getPostStatistics());
    }

    @Test
    public void shouldAddAndRemoveRoles() {
        User user = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();

        List<UserRoleRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // add ADMINISTRATOR role
        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation resourceManager = departmentUserApi.createUserRole(departmentId, new UserRoleDTO().setUser(newUser).setRole(Role.ADMINISTRATOR));
        users = departmentUserApi.getUserRoles(departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(BoardUtils.obfuscateEmail("board@mail.com"))).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // replace with MEMBER role
        UserRoleRepresentation resourceUser = departmentUserApi.updateUserRole(departmentId, resourceManager.getUser().getId(),
            new UserRoleDTO().setUser(newUser).setRole(MEMBER).setMemberCategory(MASTER_STUDENT));
        verifyContains(Collections.singletonList(resourceUser), new UserRoleRepresentation().setUser(
            new UserRepresentation().setEmail(BoardUtils.obfuscateEmail("board@mail.com"))).setRole(MEMBER).setState(State.ACCEPTED));

        // remove from resource
        departmentUserApi.deleteUserRoles(departmentId, resourceManager.getUser().getId());
        users = departmentUserApi.getUserRoles(departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldNotRemoveLastAdminRole() {
        User creator = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("last-admin-role").setSummary("last-admin-role summary")).getId();

        // add another administrator
        UserDTO newUserDTO = new UserDTO().setEmail("last-admin-role@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation boardManager = departmentUserApi.createUserRole(departmentId,
            new UserRoleDTO().setUser(newUserDTO).setRole(Role.ADMINISTRATOR));

        // remove current user as administrator
        departmentUserApi.deleteUserRoles(departmentId, creator.getId());

        // authenticate as another administrator
        User newUser = userCacheService.findOneFresh(boardManager.getUser().getId());
        testUserService.setAuthentication(newUser.getId());

        // try to remove yourself as administrator
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.updateUserRole(departmentId, boardManager.getUser().getId(),
                new UserRoleDTO().setUser(newUserDTO).setRole(MEMBER).setMemberCategory(MASTER_STUDENT)),
            ExceptionCode.IRREMOVABLE_USER_ROLE);

        List<UserRoleRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(BoardUtils.obfuscateEmail(newUserDTO.getEmail()))).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldNotRemoveLastAdminUser() {
        User creator = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("last-admin-user").setSummary("last-admin-user summary")).getId();
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.deleteUserRoles(departmentId, creator.getId()), ExceptionCode.IRREMOVABLE_USER);

        List<UserRoleRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(creator.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldNotAddUserWithNotExistingMemberCategory() {
        testUserService.authenticate();
        Long universityId =
            universityService.getOrCreateUniversity("University College London", "ucl").getId();

        Long departmentId = departmentApi.createDepartment(universityId,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary")).getId();

        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");

        // try to add a user to a board
        departmentApi.updateDepartment(departmentId,
            new DepartmentPatchDTO().setMemberCategories(
                Optional.of(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))));
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.createUserRole(departmentId,
                new UserRoleDTO().setUser(newUser).setRole(MEMBER).setMemberCategory(RESEARCH_STUDENT)),
            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try to add a user to a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.createUserRole(departmentId,
                new UserRoleDTO().setUser(newUser).setRole(MEMBER).setMemberCategory(RESEARCH_STUDENT)),
            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
    }

    @Test
    public void shouldNotAddUserRoleWithUnactivatedMemberCategory() {
        User user = testUserService.authenticate();
        Long userId = user.getId();

        Long universityId =
            universityService.getOrCreateUniversity("University College London", "ucl").getId();

        Long departmentId = departmentApi.createDepartment(universityId,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary")).getId();

        // try with a board
        departmentApi.updateDepartment(departmentId,
            new DepartmentPatchDTO().setMemberCategories(Optional.of(Arrays.asList(UNDERGRADUATE_STUDENT, MASTER_STUDENT))));
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.updateUserRole(departmentId, userId,
                new UserRoleDTO().setRole(MEMBER).setMemberCategory(RESEARCH_STUDENT)),
            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try with a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentUserApi.updateUserRole(departmentId, userId,
                new UserRoleDTO().setRole(MEMBER).setMemberCategory(RESEARCH_STUDENT)),
            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
    }

    @Test
    @Sql("classpath:data/user_autosuggest_setup.sql")
    public void shouldGetSimilarUsers() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();

        List<UserRepresentation> userRs = departmentUserApi.findUsers(departmentId, "alas");
        Assert.assertEquals(3, userRs.size());
        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(1));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(2));

        userRs = departmentUserApi.findUsers(departmentId, "knowles");
        Assert.assertEquals(3, userRs.size());
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));
        verifySuggestedUser("jakub", "knowles", "jakub@knowles.com", userRs.get(2));

        userRs = departmentUserApi.findUsers(departmentId, "alastair fib");
        Assert.assertEquals(1, userRs.size());
        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));

        userRs = departmentUserApi.findUsers(departmentId, "alastair knowles");
        Assert.assertEquals(2, userRs.size());
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));

        userRs = departmentUserApi.findUsers(departmentId, "alastair@kno");
        Assert.assertEquals(2, userRs.size());
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));

        userRs = departmentUserApi.findUsers(departmentId, "alastair@fib");
        Assert.assertEquals(1, userRs.size());
        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));

        userRs = departmentUserApi.findUsers(departmentId, "min");
        Assert.assertEquals(1, userRs.size());
        verifySuggestedUser("juan", "mingo", "juan@mingo.com", userRs.get(0));

        userRs = departmentUserApi.findUsers(departmentId, "xavier");
        Assert.assertEquals(0, userRs.size());

        testUserService.authenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> departmentUserApi.findUsers(departmentId, "alastair"), FORBIDDEN_ACTION, null);

        testUserService.unauthenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> departmentUserApi.findUsers(departmentId, "alastair"), UNAUTHENTICATED_USER, null);
    }

    @Test
    @Sql("classpath:data/resource_filter_setup.sql")
    public void shouldListAndFilterResources() {
        resourceRepository.findAll().stream().sorted((resource1, resource2) -> org.apache.commons.lang3.ObjectUtils.compare(resource1.getId(), resource2.getId()))
            .forEach(resource -> {
                if (Arrays.asList(Scope.UNIVERSITY, Scope.DEPARTMENT, Scope.BOARD).contains(resource.getScope())) {
                    resourceService.setIndexDataAndQuarter(resource);
                } else {
                    postService.setIndexDataAndQuarter((Post) resource);
                }

                resourceRepository.update(resource);
            });

        Long userId = userCacheService.findByEmail("department@administrator.com").getId();
        testUserService.setAuthentication(userId);

        List<BoardRepresentation> boardRs = boardApi.getBoards(null, false, null, null, null);
        Assert.assertEquals(2, boardRs.size());

        List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities", "Housing");

        boardRs = boardApi.getBoards(null, false, null, null, "student");
        Assert.assertEquals(2, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities", "Housing");

        boardRs = boardApi.getBoards(null, false, null, null, "promote work experience");
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        userId = userCacheService.findByEmail("department@author.com").getId();
        testUserService.setAuthentication(userId);

        boardRs = boardApi.getBoards(null, false, null, null, null);
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        boardRs = boardApi.getBoards(null, false, null, null, "student");
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        List<PostRepresentation> postRs = postApi.getPosts(null, false, null, null, null);
        Assert.assertEquals(2, postRs.size());

        List<String> postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer");

        postRs = postApi.getPosts(null, false, null, null, "optimise");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        postRs = postApi.getPosts(null, false, State.REJECTED, null, null);
        Assert.assertEquals(0, postRs.size());

        userId = userCacheService.findByEmail("department@member.com").getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(null, false, null, null, null);
        Assert.assertEquals(3, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");

        postRs = postApi.getPosts(null, false, null, null, "london");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        testUserService.unauthenticate();
        Long boardId = boardService.getBoard("ed/cs/opportunities").getId();

        postRs = postApi.getPosts(boardId, true, null, null, null);
        Assert.assertEquals(3, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");

        postRs = postApi.getPosts(boardId, true, null, null, "london");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        userId = userCacheService.findByEmail("post@administrator.com").getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(boardId, false, null, null, null);
        Assert.assertEquals(7, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Technical Analyst", "Scrum Leader", "Product Manager", "Test Engineer");

        postRs = postApi.getPosts(boardId, false, null, null, "madrid krakow");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Technical Analyst");

        userId = userCacheService.findByEmail("department@administrator.com").getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(boardId, false, null, null, null);
        Assert.assertEquals(9, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Database Engineer", "Java Web Developer", "Technical Analyst", "Scrum Leader",
            "Product Manager", "Test Engineer");

        postRs = postApi.getPosts(boardId, false, State.ACCEPTED, null, "service");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Java Web Developer");

        postRs = postApi.getPosts(boardId, false, State.ACCEPTED, null, "madrid krakow");
        Assert.assertEquals(2, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Java Web Developer", "Technical Analyst");

        ExceptionUtils.verifyException(BoardException.class,
            () -> postApi.getPosts(null, false, State.ARCHIVED, null, null), ExceptionCode.INVALID_RESOURCE_FILTER, null);

        List<String> archiveQuarters = postApi.getPostArchiveQuarters(boardId);
        verifyContains(archiveQuarters, "20164", "20171");

        postRs = postApi.getPosts(null, false, State.ARCHIVED, "20164", null);
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Software Architect");

        postRs = postApi.getPosts(boardId, false, State.ARCHIVED, "20171", "nuts");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Business Analyst");

        postRs = postApi.getPosts(boardId, false, State.ARCHIVED, "20171", "guru");
        Assert.assertEquals(0, postRs.size());
    }

    @Test
    @Sql("classpath:data/user_role_filter_setup.sql")
    public void shouldListAndFilterUserRoles() {
        for (User user : userRepository.findAll()) {
            userCacheService.setIndexData(user);
            userRepository.update(user);
        }

        Long userId = userCacheService.findByEmail("alastair@knowles.com").getId();
        testUserService.setAuthentication(userId);

        Long departmentId = resourceRepository.findByHandle("cs").getId();
        UserRolesRepresentation userRoles = departmentUserApi.getUserRoles(departmentId, null);
        Assert.assertEquals(2, userRoles.getUsers().size());
        Assert.assertEquals(2, userRoles.getMembers().size());
        Assert.assertEquals(2, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"), BoardUtils.obfuscateEmail("jakub@fibinger.com"));

        userRoles = departmentUserApi.getUserRoles(departmentId, "alastair");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"));

        userRoles = departmentUserApi.getUserRoles(departmentId, "alister");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"));

        userRoles = departmentUserApi.getUserRoles(departmentId, "beatriz");
        Assert.assertEquals(0, userRoles.getUsers().size());
        Assert.assertEquals(1, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("beatriz@rodriguez.com"));

        userRoles = departmentUserApi.getUserRoles(departmentId, "felipe");
        Assert.assertEquals(0, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(1, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getMemberRequests().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("felipe@ieder.com"));

        testUserService.unauthenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> departmentUserApi.getUserRoles(departmentId, null), UNAUTHENTICATED_USER, null);
    }

    private Pair<DepartmentRepresentation, DepartmentRepresentation> verifyPostTwoDepartments() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO1 = new DepartmentDTO().setName("department 1").setSummary("department summary");
        DepartmentDTO departmentDTO2 = new DepartmentDTO().setName("department 2").setSummary("department summary");
        DepartmentRepresentation departmentR1 = verifyPostDepartment(universityId, departmentDTO1, "department-1");
        DepartmentRepresentation departmentR2 = verifyPostDepartment(universityId, departmentDTO2, "department-2");
        return Pair.of(departmentR1, departmentR2);
    }

    private DepartmentRepresentation verifyPostDepartment(Long universityId, DepartmentDTO departmentDTO, String expectedHandle) {
        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
        Assert.assertEquals(departmentDTO.getName(), departmentR.getName());
        Assert.assertEquals(expectedHandle, departmentR.getHandle());
        Assert.assertEquals(Optional.ofNullable(departmentDTO.getMemberCategories())
            .orElse(Stream.of(MemberCategory.values()).collect(Collectors.toList())), departmentR.getMemberCategories());

        Department department = departmentService.getDepartment(departmentR.getId());
        University university = universityService.getUniversity(departmentR.getUniversity().getId());

        List<ResourceRelation> parents = resourceRelationRepository.findByResource2(department);
        Assert.assertThat(parents.stream()
            .map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(university, department));
        return departmentR;
    }

    private void verifyPatchDepartment(User user, Long departmentId, DepartmentPatchDTO departmentDTO, State expectedState) {
        testUserService.setAuthentication(user.getId());
        Department department = departmentService.getDepartment(departmentId);
        DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId, departmentDTO);

        Optional<String> nameOptional = departmentDTO.getName();
        Assert.assertEquals(nameOptional == null ? department.getName() : nameOptional.orElse(null), departmentR.getName());

        Optional<String> summaryOptional = departmentDTO.getSummary();
        Assert.assertEquals(summaryOptional == null ? department.getSummary() : summaryOptional.orElse(null), departmentR.getSummary());

        Optional<DocumentDTO> documentLogoOptional = departmentDTO.getDocumentLogo();
        verifyDocument(documentLogoOptional == null ? department.getDocumentLogo() : departmentDTO.getDocumentLogo()
            .orElse(null), departmentR.getDocumentLogo());

        Optional<String> handleOptional = departmentDTO.getHandle();
        Assert.assertEquals(handleOptional == null ? department.getHandle().split("/")[1] : handleOptional.orElse(null), departmentR.getHandle());

        Optional<List<MemberCategory>> memberCategoriesOptional = departmentDTO.getMemberCategories();
        Assert.assertEquals(memberCategoriesOptional == null ? MemberCategory.fromStrings(resourceService.getCategories(department, CategoryType.MEMBER)) :
            memberCategoriesOptional.orElse(new ArrayList<>()), departmentR.getMemberCategories());

        Assert.assertEquals(expectedState, departmentR.getState());
    }

    private void verifyDepartmentActions(User adminUser, Collection<User> unprivilegedUsers, Long boardId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.DEPARTMENT, boardId, operations, PUBLIC_ACTIONS.get(State.ACCEPTED));
        verifyResourceActions(unprivilegedUsers, Scope.DEPARTMENT, boardId, operations, PUBLIC_ACTIONS.get(State.ACCEPTED));
        verifyResourceActions(adminUser, Scope.DEPARTMENT, boardId, operations, ADMIN_ACTIONS.get(State.ACCEPTED));
    }

    private void verifyUnprivilegedDepartmentUser(List<String> departmentNames) {
        TestHelper.verifyResources(
            departmentApi.getDepartments(null, null),
            Collections.emptyList(),
            null);

        TestHelper.verifyResources(
            departmentApi.getDepartments(true, null),
            departmentNames,
            new TestHelper.ExpectedActions()
                .add(Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED))));
    }

    private void verifyPrivilegedDepartmentUser(List<String> departmentNames, List<String> adminDepartmentNames) {
        List<Action> adminActions = Lists.newArrayList(ADMIN_ACTIONS.get(State.ACCEPTED));

        TestHelper.verifyResources(
            departmentApi.getDepartments(null, null),
            adminDepartmentNames,
            new TestHelper.ExpectedActions()
                .addAll(adminDepartmentNames, adminActions));

        TestHelper.verifyResources(
            departmentApi.getDepartments(true, null),
            departmentNames,
            new TestHelper.ExpectedActions()
                .add(Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED)))
                .addAll(adminDepartmentNames, adminActions));
    }

    private void verifySuggestedDepartment(String expectedName, DepartmentRepresentation departmentR) {
        Assert.assertEquals(expectedName, departmentR.getName());

        String departmentIdString = departmentR.getId().toString();
        DocumentRepresentation documentLogoR = departmentR.getDocumentLogo();
        Assert.assertEquals(departmentIdString, documentLogoR.getCloudinaryId());
        Assert.assertEquals(departmentIdString, documentLogoR.getCloudinaryUrl());
        Assert.assertEquals(departmentIdString, documentLogoR.getFileName());
    }

    private void verifyMember(String expectedEmail, LocalDate expectedExpiryDate, MemberCategory expectedMemberCategory, UserRoleRepresentation actual) {
        Assert.assertEquals(BoardUtils.obfuscateEmail(expectedEmail), actual.getUser().getEmail());
        Assert.assertEquals(expectedExpiryDate, actual.getExpiryDate());
        Assert.assertEquals(expectedMemberCategory, actual.getMemberCategory());
    }

    private void verifyNewDepartmentBoards(Long departmentId) {
        List<BoardRepresentation> boardRs = boardApi.getBoards(departmentId, null, null, null, null);
        Assert.assertEquals(boardRs.size(), 2);

        BoardRepresentation boardR1 = boardRs.get(0);
        Assert.assertEquals("Career Opportunities", boardR1.getName());
        Assert.assertEquals("career-opportunities", boardR1.getHandle());
        Assert.assertEquals(ImmutableList.of("Employment", "Internship", "Volunteering"), boardR1.getPostCategories());
        Assert.assertEquals(State.ACCEPTED, boardR1.getState());

        BoardRepresentation boardR2 = boardRs.get(1);
        Assert.assertEquals("Research Opportunities", boardR2.getName());
        Assert.assertEquals("research-opportunities", boardR2.getHandle());
        Assert.assertEquals(ImmutableList.of("MRes", "PhD", "Postdoc"), boardR2.getPostCategories());
        Assert.assertEquals(State.ACCEPTED, boardR2.getState());
    }

    private void verifySuggestedUser(String expectedGivenName, String expectedSurname, String expectedEmail, UserRepresentation userR) {
        Assert.assertEquals(expectedGivenName, userR.getGivenName());
        Assert.assertEquals(expectedSurname, userR.getSurname());
        Assert.assertEquals(BoardUtils.obfuscateEmail(expectedEmail), userR.getEmail());

        String userIdString = userR.getId().toString();
        DocumentRepresentation documentImageR = userR.getDocumentImage();
        Assert.assertEquals(userIdString, documentImageR.getCloudinaryId());
        Assert.assertEquals(userIdString, documentImageR.getCloudinaryUrl());
        Assert.assertEquals(userIdString, documentImageR.getFileName());
    }

}
