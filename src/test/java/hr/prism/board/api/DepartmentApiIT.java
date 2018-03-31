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
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.*;
import hr.prism.board.repository.DocumentRepository;
import hr.prism.board.repository.ResourceTaskRepository;
import hr.prism.board.representation.*;
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

        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, department);
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
        Assert.assertEquals(
            ImmutableList.of(ResourceTask.CREATE_MEMBER, ResourceTask.CREATE_POST, ResourceTask.DEPLOY_BADGE),
            departmentR.getTasks().stream().map(ResourceTaskRepresentation::getTask).collect(Collectors.toList()));
        ExceptionUtils.verifyDuplicateException(() -> departmentApi.postDepartment(universityId, department), ExceptionCode.DUPLICATE_DEPARTMENT, departmentId, null);
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
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));

        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, department);
        Long departmentId = departmentR.getId();

        String departmentName = department.getName();
        Assert.assertEquals(departmentName, departmentR.getName());
        Assert.assertEquals(departmentName, departmentR.getHandle());
        Assert.assertEquals("summary", departmentR.getSummary());
        Assert.assertEquals(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT), departmentR.getMemberCategories());
        Assert.assertEquals(State.DRAFT, departmentR.getState());

        DocumentRepresentation documentR = departmentR.getDocumentLogo();
        Assert.assertEquals("d", documentR.getCloudinaryId());
        Assert.assertEquals("v", documentR.getCloudinaryUrl());
        Assert.assertEquals("g", documentR.getFileName());

        verifyNewDepartmentBoards(departmentId);
        Assert.assertEquals(
            ImmutableList.of(ResourceTask.CREATE_MEMBER, ResourceTask.CREATE_POST, ResourceTask.DEPLOY_BADGE),
            departmentR.getTasks().stream().map(ResourceTaskRepresentation::getTask).collect(Collectors.toList()));
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
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));

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
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));

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

        DepartmentRepresentation departmentR = departmentApi.patchDepartment(departmentId,
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
            () -> departmentApi.patchDepartment(departmentRs.getKey().getId(),
                new DepartmentPatchDTO()
                    .setName(Optional.of(departmentRs.getValue().getName()))),
            ExceptionCode.DUPLICATE_DEPARTMENT, departmentRs.getValue().getId());
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentHandlesByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        ExceptionUtils.verifyException(
            BoardDuplicateException.class,
            () -> departmentApi.patchDepartment(departmentRs.getKey().getId(),
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
                .setMemberCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));

        // Create unprivileged users
        List<User> unprivilegedUsers = Lists.newArrayList(
            makeUnprivilegedUsers(boardId, 2,
                TestHelper.smallSamplePost()
                    .setPostCategories(Collections.singletonList("Employment"))
                    .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)))
                .values());

        unprivilegedUsers.add(postUser);

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.EDIT, () -> departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO()))
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
                .setMemberCategories(Optional.of(ImmutableList.of(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))),
            State.DRAFT);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and change nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 4"))
                .setSummary(Optional.of("department 4 summary"))
                .setHandle(Optional.of("department-4"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                .setMemberCategories(Optional.of(ImmutableList.of(MemberCategory.MASTER_STUDENT, MemberCategory.UNDERGRADUATE_STUDENT))),
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
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
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
        resourceApi.updateResourceUser(Scope.DEPARTMENT, departmentId, departmentUser2Id, new UserRoleDTO().setRole(Role.AUTHOR));

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

        User departmentUser2 = testUserService.authenticate();
        Long departmentUser2Id = departmentUser2.getId();

        User departmentUser3 = testUserService.authenticate();
        Long departmentUser3Id = departmentUser3.getId();

        testActivityService.record();
        testNotificationService.record();
        listenForActivities(departmentUserId);
        listenForActivities(departmentUser2Id);
        listenForActivities(departmentUser3Id);

        testUserService.setAuthentication(departmentUserId);
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO()
                .setUser(new UserDTO().setId(departmentUser2Id))
                .setRole(Role.ADMINISTRATOR));

        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO()
                .setUser(new UserDTO().setId(departmentUser3Id))
                .setRole(Role.ADMINISTRATOR));

        UserRole departmentUserRole2 =
            userRoleService.findByResourceAndUserAndRole(resourceService.findOne(departmentId), departmentUser2, Role.ADMINISTRATOR);
        UserRole departmentUserRole3 =
            userRoleService.findByResourceAndUserAndRole(resourceService.findOne(departmentId), departmentUser3, Role.ADMINISTRATOR);

        String recipient2 = departmentUser2.getGivenName();
        String recipient3 = departmentUser3.getGivenName();
        testActivityService.verify(departmentUser2Id, new TestActivityService.ActivityInstance(departmentId, Activity.JOIN_DEPARTMENT_ACTIVITY));
        testActivityService.verify(departmentUser3Id, new TestActivityService.ActivityInstance(departmentId, Activity.JOIN_DEPARTMENT_ACTIVITY));
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_NOTIFICATION, userCacheService.findOne(departmentUser2Id),
                ImmutableMap.<String, String>builder().put("recipient", recipient2)
                    .put("department", "department")
                    .put("resourceRedirect", serverUrl + "/redirect?resource=" + departmentId)
                    .put("invitationUuid", departmentUserRole2.getUuid())
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_NOTIFICATION, userCacheService.findOne(departmentUser3Id),
                ImmutableMap.<String, String>builder().put("recipient", recipient3)
                    .put("department", "department")
                    .put("resourceRedirect", serverUrl + "/redirect?resource=" + departmentId)
                    .put("invitationUuid", departmentUserRole3.getUuid())
                    .build()));

        LocalDateTime resourceTaskCreatedTimestamp =
            resourceTaskRepository.findByResourceId(departmentId).iterator().next().getCreatedTimestamp();

        String recipient = departmentUser.getGivenName();
        String resourceTask =
            "<ul><li>Ready to get started - visit the user management area to build your student list.</li>" +
                "<li>Got something to share - create some posts and start sending notifications.</li>" +
                "<li>Time to tell the world - go to the badges section to learn about promoting your page on other websites.</li></ul>";
        String resourceTaskRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=tasks";

        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval1Seconds + 1));
        scheduledService.notifyDepartmentTasks(LocalDateTime.now());

        testActivityService.verify(departmentUserId,
            new TestActivityService.ActivityInstance(departmentId, Activity.CREATE_TASK_ACTIVITY));
        testActivityService.verify(departmentUser2Id,
            new TestActivityService.ActivityInstance(departmentId, Activity.JOIN_DEPARTMENT_ACTIVITY),
            new TestActivityService.ActivityInstance(departmentId, Activity.CREATE_TASK_ACTIVITY));
        testActivityService.verify(departmentUser3Id,
            new TestActivityService.ActivityInstance(departmentId, Activity.JOIN_DEPARTMENT_ACTIVITY),
            new TestActivityService.ActivityInstance(departmentId, Activity.CREATE_TASK_ACTIVITY));

        Department department0 = (Department) resourceService.findOne(departmentId);
        String departmentAdminRole1Uuid = userRoleService.findByResourceAndUserAndRole(department0, departmentUser, Role.ADMINISTRATOR).getUuid();
        String departmentAdminRole2Uuid = userRoleService.findByResourceAndUserAndRole(department0, departmentUser2, Role.ADMINISTRATOR).getUuid();
        String departmentAdminRole3Uuid = userRoleService.findByResourceAndUserAndRole(department0, departmentUser3, Role.ADMINISTRATOR).getUuid();

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", recipient)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole1Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser2,
                ImmutableMap.<String, String>builder().put("recipient", recipient2)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole2Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser3,
                ImmutableMap.<String, String>builder().put("recipient", recipient3)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole3Uuid)
                    .build()));

        DepartmentRepresentation departmentR2 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR2.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR2.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR2.getTasks().get(2).getCompleted());
        departmentApi.putTask(departmentId, departmentR2.getTasks().get(0).getId());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR3 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR3.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR3.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR3.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR4 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR4.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR4.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR4.getTasks().get(2).getCompleted());

        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval2Seconds + 1));
        scheduledService.notifyDepartmentTasks(LocalDateTime.now());

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", recipient)
                    .put("department", "department")
                    .put("resourceTask",
                        "<ul><li>Got something to share - create some posts and start sending notifications.</li>" +
                            "<li>Time to tell the world - go to the badges section to learn about promoting your page on other websites.</li></ul>")
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole1Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser2,
                ImmutableMap.<String, String>builder().put("recipient", recipient2)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole2Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser3,
                ImmutableMap.<String, String>builder().put("recipient", recipient3)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole3Uuid)
                    .build()));

        testUserService.setAuthentication(departmentUserId);
        DepartmentRepresentation departmentR5 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR5.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR5.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR5.getTasks().get(2).getCompleted());
        departmentApi.putTask(departmentId, departmentR5.getTasks().get(1).getId());
        departmentApi.putTask(departmentId, departmentR5.getTasks().get(2).getId());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR6 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR6.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR6.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR6.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR7 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR7.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR7.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR7.getTasks().get(2).getCompleted());

        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval3Seconds + 1));

        testActivityService.verify(departmentUserId);
        List<ActivityRepresentation> activities2 = activityService.getActivities(departmentUser2Id);
        Assert.assertEquals(
            Arrays.asList(Activity.CREATE_TASK_ACTIVITY, Activity.JOIN_DEPARTMENT_ACTIVITY),
            activities2.stream().map(ActivityRepresentation::getActivity).collect(Collectors.toList()));

        List<ActivityRepresentation> activities3 = activityService.getActivities(departmentUser3Id);
        Assert.assertEquals(
            Arrays.asList(Activity.CREATE_TASK_ACTIVITY, Activity.JOIN_DEPARTMENT_ACTIVITY),
            activities3.stream().map(ActivityRepresentation::getActivity).collect(Collectors.toList()));
        scheduledService.notifyDepartmentTasks(LocalDateTime.now());

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser2,
                ImmutableMap.<String, String>builder().put("recipient", recipient2)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole2Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.CREATE_TASK_NOTIFICATION, departmentUser3,
                ImmutableMap.<String, String>builder().put("recipient", recipient3)
                    .put("department", "department")
                    .put("resourceTask", resourceTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole3Uuid)
                    .build()));

        testUserService.setAuthentication(departmentUserId);
        DepartmentRepresentation departmentR8 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR8.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR8.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR8.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR9 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR9.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR9.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR9.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR10 = departmentApi.getDepartment(departmentId);
        Assert.assertFalse(departmentR10.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR10.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR10.getTasks().get(2).getCompleted());

        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
        testNotificationService.verify();
        testUserService.setAuthentication(departmentUserId);

        departmentApi.postMembers(departmentId,
            Collections.singletonList(
                new UserRoleDTO()
                    .setUser(
                        new UserDTO()
                            .setGivenName("member")
                            .setSurname("member")
                            .setEmail("member@member.com"))
                    .setEmail("member@member.com")
                    .setRole(Role.MEMBER)
                    .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT)
                    .setMemberProgram("program")
                    .setMemberYear(1)
                    .setExpiryDate(LocalDate.now().plusYears(3))));

        testUserService.setAuthentication(departmentUserId);
        DepartmentRepresentation departmentR11 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR11.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR11.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR11.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR12 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR12.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR12.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR12.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR13 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR13.getTasks().get(0).getCompleted());
        Assert.assertFalse(departmentR13.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR13.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUserId);
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
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setApplyWebsite("http://www.google.co.uk"));

        testUserService.setAuthentication(departmentUserId);
        DepartmentRepresentation departmentR14 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR14.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR14.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR14.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR15 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR15.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR15.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR15.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR16 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR16.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR16.getTasks().get(1).getCompleted());
        Assert.assertFalse(departmentR16.getTasks().get(2).getCompleted());

        WidgetOptionsDTO optionsDTO = new WidgetOptionsDTO();
        try {
            resourceApi.getResourceBadge(Scope.DEPARTMENT, departmentId, objectMapper.writeValueAsString(optionsDTO), TestHelper.mockHttpServletResponse());
        } catch (IOException e) {
            throw new Error(e);
        }

        testUserService.setAuthentication(departmentUserId);
        DepartmentRepresentation departmentR17 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR17.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR17.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR17.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR18 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR18.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR18.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR18.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR19 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR19.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR19.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR19.getTasks().get(2).getCompleted());
        scheduledService.updateDepartmentTasks(scheduledService.getBaseline());

        testUserService.setAuthentication(departmentUserId);
        DepartmentRepresentation departmentR20 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR20.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR20.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR20.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser2Id);
        DepartmentRepresentation departmentR21 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR21.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR21.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR21.getTasks().get(2).getCompleted());

        testUserService.setAuthentication(departmentUser3Id);
        DepartmentRepresentation departmentR22 = departmentApi.getDepartment(departmentId);
        Assert.assertTrue(departmentR22.getTasks().get(0).getCompleted());
        Assert.assertTrue(departmentR22.getTasks().get(1).getCompleted());
        Assert.assertTrue(departmentR22.getTasks().get(2).getCompleted());

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
        testActivityService.verify(departmentUser2Id,
            new TestActivityService.ActivityInstance(departmentId, Activity.JOIN_DEPARTMENT_ACTIVITY),
            new TestActivityService.ActivityInstance(departmentId, Activity.UPDATE_TASK_ACTIVITY));
        testActivityService.verify(departmentUser3Id,
            new TestActivityService.ActivityInstance(departmentId, Activity.JOIN_DEPARTMENT_ACTIVITY),
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
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.UPDATE_TASK_NOTIFICATION, departmentUser2,
                ImmutableMap.<String, String>builder().put("recipient", recipient2)
                    .put("department", "department")
                    .put("resourceTask", resourceUpdateTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole2Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.UPDATE_TASK_NOTIFICATION, departmentUser3,
                ImmutableMap.<String, String>builder().put("recipient", recipient3)
                    .put("department", "department")
                    .put("resourceTask", resourceUpdateTask)
                    .put("resourceTaskRedirect", resourceTaskRedirect)
                    .put("invitationUuid", departmentAdminRole3Uuid)
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

        JsonNode customer = departmentApi.addPaymentSourceAndSubscription(departmentId, "source");
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
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)));
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
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)));
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

        departmentApi.addPaymentSourceAndSubscription(departmentId, "source");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        testActivityService.record();
        testNotificationService.record();
        listenForActivities(departmentUserId);

        String recipient = departmentUser.getGivenName();
        Department department = (Department) resourceService.findOne(departmentId);
        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(department, departmentUser, Role.ADMINISTRATOR).getUuid();
        String accountRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=account";

        departmentService.processStripeWebhookEvent("id", Action.SUSPEND);
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

        departmentService.processStripeWebhookEvent("id", Action.SUSPEND);
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

        departmentApi.addPaymentSourceAndSubscription(departmentId, "source2");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        departmentApi.setPaymentSourceAsDefault(departmentId, "source2");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        departmentService.processStripeWebhookEvent("id", Action.UNSUBSCRIBE);
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

        departmentApi.addPaymentSourceAndSubscription(departmentId, "source");
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());

        departmentApi.cancelSubscription(departmentId);
        departmentR = departmentApi.getDepartment(departmentId);
        Assert.assertEquals(State.ACCEPTED, departmentR.getState());
    }

    @Test
    @Sql("classpath:data/department_autosuggest_setup.sql")
    public void shouldSuggestDepartments() {
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        List<DepartmentRepresentation> departmentRs = departmentApi.lookupDepartments(universityId, "Computer");
        Assert.assertEquals(3, departmentRs.size());

        verifySuggestedDepartment("Computer Science Department", departmentRs.get(0));
        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(1));
        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(2));

        departmentRs = departmentApi.lookupDepartments(universityId, "Computer Science Laboratory");
        Assert.assertEquals(3, departmentRs.size());

        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(0));
        verifySuggestedDepartment("Computer Science Department", departmentRs.get(1));
        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(2));

        departmentRs = departmentApi.lookupDepartments(universityId, "School of Informatics");
        Assert.assertEquals(1, departmentRs.size());

        verifySuggestedDepartment("School of Informatics", departmentRs.get(0));

        departmentRs = departmentApi.lookupDepartments(universityId, "Physics");
        Assert.assertEquals(0, departmentRs.size());

        departmentRs = departmentApi.lookupDepartments(universityId, "Mathematics");
        Assert.assertEquals(0, departmentRs.size());
    }

    @Test
    public void shouldPostMembers() {
        User user = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");

        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, departmentDTO);
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // add 200 members
        List<UserRoleDTO> userRoleDTOs1 = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            userRoleDTOs1.add(
                new UserRoleDTO()
                    .setUser(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"))
                    .setRole(Role.MEMBER).setMemberCategory(MemberCategory.MASTER_STUDENT));
        }

        departmentApi.postMembers(departmentR.getId(), userRoleDTOs1);
        UserRolesRepresentation response = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null);
        Assert.assertEquals(1, response.getUsers().size());
        Assert.assertEquals(200, response.getMembers().size());
        Assert.assertNull(response.getMemberToBeUploadedCount());

        List<UserRoleDTO> userRoleDTOs2 = new ArrayList<>();
        for (int i = 101; i <= 300; i++) {
            userRoleDTOs2.add(
                new UserRoleDTO()
                    .setUser(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"))
                    .setRole(Role.MEMBER).setMemberCategory(MemberCategory.MASTER_STUDENT));
        }

        departmentApi.postMembers(departmentR.getId(), userRoleDTOs2);
        response = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null);
        Assert.assertEquals(300, response.getMembers().size());
        Assert.assertNull(response.getMemberToBeUploadedCount());

        List<UserRoleDTO> userRoleDTOs3 = new ArrayList<>();
        userRoleDTOs3.add(
            new UserRoleDTO()
                .setUser(new UserDTO().setEmail("bulk301@mail.com").setGivenName("Bulk301").setSurname("User"))
                .setRole(Role.AUTHOR));
        ExceptionUtils.verifyException(BoardException.class,
            () -> departmentApi.postMembers(departmentR.getId(), userRoleDTOs3), ExceptionCode.INVALID_RESOURCE_USER, null);
    }

    @Test
    public void shouldUpdateMembersByPosting() {
        User user = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");

        Long departmentId = departmentApi.postDepartment(universityId, departmentDTO).getId();
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        List<UserRoleDTO> userRoleDTOs = new ArrayList<>();
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@knowles.com"))
            .setRole(Role.MEMBER).setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT));
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("jakub").setSurname("fibinger").setEmail("jakub@fibinger.com"))
            .setRole(Role.MEMBER).setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT));

        departmentApi.postMembers(departmentId, userRoleDTOs);
        List<UserRoleRepresentation> members = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getMembers();

        verifyMember("jakub@fibinger.com", null, MemberCategory.UNDERGRADUATE_STUDENT, members.get(0));
        verifyMember("alastair@knowles.com", null, MemberCategory.UNDERGRADUATE_STUDENT, members.get(1));

        Long memberId = userRepository.findByEmail("alastair@knowles.com").getId();
        testUserService.setAuthentication(memberId);

        userApi.updateUser(new UserPatchDTO().setEmail(Optional.of("alastair@alastair.com")));

        testUserService.setAuthentication(user.getId());
        userRoleDTOs = new ArrayList<>();
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@knowles.com"))
            .setRole(Role.MEMBER).setExpiryDate(LocalDate.of(2018, 7, 1)).setMemberCategory(MemberCategory.MASTER_STUDENT));

        departmentApi.postMembers(departmentId, userRoleDTOs);
        members = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getMembers();

        verifyMember("jakub@fibinger.com", null, MemberCategory.UNDERGRADUATE_STUDENT, members.get(0));
        verifyMember("alastair@alastair.com", LocalDate.of(2018, 7, 1), MemberCategory.MASTER_STUDENT, members.get(1));
    }

    @Test
    public void shouldRequestAndAcceptMembership() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, departmentDTO);
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        User boardMember = testUserService.authenticate();
        departmentApi.postMembershipRequest(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.SIXTYFIVE_PLUS).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT)
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
            () -> departmentApi.putMembershipRequest(departmentId, boardMemberId, "accepted"),
            ExceptionCode.FORBIDDEN_ACTION);

        testUserService.setAuthentication(departmentUserId);
        departmentApi.putMembershipRequest(departmentId, boardMemberId, "accepted");

        verifyActivitiesEmpty(departmentUserId);
        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER);
        Assert.assertEquals(State.ACCEPTED, userRole.getState());

        testActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(boardMemberId);

        ExceptionUtils.verifyException(
            BoardException.class,
            () -> departmentApi.postMembershipRequest(departmentId,
                new UserRoleDTO().setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setExpiryDate(LocalDate.now().plusYears(2))),
            ExceptionCode.DUPLICATE_PERMISSION);
    }

    @Test
    public void shouldRequestAndRejectMembership() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");

        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, departmentDTO);
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        User boardMember = testUserService.authenticate();
        departmentApi.postMembershipRequest(departmentId, new UserRoleDTO()
            .setUser(new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.FIFTY_SIXTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
            .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT)
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
        departmentApi.putMembershipRequest(departmentId, boardMemberId, "rejected");

        verifyActivitiesEmpty(departmentUserId);
        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER);
        Assert.assertEquals(State.REJECTED, userRole.getState());

        testActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(boardMemberId);

        ExceptionUtils.verifyException(
            BoardForbiddenException.class,
            () -> departmentApi.postMembershipRequest(departmentId,
                new UserRoleDTO().setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setExpiryDate(LocalDate.now().plusYears(2))),
            ExceptionCode.FORBIDDEN_PERMISSION);
    }

    @Test
    public void shouldRequestAndDismissMembership() {
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, departmentDTO);
        Long departmentId = departmentR.getId();

        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        User boardMember = testUserService.authenticate();
        departmentApi.postMembershipRequest(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.FIFTY_SIXTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT)
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
    public void shouldCompileStatistics() {
        Long departmentUserId = testUserService.authenticate().getId();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");

        Long departmentId = departmentApi.postDepartment(universityId, departmentDTO).getId();
        departmentApi.postDepartment(universityId, new DepartmentDTO().setName("other department").setSummary("department summary"));
        verifyStatistics(departmentId, 2L, 0L, 0L, 0L);

        Long board1Id = boardApi.postBoard(departmentId, TestHelper.smallSampleBoard().setName("board1")).getId();
        Long board2Id = boardApi.postBoard(departmentId, TestHelper.smallSampleBoard().setName("board2")).getId();
        verifyStatistics(departmentId, 4L, 0L, 0L, 0L);

        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO()
                .setUser(new UserDTO().setGivenName("one").setSurname("one").setEmail("one@one.com"))
                .setRole(Role.MEMBER));
        verifyStatistics(departmentId, 4L, 0L, 0L, 1L);

        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO()
                .setUser(new UserDTO().setGivenName("two").setSurname("two").setEmail("two@two.com"))
                .setRole(Role.MEMBER));
        verifyStatistics(departmentId, 4L, 0L, 0L, 2L);

        Long memberUser1Id = testUserService.authenticate().getId();
        departmentApi.postMembershipRequest(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2015));

        Long memberUser2Id = testUserService.authenticate().getId();
        departmentApi.postMembershipRequest(departmentId, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE)
            .setAgeRange(AgeRange.THIRTY_THIRTYNINE)
            .setLocationNationality(
                new LocationDTO().setName("London, United Kingdom")
                    .setDomicile("GBR")
                    .setGoogleId("googleId")
                    .setLatitude(BigDecimal.ONE)
                    .setLongitude(BigDecimal.ONE)))
            .setMemberCategory(MemberCategory.MASTER_STUDENT).setMemberProgram("program").setMemberYear(2016));

        verifyStatistics(departmentId, 4L, 0L, 0L, 4L);

        testUserService.setAuthentication(departmentUserId);
        departmentApi.putMembershipRequest(departmentId, memberUser1Id, "accepted");

        verifyStatistics(departmentId, 4L, 0L, 0L, 4L);
        departmentApi.putMembershipRequest(departmentId, memberUser2Id, "rejected");

        verifyStatistics(departmentId, 4L, 0L, 0L, 3L);

        testUserService.setAuthentication(departmentUserId);
        boardApi.executeAction(board1Id, "reject", new BoardPatchDTO());
        boardApi.executeAction(board2Id, "reject", new BoardPatchDTO());
        verifyStatistics(departmentId, 2L, 0L, 0L, 3L);

        boardApi.executeAction(board1Id, "restore", new BoardPatchDTO());
        verifyStatistics(departmentId, 3L, 0L, 0L, 3L);

        testUserService.authenticate();
        Long postId = postApi.postPost(board1Id,
            TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))).getId();
        verifyStatistics(departmentId, 3L, 0L, 0L, 3L);

        testUserService.setAuthentication(departmentUserId);
        postApi.executeAction(postId, "accept", new PostPatchDTO());
        postService.publishAndRetirePosts(LocalDateTime.now());
        verifyStatistics(departmentId, 3L, 1L, 1L, 3L);

        postApi.executeAction(postId, "reject", new PostPatchDTO().setComment("Not acceptable"));
        verifyStatistics(departmentId, 3L, 0L, 1L, 3L);
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
        DepartmentRepresentation departmentR = departmentApi.postDepartment(universityId, departmentDTO);
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
        DepartmentRepresentation departmentR = departmentApi.patchDepartment(departmentId, departmentDTO);

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

    private void verifyStatistics(Long departmentId, Long boardCount, Long postCount, Long authorCount, Long memberCount) {
        DepartmentRepresentation departmentR = departmentApi.getDepartment(departmentId);
        TestHelper.verifyNullableCount(postCount, departmentR.getPostCount());
        TestHelper.verifyNullableCount(memberCount, departmentR.getMemberCount());

        List<DepartmentRepresentation> departmentRs = departmentApi.getDepartments(true, null);
        TestHelper.verifyNullableCount(postCount, departmentRs.get(0).getPostCount());
        TestHelper.verifyNullableCount(memberCount, departmentRs.get(0).getMemberCount());

        TestHelper.verifyNullableCount(0L, departmentRs.get(1).getPostCount());
        TestHelper.verifyNullableCount(0L, departmentRs.get(1).getMemberCount());
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

}
