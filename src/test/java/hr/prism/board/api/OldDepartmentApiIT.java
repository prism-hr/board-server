//package hr.prism.board.api;
//
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.LinkedHashMultimap;
//import com.google.common.collect.Lists;
//import hr.prism.board.ApiTestContext;
//import hr.prism.board.TestHelper;
//import hr.prism.board.domain.*;
//import hr.prism.board.dto.*;
//import hr.prism.board.enums.*;
//import hr.prism.board.enums.Activity;
//import hr.prism.board.exception.*;
//import hr.prism.board.repository.DocumentRepository;
//import hr.prism.board.repository.ResourceTaskRepository;
//import hr.prism.board.representation.*;
//import hr.prism.board.service.DepartmentPaymentService;
//import hr.prism.board.service.ScheduledService;
//import hr.prism.board.service.TestActivityService.ActivityInstance;
//import hr.prism.board.service.TestNotificationService.NotificationInstance;
//import hr.prism.board.utils.ObjectUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.assertj.core.api.Assertions;
//import org.hamcrest.Matchers;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.inject.Inject;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Stream;
//
//import static hr.prism.board.TestHelper.mockHttpServletResponse;
//import static hr.prism.board.enums.Action.*;
//import static hr.prism.board.enums.MemberCategory.*;
//import static hr.prism.board.enums.Notification.*;
//import static hr.prism.board.enums.Role.ADMINISTRATOR;
//import static hr.prism.board.enums.Role.AUTHOR;
//import static hr.prism.board.enums.RoleType.STAFF;
//import static hr.prism.board.enums.State.ACCEPTED;
//import static hr.prism.board.enums.State.DRAFT;
//import static hr.prism.board.exception.ExceptionCode.*;
//import static hr.prism.board.utils.BoardUtils.DATETIME_FORMATTER;
//import static hr.prism.board.utils.BoardUtils.obfuscateEmail;
//import static java.math.BigDecimal.ONE;
//import static java.util.Collections.singletonList;
//import static java.util.stream.Collectors.toList;
//import static org.apache.commons.lang3.ObjectUtils.compare;
//
//@ApiTestContext
//@RunWith(SpringRunner.class)
//public class DepartmentApiIT extends AbstractIT {
//
//    private static LinkedHashMultimap<State, Action> ADMIN_ACTIONS = LinkedHashMultimap.create();
//    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();
//
//    static {
//        ADMIN_ACTIONS.putAll(ACCEPTED, Arrays.asList(VIEW, EDIT, EXTEND, SUBSCRIBE));
//        PUBLIC_ACTIONS.putAll(ACCEPTED, singletonList(VIEW));
//    }
//
//    @Value("${resource.task.notification.interval1.seconds}")
//    private Long resourceTaskNotificationInterval1Seconds;
//
//    @Value("${resource.task.notification.interval2.seconds}")
//    private Long resourceTaskNotificationInterval2Seconds;
//
//    @Value("${resource.task.notification.interval3.seconds}")
//    private Long resourceTaskNotificationInterval3Seconds;
//
//    @Value("${department.draft.expiry.seconds}")
//    private Long departmentDraftExpirySeconds;
//
//    @Value("${department.pending.expiry.seconds}")
//    private Long departmentPendingExpirySeconds;
//
//    @Value("${department.pending.notification.interval1.seconds}")
//    private Long departmentPendingNotificationInterval1Seconds;
//
//    @Value("${department.pending.notification.interval2.seconds}")
//    private Long departmentPendingNotificationInterval2Seconds;
//
//    @Inject
//    private DocumentRepository documentRepository;
//
//    @Inject
//    private ResourceTaskRepository resourceTaskRepository;
//
//    @Inject
//    private DepartmentPaymentService departmentPaymentService;
//
//    @Inject
//    private ScheduledService scheduledService;
//
//    @Test
//    public void shouldCreateDepartment() {
//        testUserService.authenticate();
//        University university = universityService.getOrCreateUniversity("University College London", "ucl");
//        Long universityId = university.getId();
//
//        Document documentLogo = new Document();
//        documentLogo.setCloudinaryId("c");
//        documentLogo.setCloudinaryUrl("u");
//        documentLogo.setFileName("f");
//
//        documentRepository.save(documentLogo);
//        university.setDocumentLogo(documentLogo);
//        universityRepository.save(university);
//
//        DepartmentDTO department =
//            new DepartmentDTO()
//                .setName("department")
//                .setSummary("summary");
//
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, department);
//        Long departmentId = departmentR.getId();
//
//        String departmentName = department.getName();
//        Assert.assertEquals(departmentName, departmentR.getName());
//        Assert.assertEquals(departmentName, departmentR.getHandle());
//        Assert.assertEquals("summary", departmentR.getSummary());
//        Assert.assertEquals(Stream.of(MemberCategory.values()).collect(toList()), departmentR.getMemberCategories());
//        Assert.assertEquals(State.DRAFT, departmentR.getState());
//
//        DocumentRepresentation documentR = departmentR.getDocumentLogo();
//        Assert.assertEquals("c", documentR.getCloudinaryId());
//        Assert.assertEquals("u", documentR.getCloudinaryUrl());
//        Assert.assertEquals("f", documentR.getFileName());
//
//        verifyNewDepartmentBoards(departmentId);
//        ExceptionUtils.verifyDuplicateException(() -> departmentApi.createDepartment(universityId, department), ExceptionCode.DUPLICATE_RESOURCE, departmentId, null);
//    }
//
//    @Test
//    public void shouldCreateDepartmentOverridingDefaults() {
//        testUserService.authenticate();
//        University university = universityService.getOrCreateUniversity("University College London", "ucl");
//        Long universityId = university.getId();
//
//        DepartmentDTO department =
//            new DepartmentDTO()
//                .setName("department")
//                .setSummary("summary")
//                .setDocumentLogo(
//                    new DocumentDTO()
//                        .setCloudinaryId("d")
//                        .setCloudinaryUrl("v")
//                        .setFileName("g"))
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT));
//
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, department);
//        Long departmentId = departmentR.getId();
//
//        String departmentName = department.getName();
//        Assert.assertEquals(departmentName, departmentR.getName());
//        Assert.assertEquals(departmentName, departmentR.getHandle());
//        Assert.assertEquals("summary", departmentR.getSummary());
//        Assert.assertEquals(singletonList(UNDERGRADUATE_STUDENT), departmentR.getMemberCategories());
//        Assert.assertEquals(State.DRAFT, departmentR.getState());
//
//        DocumentRepresentation documentR = departmentR.getDocumentLogo();
//        Assert.assertEquals("d", documentR.getCloudinaryId());
//        Assert.assertEquals("v", documentR.getCloudinaryUrl());
//        Assert.assertEquals("g", documentR.getFileName());
//
//        verifyNewDepartmentBoards(departmentId);
//    }
//
//    @Test
//    public void shouldCreateAndListDepartments() {
//        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        User user1 = testUserService.authenticate();
//        DepartmentDTO departmentDTO1 = new DepartmentDTO().setName("department1").setSummary("department summary");
//        DepartmentRepresentation departmentR1 = verifyPostDepartment(universityId, departmentDTO1, "department1");
//
//        Long departmentId1 = departmentR1.getId();
//        Long boardId1 = boardApi.getBoards(departmentId1, null, null, null, null).get(0).getId();
//        unprivilegedUsers.put("department1", makeUnprivilegedUsers(boardId1, 10,
//            TestHelper.samplePost().setPostCategories(singletonList("Employment"))));
//
//        testUserService.setAuthentication(user1);
//        DepartmentDTO departmentDTO2 = new DepartmentDTO().setName("department2").setSummary("department summary");
//        DepartmentRepresentation departmentR2 = verifyPostDepartment(universityId, departmentDTO2, "department2");
//
//        Long departmentId2 = departmentR2.getId();
//        Long boardId2 = boardApi.getBoards(departmentId2, null, null, null, null).get(0).getId();
//        unprivilegedUsers.put("department2", makeUnprivilegedUsers(boardId2, 20,
//            TestHelper.smallSamplePost()
//                .setPostCategories(singletonList("Employment"))
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT))));
//
//        User user2 = testUserService.authenticate();
//        DepartmentDTO departmentDTO3 = new DepartmentDTO().setName("department3").setSummary("department summary");
//        DepartmentRepresentation departmentR3 = verifyPostDepartment(universityId, departmentDTO3, "department3");
//
//        Long departmentId3 = departmentR3.getId();
//        Long boardId3 = boardApi.getBoards(departmentId3, null, null, null, null).get(0).getId();
//        unprivilegedUsers.put("department3", makeUnprivilegedUsers(boardId3, 30,
//            TestHelper.samplePost().setPostCategories(singletonList("Employment"))));
//
//        testUserService.setAuthentication(user2);
//        DepartmentDTO departmentDTO4 = new DepartmentDTO().setName("department4").setSummary("department summary");
//        DepartmentRepresentation departmentR4 = verifyPostDepartment(universityId, departmentDTO4, "department4");
//
//        Long departmentId4 = departmentR4.getId();
//        Long boardId4 = boardApi.getBoards(departmentId4, null, null, null, null).get(0).getId();
//        unprivilegedUsers.put("department4", makeUnprivilegedUsers(boardId4, 40,
//            TestHelper.smallSamplePost()
//                .setPostCategories(singletonList("Employment"))
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT))));
//
//        List<String> departmentNames = Arrays.asList(
//            "department1", "department10", "department2", "department20", "department3", "department30", "department4", "department40");
//
//        testUserService.unauthenticate();
//        verifyUnprivilegedDepartmentUser(departmentNames);
//
//        for (String departmentName : unprivilegedUsers.keySet()) {
//            Map<Scope, User> unprivilegedUserMap = unprivilegedUsers.get(departmentName);
//            for (Scope scope : unprivilegedUserMap.keySet()) {
//                testUserService.setAuthentication(unprivilegedUserMap.get(scope));
//                if (scope == Scope.DEPARTMENT) {
//                    verifyPrivilegedDepartmentUser(departmentNames, singletonList(departmentName + "0"));
//                } else {
//                    verifyUnprivilegedDepartmentUser(departmentNames);
//                }
//            }
//        }
//
//        testUserService.setAuthentication(user1);
//        verifyPrivilegedDepartmentUser(departmentNames, Arrays.asList("department1", "department2"));
//
//        testUserService.setAuthentication(user2);
//        verifyPrivilegedDepartmentUser(departmentNames, Arrays.asList("department3", "department4"));
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateDepartmentHandle() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        verifyPostDepartment(universityId,
//            new DepartmentDTO().setName("new department with long name").setSummary("department summary"), "new-department-with-long");
//
//        Long departmentId = verifyPostDepartment(universityId,
//            new DepartmentDTO().setName("new department with long name too").setSummary("department summary"), "new-department-with-long-2").getId();
//
//        DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId,
//            new DepartmentPatchDTO()
//                .setHandle(Optional.of("new-department-with-longer")));
//        Assert.assertEquals("new-department-with-longer", departmentR.getHandle());
//
//        verifyPostDepartment(universityId,
//            new DepartmentDTO().setName("new department with long name also").setSummary("department summary"), "new-department-with-long-2");
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
//        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
//        ExceptionUtils.verifyDuplicateException(
//            () -> departmentApi.updateDepartment(departmentRs.getKey().getId(),
//                new DepartmentPatchDTO()
//                    .setName(Optional.of(departmentRs.getValue().getName()))),
//            ExceptionCode.DUPLICATE_RESOURCE, departmentRs.getValue().getId());
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateDepartmentHandlesByUpdating() {
//        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
//        ExceptionUtils.verifyException(
//            BoardDuplicateException.class,
//            () -> departmentApi.updateDepartment(departmentRs.getKey().getId(),
//                new DepartmentPatchDTO()
//                    .setHandle(Optional.of(departmentRs.getValue().getHandle()))),
//            ExceptionCode.DUPLICATE_RESOURCE_HANDLE);
//    }
//
//    @Test
//    public void shouldSupportDepartmentActionsAndPermissions() {
//        // Create department and board
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
//        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
//        Long departmentId = departmentR.getId();
//        Long boardId = boardApi.getBoards(departmentId, null, null, null, null).get(0).getId();
//
//        // Create post
//        User postUser = testUserService.authenticate();
//        postApi.postPost(boardId,
//            TestHelper.smallSamplePost()
//                .setPostCategories(singletonList("Employment"))
//                .setMemberCategories(singletonList(MASTER_STUDENT)));
//
//        // Create unprivileged users
//        List<User> unprivilegedUsers = Lists.newArrayList(
//            makeUnprivilegedUsers(boardId, 2,
//                TestHelper.smallSamplePost()
//                    .setPostCategories(singletonList("Employment"))
//                    .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT)))
//                .values());
//
//        unprivilegedUsers.add(postUser);
//
//        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
//            .put(EDIT, () -> departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO()))
//            .build();
//
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//
//        // Check that we do not audit viewing
//        departmentApi.getDepartment(departmentId);
//
//        // Check that we can make changes and leave nullable values null
//        verifyPatchDepartment(departmentUser, departmentId,
//            new DepartmentPatchDTO()
//                .setName(Optional.of("department 2"))
//                .setHandle(Optional.of("department-2")));
//
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//
//        // Check that we can make further changes and set nullable values
//        verifyPatchDepartment(departmentUser, departmentId,
//            new DepartmentPatchDTO()
//                .setName(Optional.of("department 3"))
//                .setSummary(Optional.of("department 3 summary"))
//                .setHandle(Optional.of("department-3"))
//                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
//                .setMemberCategories(Optional.of(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))));
//
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//
//        // Check that we can make further changes and change nullable values
//        verifyPatchDepartment(departmentUser, departmentId,
//            new DepartmentPatchDTO()
//                .setName(Optional.of("department 4"))
//                .setSummary(Optional.of("department 4 summary"))
//                .setHandle(Optional.of("department-4"))
//                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
//                .setMemberCategories(Optional.of(ImmutableList.of(MASTER_STUDENT, UNDERGRADUATE_STUDENT))));
//
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//
//        // Check that we can clear nullable values
//        verifyPatchDepartment(departmentUser, departmentId,
//            new DepartmentPatchDTO()
//                .setDocumentLogo(Optional.empty())
//                .setMemberCategories(Optional.empty()));
//
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//
//        testUserService.setAuthentication(departmentUser);
//        Long departmentUser2Id =
//            departmentUserApi.createUserRoles(departmentId,
//                new StaffDTO()
//                    .setUser(new UserDTO()
//                        .setGivenName("admin1")
//                        .setSurname("admin1")
//                        .setEmail("admin1@admin1.com"))
//                    .setRoles(singletonList(ADMINISTRATOR))).getUser().getId();
//
//        User departmentUser2 = userService.findByHandle(departmentUser2Id);
//        UserRole department2UserRole = userRoleService.getByResourceUserAndRole(resourceService.findByHandle(departmentId), departmentUser2, ADMINISTRATOR);
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//        testNotificationService.verify(new NotificationInstance(JOIN_DEPARTMENT_NOTIFICATION, departmentUser2,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", "admin1")
//                .put("department", "department 4")
//                .put("resourceRedirect", serverUrl + "/redirect?resource=" + departmentId)
//                .put("invitationUuid", department2UserRole.getUuid())
//                .build()));
//
//        testUserService.setAuthentication(departmentUser);
//        departmentUserApi.updateUserRoles(departmentId, departmentUser2Id, new StaffDTO().setRoles(singletonList(AUTHOR)));
//
//        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
//        testNotificationService.verify();
//
//        testUserService.setAuthentication(departmentUser);
//        List<ResourceOperationRepresentation> resourceOperationRs = departmentApi.getDepartmentOperations(departmentId);
//        Assert.assertEquals(5, resourceOperationRs.size());
//
//        // Operations are returned most recent first - reverse the order to make it easier to test
//        resourceOperationRs = Lists.reverse(resourceOperationRs);
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), EXTEND, departmentUser);
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("name", "department", "department 2")
//                .put("handle", "department", "department-2"));
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("name", "department 2", "department 3")
//                .put("summary", "department summary", "department 3 summary")
//                .put("handle", "department-2", "department-3")
//                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
//                .put("memberCategories",
//                    Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT", "RESEARCH_STUDENT", "RESEARCH_STAFF"),
//                    Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT")));
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("name", "department 3", "department 4")
//                .put("summary", "department 3 summary", "department 4 summary")
//                .put("handle", "department-3", "department-4")
//                .put("documentLogo",
//                    ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"),
//                    ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
//                .put("memberCategories",
//                    Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT"),
//                    Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT")));
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("documentLogo", ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"), null)
//                .put("memberCategories", Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT"), null));
//    }
//
//    @Test
//    public void shouldSupportDepartmentTasks() {
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentUserId = departmentUser.getId();
//
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
//        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
//        Long departmentId = departmentR.getId();
//
//        listenForActivities(departmentUser);
//
//        LocalDateTime resourceTaskCreatedTimestamp =
//            resourceTaskRepository.findByResourceId(departmentId).iterator().next().getCreatedTimestamp();
//
//        String recipient = departmentUser.getGivenName();
//        String resourceTaskRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=tasks";
//
//        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
//            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval1Seconds + 1));
//        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
//
//        testActivityService.verify(departmentUserId,
//            new ActivityInstance(departmentId, Activity.CREATE_TASK_ACTIVITY));
//
//        Department department0 = (Department) resourceService.findByHandle(departmentId);
//        String departmentAdminRole1Uuid = userRoleService.getByResourceUserAndRole(department0, departmentUser, ADMINISTRATOR).getUuid();
//
//        testNotificationService.verify(
//            new NotificationInstance(CREATE_TASK_NOTIFICATION, departmentUser,
//                ImmutableMap.<String, String>builder().put("recipient", recipient)
//                    .put("department", "department")
//                    .put("resourceTask",
//                        "<ul><li>Ready to get started - visit the user management area to build your student list.</li>" +
//                            "<li>Got something to share - create some posts and start sending notifications.</li>" +
//                            "<li>Time to tell the world - go to the badges section to learn about promoting your board on your website.</li></ul>")
//                    .put("resourceTaskRedirect", resourceTaskRedirect)
//                    .put("invitationUuid", departmentAdminRole1Uuid)
//                    .build()));
//
//        DepartmentDashboardRepresentation dashboard1 = departmentApi.getDepartmentDashboard(departmentId);
//        Assert.assertNull(dashboard1.getTasks().get(0).getCompleted());
//        Assert.assertNull(dashboard1.getTasks().get(1).getCompleted());
//        Assert.assertNull(dashboard1.getTasks().get(2).getCompleted());
//
//        departmentUserApi.createMembers(departmentId,
//            singletonList(
//                new MemberDTO()
//                    .setUser(
//                        new UserDTO()
//                            .setGivenName("member")
//                            .setSurname("member")
//                            .setEmail("member@member.com"))
//                    .setEmail("member@member.com")
//                    .setMemberCategory(UNDERGRADUATE_STUDENT)
//                    .setMemberProgram("program")
//                    .setMemberYear(1)
//                    .setExpiryDate(LocalDate.now().plusYears(3))));
//
//        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
//            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval2Seconds + 1));
//
//        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
//        testNotificationService.verify(
//            new NotificationInstance(CREATE_TASK_NOTIFICATION, departmentUser,
//                ImmutableMap.<String, String>builder().put("recipient", recipient)
//                    .put("department", "department")
//                    .put("resourceTask",
//                        "<ul><li>Got something to share - create some posts and start sending notifications.</li>" +
//                            "<li>Time to tell the world - go to the badges section to learn about promoting your board on your website.</li></ul>")
//                    .put("resourceTaskRedirect", resourceTaskRedirect)
//                    .put("invitationUuid", departmentAdminRole1Uuid)
//                    .build()));
//
//        DepartmentDashboardRepresentation dashboard2 = departmentApi.getDepartmentDashboard(departmentId);
//        Assert.assertTrue(dashboard2.getTasks().get(0).getCompleted());
//        Assert.assertNull(dashboard2.getTasks().get(1).getCompleted());
//        Assert.assertNull(dashboard2.getTasks().get(2).getCompleted());
//
//        Long boardId = boardApi.getBoards(departmentId, true, null, null, null).get(0).getId();
//        postApi.postPost(boardId,
//            new PostDTO()
//                .setName("name")
//                .setSummary("summary")
//                .setDescription("description")
//                .setOrganization(
//                    new OrganizationDTO()
//                        .setName("organization name"))
//                .setLocation(
//                    new LocationDTO()
//                        .setName("location")
//                        .setDomicile("GB")
//                        .setGoogleId("google")
//                        .setLatitude(ONE)
//                        .setLongitude(ONE))
//                .setPostCategories(singletonList("Employment"))
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT))
//                .setApplyWebsite("http://www.google.co.uk"));
//
//        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
//            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval3Seconds + 1));
//
//        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
//        testNotificationService.verify(
//            new NotificationInstance(CREATE_TASK_NOTIFICATION, departmentUser,
//                ImmutableMap.<String, String>builder().put("recipient", recipient)
//                    .put("department", "department")
//                    .put("resourceTask",
//                        "<ul><li>Time to tell the world - go to the badges section to learn about promoting your board on your website.</li></ul>")
//                    .put("resourceTaskRedirect", resourceTaskRedirect)
//                    .put("invitationUuid", departmentAdminRole1Uuid)
//                    .build()));
//
//        DepartmentDashboardRepresentation dashboard3 = departmentApi.getDepartmentDashboard(departmentId);
//        Assert.assertTrue(dashboard3.getTasks().get(0).getCompleted());
//        Assert.assertTrue(dashboard3.getTasks().get(1).getCompleted());
//        Assert.assertNull(dashboard3.getTasks().get(2).getCompleted());
//
//        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
//        testNotificationService.verify();
//
//        DepartmentBadgeOptionsDTO optionsDTO = new DepartmentBadgeOptionsDTO();
//        try {
//            departmentApi.getDepartmentBadge(departmentId, objectMapper.writeValueAsString(optionsDTO), mockHttpServletResponse());
//        } catch (IOException e) {
//            throw new Error(e);
//        }
//
//        testActivityService.verify(departmentUserId);
//        DepartmentDashboardRepresentation dashboard4 = departmentApi.getDepartmentDashboard(departmentId);
//        Assert.assertTrue(dashboard4.getTasks().get(0).getCompleted());
//        Assert.assertTrue(dashboard4.getTasks().get(1).getCompleted());
//        Assert.assertTrue(dashboard4.getTasks().get(2).getCompleted());
//
//        LocalDateTime baseline = LocalDateTime.of(2017, 9, 1, 9, 0, 0);
//        LocalDateTime baseline1 = baseline.minusMonths(1).minusDays(1);
//        Department department1 = departmentService.findByHandle(departmentId);
//        department1.setCreatedTimestamp(baseline1);
//        department1.setLastMemberTimestamp(baseline1);
//        department1.setLastTaskCreationTimestamp(baseline.minusYears(1));
//        resourceRepository.save(department1);
//
//        scheduledService.updateDepartmentTasks(baseline);
//        resourceTaskRepository.updateCreatedTimestampByResourceId(departmentId,
//            resourceTaskCreatedTimestamp.minusSeconds(resourceTaskNotificationInterval1Seconds + 1));
//        scheduledService.notifyDepartmentTasks(LocalDateTime.now());
//
//        testActivityService.verify(departmentUserId,
//            new ActivityInstance(departmentId, Activity.UPDATE_TASK_ACTIVITY));
//
//        String resourceUpdateTask =
//            "<ul><li>New students arriving - visit the user management area to update your student list.</li></ul>";
//        testNotificationService.verify(
//            new NotificationInstance(Notification.UPDATE_TASK_NOTIFICATION, departmentUser,
//                ImmutableMap.<String, String>builder().put("recipient", recipient)
//                    .put("department", "department")
//                    .put("resourceTask", resourceUpdateTask)
//                    .put("resourceTaskRedirect", resourceTaskRedirect)
//                    .put("invitationUuid", departmentAdminRole1Uuid)
//                    .build()));
//    }
//
//    @Test
//    public void shouldSupportDepartmentSubscriptions() throws IOException {
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentUserId = departmentUser.getId();
//
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
//        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
//        Assert.assertEquals(State.DRAFT, departmentR.getState());
//        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, SUBSCRIBE);
//
//        // Check notifications not fired for draft state
//        LocalDateTime baseline = LocalDateTime.now();
//        scheduledService.updateDepartmentSubscriptions(baseline);
//
//        Long departmentId = departmentR.getId();
//        Department department = (Department) resourceRepository.findOne(departmentId);
//
//        // Simulate ending the draft stage
//        resourceRepository.updateStateChangeTimestampById(departmentId,
//            department.getStateChangeTimestamp().minusSeconds(departmentDraftExpirySeconds + 1));
//        departmentService.updateSubscriptions(LocalDateTime.now());
//
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(State.PENDING, departmentR.getState());
//        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, SUBSCRIBE);
//
//        listenForActivities(departmentUser);
//
//        String recipient = departmentUser.getGivenName();
//        department = (Department) resourceService.findByHandle(departmentId);
//        Assert.assertNull(department.getNotifiedCount());
//
//        String departmentAdminRoleUuid = userRoleService.getByResourceUserAndRole(department, departmentUser, ADMINISTRATOR).getUuid();
//        String pendingExpiryDeadline = department.getStateChangeTimestamp()
//            .plusSeconds(departmentPendingExpirySeconds).toLocalDate().format(DATETIME_FORMATTER);
//        String accountRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=account";
//
//        // Update subscriptions - check notifications only fired once
//        scheduledService.updateDepartmentSubscriptions(baseline);
//        scheduledService.updateDepartmentSubscriptions(baseline);
//
//        testActivityService.verify(departmentUserId, new ActivityInstance(departmentId, Activity.SUBSCRIBE_DEPARTMENT_ACTIVITY));
//        userActivityApi.dismissActivity(userActivityApi.getActivities().iterator().next().getId());
//        testNotificationService.verify(new NotificationInstance(SUBSCRIBE_DEPARTMENT_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", recipient)
//                .put("pendingExpiryDeadline", pendingExpiryDeadline)
//                .put("department", "department")
//                .put("accountRedirect", accountRedirect)
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        department = (Department) resourceService.findByHandle(departmentId);
//        Assert.assertEquals(new Integer(1), department.getNotifiedCount());
//        LocalDateTime pendingCommencedTimestamp = department.getStateChangeTimestamp();
//
//        // Simulate end of first notification period
//        resourceRepository.updateStateChangeTimestampById(departmentId, pendingCommencedTimestamp.minusSeconds(departmentPendingNotificationInterval1Seconds + 1));
//        departmentService.updateSubscriptions(LocalDateTime.now());
//        department = (Department) resourceService.findByHandle(departmentId);
//
//        pendingExpiryDeadline = department.getStateChangeTimestamp()
//            .plusSeconds(departmentPendingExpirySeconds).toLocalDate().format(DATETIME_FORMATTER);
//
//        // Update subscriptions - check notifications only fired once
//        scheduledService.updateDepartmentSubscriptions(baseline);
//        scheduledService.updateDepartmentSubscriptions(baseline);
//
//        verifyActivitiesEmpty(departmentUser);
//        testNotificationService.verify(new NotificationInstance(SUBSCRIBE_DEPARTMENT_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", recipient)
//                .put("pendingExpiryDeadline", pendingExpiryDeadline)
//                .put("department", "department")
//                .put("accountRedirect", accountRedirect)
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        // Simulate end of second notification period
//        resourceRepository.updateStateChangeTimestampById(departmentId, pendingCommencedTimestamp.minusSeconds(departmentPendingNotificationInterval2Seconds + 1));
//        departmentService.updateSubscriptions(LocalDateTime.now());
//        department = (Department) resourceService.findByHandle(departmentId);
//
//        pendingExpiryDeadline = department.getStateChangeTimestamp()
//            .plusSeconds(departmentPendingExpirySeconds).toLocalDate().format(DATETIME_FORMATTER);
//
//        // Update subscriptions - check notifications only fired once
//        scheduledService.updateDepartmentSubscriptions(baseline);
//        scheduledService.updateDepartmentSubscriptions(baseline);
//
//        verifyActivitiesEmpty(departmentUser);
//        testNotificationService.verify(new NotificationInstance(SUBSCRIBE_DEPARTMENT_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", recipient)
//                .put("pendingExpiryDeadline", pendingExpiryDeadline)
//                .put("department", "department")
//                .put("accountRedirect", accountRedirect)
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        JsonNode customer = departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source");
//        Assert.assertNotNull(customer);
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, SUBSCRIBE, UNSUBSCRIBE);
//        Assert.assertEquals("id", departmentR.getCustomerId());
//    }
//
//    @Test
//    public void shouldRejectDepartmentWhenTrialPeriodEnds() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        // Verify department actions
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
//        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
//        Assert.assertEquals(State.DRAFT, departmentR.getState());
//        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, SUBSCRIBE);
//
//        Long departmentId = departmentR.getId();
//        Department department = (Department) resourceRepository.findOne(departmentId);
//
//        // Verify board actions
//        List<BoardRepresentation> boardRs = boardApi.getBoards(departmentId, null, null, null, null);
//        boardRs.forEach(boardR -> {
//            Assert.assertEquals(ACCEPTED, boardR.getState());
//            Assertions.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//                .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, Action.REJECT);
//        });
//
//        // Create post
//        PostRepresentation postR = postApi.postPost(boardRs.get(0).getId(),
//            TestHelper.smallSamplePost()
//                .setPostCategories(singletonList("Employment"))
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT)));
//        Assert.assertEquals(State.PENDING, postR.getState());
//        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsOnly(VIEW, EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
//        Long postId = postR.getId();
//
//        // Verify post actions
//        postService.publishAndRetirePosts(LocalDateTime.now());
//        postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
//        Assert.assertEquals(ACCEPTED, postR.getState());
//        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsOnly(VIEW, Action.PURSUE, EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
//
//        // Simulate ending the draft stage
//        resourceRepository.updateStateChangeTimestampById(departmentId,
//            department.getStateChangeTimestamp().minusSeconds(departmentDraftExpirySeconds + 1));
//        departmentService.updateSubscriptions(LocalDateTime.now());
//
//        // Verify department actions
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(State.PENDING, departmentR.getState());
//        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, SUBSCRIBE);
//
//        // Verify board actions
//        boardRs = boardApi.getBoards(departmentId, null, null, null, null);
//        boardRs.forEach(boardR -> {
//            Assert.assertEquals(ACCEPTED, boardR.getState());
//            Assertions.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//                .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, Action.REJECT);
//        });
//
//        // Verify post actions
//        postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
//        Assert.assertEquals(ACCEPTED, postR.getState());
//        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsOnly(VIEW, Action.PURSUE, EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
//
//        // Create post
//        PostRepresentation post2R = postApi.postPost(boardRs.get(0).getId(),
//            TestHelper.smallSamplePost()
//                .setName("post2")
//                .setPostCategories(singletonList("Employment"))
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT)));
//        Long post2Id = post2R.getId();
//        Assert.assertEquals(State.PENDING, post2R.getState());
//
//        // Simulate ending the pending stage
//        resourceRepository.updateStateChangeTimestampById(departmentId,
//            department.getStateChangeTimestamp().minusSeconds(departmentPendingExpirySeconds + 1));
//        departmentService.updateSubscriptions(LocalDateTime.now());
//
//        // Verify department actions
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(State.REJECTED, departmentR.getState());
//        Assertions.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsExactlyInAnyOrder(VIEW, EDIT, SUBSCRIBE);
//
//        // Verify board actions
//        boardRs = boardApi.getBoards(departmentId, null, null, null, null);
//        boardRs.forEach(boardR -> {
//            Assert.assertEquals(ACCEPTED, boardR.getState());
//            Assertions.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//                .containsExactlyInAnyOrder(VIEW, EDIT, Action.REJECT);
//        });
//
//        // Verify post actions
//        postService.publishAndRetirePosts(LocalDateTime.now());
//        postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
//        Assert.assertEquals(ACCEPTED, postR.getState());
//        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsOnly(VIEW, EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
//
//        // Verify post 2 actions
//        post2R = postApi.getPost(post2Id, TestHelper.mockHttpServletRequest("address"));
//        Assert.assertEquals(State.PENDING, post2R.getState());
//        Assertions.assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(toList()))
//            .containsOnly(VIEW, EDIT, Action.SUSPEND, Action.REJECT, Action.WITHDRAW);
//    }
//
//    @Test
//    public void shouldSuspendDepartmentWhenPaymentFails() throws IOException {
//        User departmentUser = testUserService.authenticate();
//        Long departmentUserId = departmentUser.getId();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
//        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
//        Assert.assertEquals(State.DRAFT, departmentR.getState());
//        Long departmentId = departmentR.getId();
//
//        departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//        listenForActivities(departmentUser);
//
//        String recipient = departmentUser.getGivenName();
//        Department department = (Department) resourceService.findByHandle(departmentId);
//        String departmentAdminRoleUuid = userRoleService.getByResourceUserAndRole(department, departmentUser, ADMINISTRATOR).getUuid();
//        String accountRedirect = serverUrl + "/redirect?resource=" + departmentId + "&view=account";
//
//        departmentPaymentService.processSubscriptionSuspension("id");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//
//        testActivityService.verify(departmentUserId, new ActivityInstance(departmentId, Activity.SUSPEND_DEPARTMENT_ACTIVITY));
//        userActivityApi.dismissActivity(userActivityApi.getActivities().iterator().next().getId());
//        testNotificationService.verify(new NotificationInstance(Notification.SUSPEND_DEPARTMENT_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", recipient)
//                .put("department", "department")
//                .put("accountRedirect", accountRedirect)
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        departmentPaymentService.processSubscriptionSuspension("id");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//
//        // Second failed payment event should not result in another activity
//        Assertions.assertThat(userActivityApi.getActivities()).isEmpty();
//        testNotificationService.verify(new NotificationInstance(Notification.SUSPEND_DEPARTMENT_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", recipient)
//                .put("department", "department")
//                .put("accountRedirect", accountRedirect)
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source2");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//
//        departmentPaymentApi.setPaymentSourceAsDefault(departmentId, "source2");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//
//        departmentPaymentService.processSubscriptionCancellation("id");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(State.REJECTED, departmentR.getState());
//    }
//
//    @Test
//    public void shouldLeaveDepartmentInSameStateWhenManuallyUnsubscribing() throws IOException {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department").setSummary("department summary");
//        DepartmentRepresentation departmentR = verifyPostDepartment(universityId, departmentDTO, "department");
//        Assert.assertEquals(State.DRAFT, departmentR.getState());
//        Long departmentId = departmentR.getId();
//
//        departmentPaymentApi.addPaymentSourceAndSubscription(departmentId, "source");
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//
//        departmentPaymentApi.cancelSubscription(departmentId);
//        departmentR = departmentApi.getDepartment(departmentId);
//        Assert.assertEquals(ACCEPTED, departmentR.getState());
//    }
//
//    @Test
//    @Sql("classpath:data/department_autosuggest_setup.sql")
//    public void shouldSuggestDepartments() {
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        List<DepartmentRepresentation> departmentRs = departmentApi.findDepartments(universityId, "Computer");
//        Assert.assertEquals(3, departmentRs.size());
//
//        verifySuggestedDepartment("Computer Science Department", departmentRs.get(0));
//        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(1));
//        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(2));
//
//        departmentRs = departmentApi.findDepartments(universityId, "Computer Science Laboratory");
//        Assert.assertEquals(3, departmentRs.size());
//
//        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(0));
//        verifySuggestedDepartment("Computer Science Department", departmentRs.get(1));
//        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(2));
//
//        departmentRs = departmentApi.findDepartments(universityId, "School of Informatics");
//        Assert.assertEquals(1, departmentRs.size());
//
//        verifySuggestedDepartment("School of Informatics", departmentRs.get(0));
//
//        departmentRs = departmentApi.findDepartments(universityId, "Physics");
//        Assert.assertEquals(0, departmentRs.size());
//
//        departmentRs = departmentApi.findDepartments(universityId, "Mathematics");
//        Assert.assertEquals(0, departmentRs.size());
//    }
//
//    @Test
//    public void shouldPostMembers() {
//        User user = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
//
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
//        List<StaffRepresentation> staff = departmentUserApi.getUserRoles(departmentR.getId(), null).getStaff();
//        verifyContains(staff,
//            new StaffRepresentation()
//                .setUser(
//                    new UserRepresentation()
//                        .setEmail(user.getEmailDisplay()))
//                .setRoles(singletonList(ADMINISTRATOR)));
//
//        // add 200 members
//        List<MemberDTO> userRoleDTOs1 = new ArrayList<>();
//        for (int i = 1; i <= 200; i++) {
//            userRoleDTOs1.add(
//                new MemberDTO()
//                    .setUser(
//                        new UserDTO()
//                            .setEmail("bulk" + i + "@mail.com").
//                            setGivenName("Bulk" + i)
//                            .setSurname("User"))
//                    .setMemberCategory(MASTER_STUDENT));
//        }
//
//        departmentUserApi.createMembers(departmentR.getId(), userRoleDTOs1);
//        UserRolesRepresentation response = departmentUserApi.getUserRoles(departmentR.getId(), null);
//        Assert.assertEquals(1, response.getStaff().size());
//        Assert.assertEquals(200, response.getMembers().size());
//        Assert.assertNull(response.getMemberToBeUploadedCount());
//
//        List<MemberDTO> userRoleDTOs2 = new ArrayList<>();
//        for (int i = 101; i <= 300; i++) {
//            userRoleDTOs2.add(
//                new MemberDTO()
//                    .setUser(
//                        new UserDTO()
//                            .setEmail("bulk" + i + "@mail.com")
//                            .setGivenName("Bulk" + i)
//                            .setSurname("User"))
//                    .setMemberCategory(MASTER_STUDENT));
//        }
//
//        departmentUserApi.createMembers(departmentR.getId(), userRoleDTOs2);
//        response = departmentUserApi.getUserRoles(departmentR.getId(), null);
//        Assert.assertEquals(300, response.getMembers().size());
//        Assert.assertNull(response.getMemberToBeUploadedCount());
//    }
//
//    @Test
//    public void shouldUpdateMembersByPosting() {
//        User user = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
//
//        Long departmentId = departmentApi.createDepartment(universityId, departmentDTO).getId();
//        List<StaffRepresentation> staff = departmentUserApi.getUserRoles(departmentId, null).getStaff();
//        verifyContains(staff,
//            new StaffRepresentation()
//                .setUser(
//                    new UserRepresentation()
//                        .setEmail(user.getEmailDisplay()))
//                .setRoles(singletonList(ADMINISTRATOR)));
//
//        List<MemberDTO> userRoleDTOs = new ArrayList<>();
//        userRoleDTOs.add(
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGivenName("alastair")
//                        .setSurname("knowles")
//                        .setEmail("alastair@knowles.com"))
//                .setMemberCategory(UNDERGRADUATE_STUDENT));
//
//        userRoleDTOs.add(
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGivenName("jakub")
//                        .setSurname("fibinger")
//                        .setEmail("jakub@fibinger.com"))
//                .setMemberCategory(UNDERGRADUATE_STUDENT));
//
//        departmentUserApi.createMembers(departmentId, userRoleDTOs);
//        List<MemberRepresentation> members = departmentUserApi.getUserRoles(departmentId, null).getMembers();
//
//        verifyMember("jakub@fibinger.com", null, UNDERGRADUATE_STUDENT, members.get(0));
//        verifyMember("alastair@knowles.com", null, UNDERGRADUATE_STUDENT, members.get(1));
//
//        User member = userRepository.findByEmail("alastair@knowles.com");
//        testUserService.setAuthentication(member);
//
//        userApi.updateUser(new UserPatchDTO().setEmail(Optional.of("alastair@alastair.com")));
//
//        testUserService.setAuthentication(user);
//        userRoleDTOs = new ArrayList<>();
//        userRoleDTOs.add(
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGivenName("alastair")
//                        .setSurname("knowles")
//                        .setEmail("alastair@knowles.com"))
//                .setExpiryDate(LocalDate.of(2018, 7, 1))
//                .setMemberCategory(MASTER_STUDENT));
//
//        departmentUserApi.createMembers(departmentId, userRoleDTOs);
//        members = departmentUserApi.getUserRoles(departmentId, null).getMembers();
//
//        verifyMember("jakub@fibinger.com", null, UNDERGRADUATE_STUDENT, members.get(0));
//        verifyMember("alastair@alastair.com", LocalDate.of(2018, 7, 1), MASTER_STUDENT, members.get(1));
//    }
//
//    @Test
//    public void shouldRequestAndAcceptMembership() {
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
//        Long departmentId = departmentR.getId();
//
//        Long departmentUserId = departmentUser.getId();
//        listenForActivities(departmentUser);
//
//        User boardMember = testUserService.authenticate();
//        departmentUserApi.createMembershipRequest(departmentId,
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGender(Gender.MALE)
//                        .setAgeRange(AgeRange.SIXTYFIVE_PLUS)
//                        .setLocationNationality(
//                            new LocationDTO()
//                                .setName("London, United Kingdom")
//                                .setDomicile("GBR")
//                                .setGoogleId("googleId")
//                                .setLatitude(ONE)
//                                .setLongitude(ONE)))
//                .setMemberCategory(UNDERGRADUATE_STUDENT)
//                .setMemberProgram("program")
//                .setMemberYear(3)
//                .setExpiryDate(LocalDate.now().plusYears(2)));
//
//        Long boardMemberId = boardMember.getId();
//        testActivityService.verify(departmentUserId,
//            new ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));
//
//        Resource department = resourceService.findByHandle(departmentId);
//        String departmentAdminRoleUuid = userRoleService.getByResourceUserAndRole(department, departmentUser, ADMINISTRATOR).getUuid();
//
//        testNotificationService.verify(new NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", departmentUser.getGivenName())
//                .put("department", departmentR.getName())
//                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        testUserService.setAuthentication(boardMember);
//        ExceptionUtils.verifyException(
//            BoardForbiddenException.class,
//            () -> departmentUserApi.reviewMembershipRequest(departmentId, boardMemberId, "accepted"),
//            ExceptionCode.FORBIDDEN_ACTION);
//
//        testUserService.setAuthentication(departmentUser);
//        departmentUserApi.reviewMembershipRequest(departmentId, boardMemberId, "accepted");
//
//        verifyActivitiesEmpty(departmentUser);
//        UserRole userRole = userRoleService.getByResourceUserAndRole(department, boardMember, Role.MEMBER);
//        Assert.assertEquals(ACCEPTED, userRole.getState());
//
//        testUserService.setAuthentication(boardMember);
//
//        ExceptionUtils.verifyException(
//            BoardException.class,
//            () -> departmentUserApi.createMembershipRequest(departmentId,
//                new MemberDTO()
//                    .setMemberCategory(UNDERGRADUATE_STUDENT)
//                    .setExpiryDate(LocalDate.now().plusYears(2))),
//            ExceptionCode.DUPLICATE_PERMISSION);
//    }
//
//    @Test
//    public void shouldRequestAndRejectMembership() {
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
//
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
//        Long departmentId = departmentR.getId();
//
//        Long departmentUserId = departmentUser.getId();
//        listenForActivities(departmentUser);
//
//        User boardMember = testUserService.authenticate();
//        departmentUserApi.createMembershipRequest(departmentId,
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGender(Gender.MALE)
//                        .setAgeRange(AgeRange.FIFTY_SIXTYFOUR)
//                        .setLocationNationality(
//                            new LocationDTO()
//                                .setName("London, United Kingdom")
//                                .setDomicile("GBR")
//                                .setGoogleId("googleId")
//                                .setLatitude(ONE)
//                                .setLongitude(ONE)))
//                .setMemberCategory(UNDERGRADUATE_STUDENT)
//                .setMemberProgram("program")
//                .setMemberYear(2)
//                .setExpiryDate(LocalDate.now().plusYears(2)));
//
//        Long boardMemberId = boardMember.getId();
//        testActivityService.verify(departmentUserId,
//            new ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));
//
//        Resource department = resourceService.findByHandle(departmentId);
//        String departmentAdminRoleUuid = userRoleService.getByResourceUserAndRole(department, departmentUser, ADMINISTRATOR).getUuid();
//
//        testNotificationService.verify(new NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", departmentUser.getGivenName())
//                .put("department", departmentR.getName())
//                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        testUserService.setAuthentication(departmentUser);
//        departmentUserApi.reviewMembershipRequest(departmentId, boardMemberId, "rejected");
//
//        verifyActivitiesEmpty(departmentUser);
//        UserRole userRole = userRoleService.getByResourceUserAndRole(department, boardMember, Role.MEMBER);
//        Assert.assertEquals(State.REJECTED, userRole.getState());
//
//        testUserService.setAuthentication(boardMember);
//
//        ExceptionUtils.verifyException(
//            BoardForbiddenException.class,
//            () -> departmentUserApi.createMembershipRequest(departmentId,
//                new MemberDTO()
//                    .setMemberCategory(UNDERGRADUATE_STUDENT)
//                    .setExpiryDate(LocalDate.now().plusYears(2))),
//            ExceptionCode.FORBIDDEN_PERMISSION);
//    }
//
//    @Test
//    public void shouldRequestAndDismissMembership() {
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        DepartmentDTO departmentDTO = new DepartmentDTO().setName("department1").setSummary("department summary");
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
//        Long departmentId = departmentR.getId();
//
//        Long departmentUserId = departmentUser.getId();
//        listenForActivities(departmentUser);
//
//        User boardMember = testUserService.authenticate();
//        departmentUserApi.createMembershipRequest(departmentId,
//            new MemberDTO().setUser(
//                new UserDTO()
//                    .setGender(Gender.FEMALE)
//                    .setAgeRange(AgeRange.FIFTY_SIXTYFOUR)
//                    .setLocationNationality(
//                        new LocationDTO()
//                            .setName("London, United Kingdom")
//                            .setDomicile("GBR")
//                            .setGoogleId("googleId")
//                            .setLatitude(ONE)
//                            .setLongitude(ONE)))
//                .setMemberCategory(UNDERGRADUATE_STUDENT)
//                .setMemberProgram("program")
//                .setMemberYear(1)
//                .setExpiryDate(LocalDate.now().plusYears(2)));
//
//        Long boardMemberId = boardMember.getId();
//        testActivityService.verify(departmentUserId,
//            new ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));
//
//        Resource department = resourceService.findByHandle(departmentId);
//        String departmentAdminRoleUuid = userRoleService.getByResourceUserAndRole(department, departmentUser, ADMINISTRATOR).getUuid();
//
//        testNotificationService.verify(new NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, departmentUser,
//            ImmutableMap.<String, String>builder()
//                .put("recipient", departmentUser.getGivenName())
//                .put("department", departmentR.getName())
//                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
//                .put("invitationUuid", departmentAdminRoleUuid)
//                .build()));
//
//        UserRole userRole = userRoleService.getByResourceUserAndRole(department, boardMember, Role.MEMBER);
//        Long activityId = activityService.getByUserRoleAndActivity(userRole, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY).getId();
//
//        testUserService.setAuthentication(departmentUser);
//        userActivityApi.dismissActivity(activityId);
//
//        verifyActivitiesEmpty(departmentUser);
//    }
//
//    @Test
//    public void shouldSupportDepartmentDashboard() {
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        Long departmentId1 = departmentApi.createDepartment(universityId,
//            new DepartmentDTO().setName("department 1").setSummary("department summary")).getId();
//        Long departmentId2 = departmentApi.createDepartment(universityId,
//            new DepartmentDTO().setName("department 2").setSummary("department summary")).getId();
//
//        StatisticsRepresentation emptyMemberStatistics =
//            new StatisticsRepresentation<>()
//                .setCountLive(0L)
//                .setCountThisYear(0L)
//                .setCountAllTime(0L)
//                .setMostRecent(null);
//
//        PostStatisticsRepresentation emptyPostStatistics =
//            new PostStatisticsRepresentation()
//                .setCountLive(0L)
//                .setCountThisYear(0L)
//                .setCountAllTime(0L)
//                .setMostRecent(null)
//                .setViewCountLive(0L)
//                .setViewCountThisYear(0L)
//                .setViewCountAllTime(0L)
//                .setMostRecentView(null)
//                .setReferralCountLive(0L)
//                .setReferralCountThisYear(0L)
//                .setReferralCountAllTime(0L)
//                .setMostRecentReferral(null)
//                .setResponseCountLive(0L)
//                .setResponseCountThisYear(0L)
//                .setResponseCountAllTime(0L)
//                .setMostRecentResponse(null);
//
//        DepartmentDashboardRepresentation department1Dashboard1 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(emptyMemberStatistics, department1Dashboard1.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department1Dashboard1.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard1 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard1.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard1.getPostStatistics());
//
//        UserRoleRepresentation member1 = departmentUserApi.createUserRoles(departmentId1,
//            new MemberDTO()
//                .setUser(new UserDTO().setGivenName("one").setSurname("one").setEmail("one@one.com")));
//
//        DepartmentDashboardRepresentation department1Dashboard2 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(1L)
//                .setCountThisYear(1L)
//                .setCountAllTime(1L)
//                .setMostRecent(userService.findByHandle(member1.getUser().getId()).getCreatedTimestamp()),
//            department1Dashboard2.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department1Dashboard2.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard2 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard2.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard2.getPostStatistics());
//
//        UserRoleRepresentation member2 = departmentUserApi.createUserRoles(departmentId1,
//            new MemberDTO()
//                .setUser(new UserDTO().setGivenName("two").setSurname("two").setEmail("two@two.com")));
//
//        DepartmentDashboardRepresentation department1Dashboard3 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(2L)
//                .setCountThisYear(2L)
//                .setCountAllTime(2L)
//                .setMostRecent(userService.findByHandle(member2.getUser().getId()).getCreatedTimestamp()),
//            department1Dashboard3.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department1Dashboard3.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard3 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard3.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard3.getPostStatistics());
//
//        Long memberId3 = testUserService.authenticate().getId();
//        departmentUserApi.createMembershipRequest(departmentId1,
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGender(Gender.FEMALE)
//                        .setAgeRange(AgeRange.THIRTY_THIRTYNINE)
//                        .setLocationNationality(
//                            new LocationDTO()
//                                .setName("London, United Kingdom")
//                                .setDomicile("GBR")
//                                .setGoogleId("googleId")
//                                .setLatitude(ONE)
//                                .setLongitude(ONE)))
//                .setMemberCategory(UNDERGRADUATE_STUDENT)
//                .setMemberProgram("program")
//                .setMemberYear(2015));
//
//        Long memberId4 = testUserService.authenticate().getId();
//        departmentUserApi.createMembershipRequest(departmentId1,
//            new MemberDTO()
//                .setUser(
//                    new UserDTO()
//                        .setGender(Gender.FEMALE)
//                        .setAgeRange(AgeRange.THIRTY_THIRTYNINE)
//                        .setLocationNationality(
//                            new LocationDTO()
//                                .setName("London, United Kingdom")
//                                .setDomicile("GBR")
//                                .setGoogleId("googleId")
//                                .setLatitude(ONE)
//                                .setLongitude(ONE)))
//                .setMemberCategory(MASTER_STUDENT)
//                .setMemberProgram("program")
//                .setMemberYear(2016));
//
//        testUserService.setAuthentication(departmentUser);
//        UserRolesRepresentation userRoles = departmentUserApi.getUserRoles(departmentId1, null);
//        LocalDateTime member3CreatedTimestamp =
//            userService.findByHandle(
//                userRoles.getMemberRequests()
//                    .stream()
//                    .filter(userRole -> userRole.getUser().getId().equals(memberId3))
//                    .findFirst()
//                    .orElseThrow(Error::new)
//                    .getUser().getId()).getCreatedTimestamp();
//
//        LocalDateTime member4CreatedTimestamp =
//            userService.findByHandle(
//                userRoles.getMemberRequests()
//                    .stream()
//                    .filter(userRole -> userRole.getUser().getId().equals(memberId4))
//                    .findFirst()
//                    .orElseThrow(Error::new)
//                    .getUser().getId()).getCreatedTimestamp();
//
//        DepartmentDashboardRepresentation department1Dashboard4 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(4L)
//                .setCountThisYear(4L)
//                .setCountAllTime(4L)
//                .setMostRecent(member4CreatedTimestamp),
//            department1Dashboard4.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department1Dashboard4.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard4 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard4.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard4.getPostStatistics());
//
//        testUserService.setAuthentication(departmentUser);
//        departmentUserApi.reviewMembershipRequest(departmentId1, memberId3, "accepted");
//        departmentUserApi.reviewMembershipRequest(departmentId1, memberId4, "rejected");
//
//        DepartmentDashboardRepresentation department1Dashboard5 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(3L)
//                .setCountThisYear(3L)
//                .setCountAllTime(3L)
//                .setMostRecent(member3CreatedTimestamp),
//            department1Dashboard5.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department1Dashboard5.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard5 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard5.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard5.getPostStatistics());
//
//        testUserService.authenticate();
//        PostRepresentation post = postApi.postPost(department1Dashboard1.getBoards().get(0).getId(),
//            TestHelper.smallSamplePost()
//                .setMemberCategories(singletonList(UNDERGRADUATE_STUDENT))
//                .setPostCategories(singletonList("Employment")));
//        Long postId = post.getId();
//
//        testUserService.setAuthentication(departmentUser);
//        DepartmentDashboardRepresentation department1Dashboard6 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(3L)
//                .setCountThisYear(3L)
//                .setCountAllTime(3L)
//                .setMostRecent(member3CreatedTimestamp),
//            department1Dashboard6.getMemberStatistics());
//
//        Assert.assertEquals(
//            new PostStatisticsRepresentation()
//                .setCountLive(0L)
//                .setCountThisYear(1L)
//                .setCountAllTime(1L)
//                .setMostRecent(post.getCreatedTimestamp())
//                .setViewCountLive(0L)
//                .setViewCountThisYear(0L)
//                .setViewCountAllTime(0L)
//                .setMostRecentView(null)
//                .setReferralCountLive(0L)
//                .setReferralCountThisYear(0L)
//                .setReferralCountAllTime(0L)
//                .setMostRecentReferral(null)
//                .setResponseCountLive(0L)
//                .setResponseCountThisYear(0L)
//                .setResponseCountAllTime(0L)
//                .setMostRecentResponse(null),
//            department1Dashboard6.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard6 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard6.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard6.getPostStatistics());
//
//        testUserService.setAuthentication(departmentUser);
//        postApi.executeActionOnPost(postId, "accept", new PostPatchDTO());
//        postService.publishAndRetirePosts(LocalDateTime.now());
//
//        DepartmentDashboardRepresentation department1Dashboard7 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(3L)
//                .setCountThisYear(3L)
//                .setCountAllTime(3L)
//                .setMostRecent(member3CreatedTimestamp),
//            department1Dashboard7.getMemberStatistics());
//
//        Assert.assertEquals(
//            new PostStatisticsRepresentation()
//                .setCountLive(1L)
//                .setCountThisYear(1L)
//                .setCountAllTime(1L)
//                .setMostRecent(post.getCreatedTimestamp())
//                .setViewCountLive(0L)
//                .setViewCountThisYear(0L)
//                .setViewCountAllTime(0L)
//                .setMostRecentView(null)
//                .setReferralCountLive(0L)
//                .setReferralCountThisYear(0L)
//                .setReferralCountAllTime(0L)
//                .setMostRecentReferral(null)
//                .setResponseCountLive(0L)
//                .setResponseCountThisYear(0L)
//                .setResponseCountAllTime(0L)
//                .setMostRecentResponse(null),
//            department1Dashboard7.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard7 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard7.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard7.getPostStatistics());
//
//        postApi.executeActionOnPost(postId, "reject", new PostPatchDTO().setComment("Not acceptable"));
//
//        DepartmentDashboardRepresentation department1Dashboard8 = departmentApi.getDepartmentDashboard(departmentId1);
//        Assert.assertEquals(
//            new StatisticsRepresentation<>()
//                .setCountLive(3L)
//                .setCountThisYear(3L)
//                .setCountAllTime(3L)
//                .setMostRecent(member3CreatedTimestamp),
//            department1Dashboard8.getMemberStatistics());
//
//        Assert.assertEquals(
//            new PostStatisticsRepresentation()
//                .setCountLive(0L)
//                .setCountThisYear(1L)
//                .setCountAllTime(1L)
//                .setMostRecent(post.getCreatedTimestamp())
//                .setViewCountLive(0L)
//                .setViewCountThisYear(0L)
//                .setViewCountAllTime(0L)
//                .setMostRecentView(null)
//                .setReferralCountLive(0L)
//                .setReferralCountThisYear(0L)
//                .setReferralCountAllTime(0L)
//                .setMostRecentReferral(null)
//                .setResponseCountLive(0L)
//                .setResponseCountThisYear(0L)
//                .setResponseCountAllTime(0L)
//                .setMostRecentResponse(null),
//            department1Dashboard8.getPostStatistics());
//
//        DepartmentDashboardRepresentation department2Dashboard8 = departmentApi.getDepartmentDashboard(departmentId2);
//        Assert.assertEquals(emptyMemberStatistics, department2Dashboard8.getMemberStatistics());
//        Assert.assertEquals(emptyPostStatistics, department2Dashboard8.getPostStatistics());
//    }
//
//    @Test
//    public void shouldAddAndRemoveRoles() {
//        User user = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId = departmentApi.createDepartment(universityId,
//            new DepartmentDTO()
//                .setName("department")
//                .setSummary("department summary")).getId();
//
//        List<StaffRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getStaff();
//        verifyContains(users, new StaffRepresentation().setUser(new UserRepresentation().setEmail(user.getEmailDisplay())));
//
//        // add ADMINISTRATOR role
//        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");
//        UserRoleRepresentation resourceManager = departmentUserApi.createUserRoles(departmentId,
//            new StaffDTO()
//                .setUser(newUser)
//                .setRoles(singletonList(ADMINISTRATOR)));
//
//        users = departmentUserApi.getUserRoles(departmentId, null).getStaff();
//        verifyContains(users, new StaffRepresentation()
//            .setUser(
//                new UserRepresentation()
//                    .setEmail(user.getEmailDisplay()))
//            .setRoles(singletonList(ADMINISTRATOR)));
//
//        verifyContains(users,
//            new StaffRepresentation()
//                .setUser(
//                    new UserRepresentation()
//                        .setEmail(obfuscateEmail("board@mail.com")))
//                .setRoles(singletonList(ADMINISTRATOR)));
//
//        // replace with MEMBER role
//        MemberRepresentation resourceUser =
//            (MemberRepresentation) departmentUserApi.updateUserRoles(departmentId, resourceManager.getUser().getId(),
//                new MemberDTO()
//                    .setUser(newUser)
//                    .setMemberCategory(MASTER_STUDENT));
//
//        verifyContains(singletonList(resourceUser),
//            new MemberRepresentation()
//                .setUser(
//                    new UserRepresentation()
//                        .setEmail(obfuscateEmail("board@mail.com")))
//                .setState(ACCEPTED));
//
//        // remove from resource
//        departmentUserApi.deleteUserRoles(departmentId, resourceManager.getUser().getId(), RoleType.MEMBER);
//        users = departmentUserApi.getUserRoles(departmentId, null).getStaff();
//        verifyContains(users,
//            new StaffRepresentation()
//                .setUser(
//                    new UserRepresentation()
//                        .setEmail(user.getEmailDisplay()))
//                .setRoles(singletonList(ADMINISTRATOR)));
//    }
//
//    @Test
//    public void shouldNotRemoveLastAdminRole() {
//        User creator = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId = departmentApi.createDepartment(universityId,
//            new DepartmentDTO()
//                .setName("last-admin-role")
//                .setSummary("last-admin-role summary")).getId();
//
//        // add another administrator
//        UserDTO newUserDTO = new UserDTO().setEmail("last-admin-role@mail.com").setGivenName("Sample").setSurname("User");
//        UserRoleRepresentation departmentAdmin =
//            departmentUserApi.createUserRoles(departmentId,
//                new StaffDTO()
//                    .setUser(newUserDTO)
//                    .setRoles(singletonList(ADMINISTRATOR)));
//
//        // remove current user as administrator
//        departmentUserApi.deleteUserRoles(departmentId, creator.getId(), STAFF);
//
//        // authenticate as another administrator
//        User newUser = userService.findByHandle(departmentAdmin.getUser().getId());
//        testUserService.setAuthentication(newUser);
//
//        // try to remove yourself as administrator
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> departmentUserApi.updateUserRoles(departmentId, departmentAdmin.getUser().getId(),
//                new MemberDTO().setUser(newUserDTO).setMemberCategory(MASTER_STUDENT)),
//            ExceptionCode.IRREMOVABLE_USER_ROLE);
//
//        List<StaffRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getStaff();
//        verifyContains(users,
//            new StaffRepresentation()
//                .setUser(
//                    new UserRepresentation()
//                        .setEmail(obfuscateEmail(newUserDTO.getEmail())))
//                .setRoles(singletonList(ADMINISTRATOR)));
//    }
//
//    @Test
//    public void shouldNotRemoveLastAdminUser() {
//        User creator = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("last-admin-user").setSummary("last-admin-user summary")).getId();
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> departmentUserApi.deleteUserRoles(departmentId, creator.getId(), STAFF), ExceptionCode.IRREMOVABLE_USER);
//
//        List<StaffRepresentation> users = departmentUserApi.getUserRoles(departmentId, null).getStaff();
//        verifyContains(users, new StaffRepresentation().setUser(new UserRepresentation()
//            .setEmail(creator.getEmailDisplay())).setRoles(singletonList(ADMINISTRATOR)));
//    }
//
//    @Test
//    public void shouldNotAddUserWithNotExistingMemberCategory() {
//        testUserService.authenticate();
//        Long universityId =
//            universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        Long departmentId = departmentApi.createDepartment(universityId,
//            new DepartmentDTO()
//                .setName("department")
//                .setSummary("department summary")).getId();
//
//        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");
//
//        // try to add a user to a board
//        departmentApi.updateDepartment(departmentId,
//            new DepartmentPatchDTO().setMemberCategories(
//                Optional.of(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))));
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> departmentUserApi.createUserRoles(departmentId,
//                new MemberDTO().setUser(newUser).setMemberCategory(RESEARCH_STUDENT)),
//            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
//
//        // try to add a user to a department
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> departmentUserApi.createUserRoles(departmentId,
//                new MemberDTO().setUser(newUser).setMemberCategory(RESEARCH_STUDENT)),
//            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
//    }
//
//    @Test
//    public void shouldNotAddUserRoleWithUnactivatedMemberCategory() {
//        User user = testUserService.authenticate();
//        Long userId = user.getId();
//
//        Long universityId =
//            universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        Long departmentId = departmentApi.createDepartment(universityId,
//            new DepartmentDTO()
//                .setName("department")
//                .setSummary("department summary")).getId();
//
//        // try with a board
//        departmentApi.updateDepartment(departmentId,
//            new DepartmentPatchDTO().setMemberCategories(Optional.of(Arrays.asList(UNDERGRADUATE_STUDENT, MASTER_STUDENT))));
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> departmentUserApi.updateUserRoles(departmentId, userId,
//                new MemberDTO().setMemberCategory(RESEARCH_STUDENT)),
//            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
//
//        // try with a department
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> departmentUserApi.updateUserRoles(departmentId, userId,
//                new MemberDTO().setMemberCategory(RESEARCH_STUDENT)),
//            INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
//    }
//
//    @Test
//    @Sql("classpath:data/user_autosuggest_setup.sql")
//    public void shouldGetSimilarUsers() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
//
//        List<UserRepresentation> userRs = departmentUserApi.findUsers(departmentId, "alas");
//        Assert.assertEquals(3, userRs.size());
//        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(1));
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(2));
//
//        userRs = departmentUserApi.findUsers(departmentId, "knowles");
//        Assert.assertEquals(3, userRs.size());
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));
//        verifySuggestedUser("jakub", "knowles", "jakub@knowles.com", userRs.get(2));
//
//        userRs = departmentUserApi.findUsers(departmentId, "alastair fib");
//        Assert.assertEquals(1, userRs.size());
//        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));
//
//        userRs = departmentUserApi.findUsers(departmentId, "alastair knowles");
//        Assert.assertEquals(2, userRs.size());
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));
//
//        userRs = departmentUserApi.findUsers(departmentId, "alastair@kno");
//        Assert.assertEquals(2, userRs.size());
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
//        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));
//
//        userRs = departmentUserApi.findUsers(departmentId, "alastair@fib");
//        Assert.assertEquals(1, userRs.size());
//        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));
//
//        userRs = departmentUserApi.findUsers(departmentId, "min");
//        Assert.assertEquals(1, userRs.size());
//        verifySuggestedUser("juan", "mingo", "juan@mingo.com", userRs.get(0));
//
//        userRs = departmentUserApi.findUsers(departmentId, "xavier");
//        Assert.assertEquals(0, userRs.size());
//
//        testUserService.authenticate();
//        ExceptionUtils.verifyException(BoardForbiddenException.class,
//            () -> departmentUserApi.findUsers(departmentId, "alastair"), FORBIDDEN_ACTION, null);
//
//        testUserService.unauthenticate();
//        ExceptionUtils.verifyException(BoardForbiddenException.class,
//            () -> departmentUserApi.findUsers(departmentId, "alastair"), UNAUTHENTICATED_USER, null);
//    }
//
//    @Test
//    @Sql("classpath:data/resource_filter_setup.sql")
//    public void shouldListAndFilterResources() {
//        resourceRepository.findAll()
//            .stream()
//            .sorted((resource1, resource2) -> compare(resource1.getId(), resource2.getId()))
//            .forEach(resource -> {
//                if (Arrays.asList(Scope.UNIVERSITY, Scope.DEPARTMENT, Scope.BOARD).contains(resource.getScope())) {
//                    resourceService.setIndexDataAndQuarter(resource);
//                } else {
//                    postService.setIndexDataAndQuarter((Post) resource);
//                }
//
//                resourceRepository.save(resource);
//            });
//
//        User user = userService.getByEmail("department@administrator.com");
//        testUserService.setAuthentication(user);
//
//        List<BoardRepresentation> boardRs = boardApi.getBoards(null, false, null, null, null);
//        Assert.assertEquals(2, boardRs.size());
//
//        List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(toList());
//        verifyContains(boardNames, "Opportunities", "Housing");
//
//        boardRs = boardApi.getBoards(null, false, null, null, "student");
//        Assert.assertEquals(2, boardRs.size());
//
//        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(toList());
//        verifyContains(boardNames, "Opportunities", "Housing");
//
//        boardRs = boardApi.getBoards(null, false, null, null, "promote work experience");
//        Assert.assertEquals(1, boardRs.size());
//
//        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(toList());
//        verifyContains(boardNames, "Opportunities");
//
//        user = userService.getByEmail("department@author.com");
//        testUserService.setAuthentication(user);
//
//        boardRs = boardApi.getBoards(null, false, null, null, null);
//        Assert.assertEquals(1, boardRs.size());
//
//        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(toList());
//        verifyContains(boardNames, "Opportunities");
//
//        boardRs = boardApi.getBoards(null, false, null, null, "student");
//        Assert.assertEquals(1, boardRs.size());
//
//        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(toList());
//        verifyContains(boardNames, "Opportunities");
//
//        List<PostRepresentation> postRs = postApi.getPosts(null, false, null, null, null);
//        Assert.assertEquals(2, postRs.size());
//
//        List<String> postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Database Engineer", "Java Web Developer");
//
//        postRs = postApi.getPosts(null, false, null, null, "optimise");
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Database Engineer");
//
//        postRs = postApi.getPosts(null, false, State.REJECTED, null, null);
//        Assert.assertEquals(0, postRs.size());
//
//        user = userService.getByEmail("department@member.com");
//        testUserService.setAuthentication(user);
//
//        postRs = postApi.getPosts(null, false, null, null, null);
//        Assert.assertEquals(3, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");
//
//        postRs = postApi.getPosts(null, false, null, null, "london");
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Database Engineer");
//
//        testUserService.unauthenticate();
//        Long boardId = boardService.findByHandle("ed/cs/opportunities").getId();
//
//        postRs = postApi.getPosts(boardId, true, null, null, null);
//        Assert.assertEquals(3, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");
//
//        postRs = postApi.getPosts(boardId, true, null, null, "london");
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Database Engineer");
//
//        user = userService.getByEmail("post@administrator.com");
//        testUserService.setAuthentication(user);
//
//        postRs = postApi.getPosts(boardId, false, null, null, null);
//        Assert.assertEquals(7, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Technical Analyst", "Scrum Leader", "Product Manager", "Test Engineer");
//
//        postRs = postApi.getPosts(boardId, false, null, null, "madrid krakow");
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Technical Analyst");
//
//        user = userService.getByEmail("department@administrator.com");
//        testUserService.setAuthentication(user);
//
//        postRs = postApi.getPosts(boardId, false, null, null, null);
//        Assert.assertEquals(9, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Database Engineer", "Java Web Developer", "Technical Analyst", "Scrum Leader",
//            "Product Manager", "Test Engineer");
//
//        postRs = postApi.getPosts(boardId, false, ACCEPTED, null, "service");
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Java Web Developer");
//
//        postRs = postApi.getPosts(boardId, false, ACCEPTED, null, "madrid krakow");
//        Assert.assertEquals(2, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Java Web Developer", "Technical Analyst");
//
//        ExceptionUtils.verifyException(BoardException.class,
//            () -> postApi.getPosts(null, false, State.ARCHIVED, null, null), ExceptionCode.INVALID_RESOURCE_FILTER, null);
//
//        List<String> archiveQuarters = postApi.getPostArchiveQuarters(boardId);
//        verifyContains(archiveQuarters, "20164", "20171");
//
//        postRs = postApi.getPosts(null, false, State.ARCHIVED, "20164", null);
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Software Architect");
//
//        postRs = postApi.getPosts(boardId, false, State.ARCHIVED, "20171", "nuts");
//        Assert.assertEquals(1, postRs.size());
//
//        postNames = postRs.stream().map(PostRepresentation::getName).collect(toList());
//        verifyContains(postNames, "Business Analyst");
//
//        postRs = postApi.getPosts(boardId, false, State.ARCHIVED, "20171", "guru");
//        Assert.assertEquals(0, postRs.size());
//    }
//
//    @Test
//    @Sql("classpath:data/user_role_filter_setup.sql")
//    public void shouldListAndFilterUserRoles() {
//        for (User user : userRepository.findAll()) {
//            userService.updateUserIndex(user);
//        }
//
//        User user = userService.getByEmail("alastair@knowles.com");
//        testUserService.setAuthentication(user);
//
//        Long departmentId = resourceRepository.findByHandle("cs").getId();
//        UserRolesRepresentation userRoles = departmentUserApi.getUserRoles(departmentId, null);
//        Assert.assertEquals(2, userRoles.getStaff().size());
//        Assert.assertEquals(2, userRoles.getMembers().size());
//        Assert.assertEquals(2, userRoles.getMemberRequests().size());
//        verifyContains(userRoles.getStaff().stream().map(userRole -> userRole.getUser().getEmail()).collect(toList()),
//            obfuscateEmail("alastair@knowles.com"), obfuscateEmail("jakub@fibinger.com"));
//
//        userRoles = departmentUserApi.getUserRoles(departmentId, "alastair");
//        Assert.assertEquals(1, userRoles.getStaff().size());
//        Assert.assertEquals(0, userRoles.getMembers().size());
//        Assert.assertEquals(0, userRoles.getMemberRequests().size());
//        verifyContains(userRoles.getStaff().stream().map(userRole -> userRole.getUser().getEmail()).collect(toList()),
//            obfuscateEmail("alastair@knowles.com"));
//
//        userRoles = departmentUserApi.getUserRoles(departmentId, "alister");
//        Assert.assertEquals(1, userRoles.getStaff().size());
//        Assert.assertEquals(0, userRoles.getMembers().size());
//        Assert.assertEquals(0, userRoles.getMemberRequests().size());
//        verifyContains(userRoles.getStaff().stream().map(userRole -> userRole.getUser().getEmail()).collect(toList()),
//            obfuscateEmail("alastair@knowles.com"));
//
//        userRoles = departmentUserApi.getUserRoles(departmentId, "beatriz");
//        Assert.assertEquals(0, userRoles.getStaff().size());
//        Assert.assertEquals(1, userRoles.getMembers().size());
//        Assert.assertEquals(0, userRoles.getMemberRequests().size());
//        verifyContains(userRoles.getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(toList()),
//            obfuscateEmail("beatriz@rodriguez.com"));
//
//        userRoles = departmentUserApi.getUserRoles(departmentId, "felipe");
//        Assert.assertEquals(0, userRoles.getStaff().size());
//        Assert.assertEquals(0, userRoles.getMembers().size());
//        Assert.assertEquals(1, userRoles.getMemberRequests().size());
//        verifyContains(userRoles.getMemberRequests().stream().map(userRole -> userRole.getUser().getEmail()).collect(toList()),
//            obfuscateEmail("felipe@ieder.com"));
//
//        testUserService.unauthenticate();
//        ExceptionUtils.verifyException(BoardForbiddenException.class,
//            () -> departmentUserApi.getUserRoles(departmentId, null), UNAUTHENTICATED_USER, null);
//    }
//
//    private Pair<DepartmentRepresentation, DepartmentRepresentation> verifyPostTwoDepartments() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        DepartmentDTO departmentDTO1 = new DepartmentDTO().setName("department 1").setSummary("department summary");
//        DepartmentDTO departmentDTO2 = new DepartmentDTO().setName("department 2").setSummary("department summary");
//        DepartmentRepresentation departmentR1 = verifyPostDepartment(universityId, departmentDTO1, "department-1");
//        DepartmentRepresentation departmentR2 = verifyPostDepartment(universityId, departmentDTO2, "department-2");
//        return Pair.of(departmentR1, departmentR2);
//    }
//
//    private DepartmentRepresentation verifyPostDepartment(Long universityId, DepartmentDTO departmentDTO, String expectedHandle) {
//        DepartmentRepresentation departmentR = departmentApi.createDepartment(universityId, departmentDTO);
//        Assert.assertEquals(departmentDTO.getName(), departmentR.getName());
//        Assert.assertEquals(expectedHandle, departmentR.getHandle());
//        Assert.assertEquals(Optional.ofNullable(departmentDTO.getMemberCategories())
//            .orElse(Stream.of(MemberCategory.values()).collect(toList())), departmentR.getMemberCategories());
//
//        Department department = departmentService.findByHandle(departmentR.getId());
//        University university = universityService.findByHandle(departmentR.getUniversity().getId());
//
//        List<ResourceRelation> parents = resourceRelationRepository.findByResource2(department);
//        Assert.assertThat(parents.stream()
//            .map(ResourceRelation::getResource1).collect(toList()), Matchers.containsInAnyOrder(university, department));
//        return departmentR;
//    }
//
//    private void verifyPatchDepartment(User user, Long departmentId, DepartmentPatchDTO departmentDTO) {
//        testUserService.setAuthentication(user);
//        Department department = departmentService.findByHandle(departmentId);
//        DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId, departmentDTO);
//
//        Optional<String> nameOptional = departmentDTO.getName();
//        Assert.assertEquals(nameOptional == null ? department.getName() : nameOptional.orElse(null), departmentR.getName());
//
//        Optional<String> summaryOptional = departmentDTO.getSummary();
//        Assert.assertEquals(summaryOptional == null ? department.getSummary() : summaryOptional.orElse(null), departmentR.getSummary());
//
//        Optional<DocumentDTO> documentLogoOptional = departmentDTO.getDocumentLogo();
//        verifyDocument(documentLogoOptional == null ? department.getDocumentLogo() : departmentDTO.getDocumentLogo()
//            .orElse(null), departmentR.getDocumentLogo());
//
//        Optional<String> handleOptional = departmentDTO.getHandle();
//        Assert.assertEquals(handleOptional == null ? department.getHandle().split("/")[1] : handleOptional.orElse(null), departmentR.getHandle());
//
//        Optional<List<MemberCategory>> memberCategoriesOptional = departmentDTO.getMemberCategories();
//        Assert.assertEquals(memberCategoriesOptional == null ? MemberCategory.fromStrings(resourceService.getCategories(department, CategoryType.MEMBER)) :
//            memberCategoriesOptional.orElse(new ArrayList<>()), departmentR.getMemberCategories());
//
//        Assert.assertEquals(DRAFT, departmentR.getState());
//    }
//
//    private void verifyDepartmentActions(User adminUser, Collection<User> unprivilegedUsers, Long boardId, Map<Action, Runnable> operations) {
//        verifyResourceActions(Scope.DEPARTMENT, boardId, operations, PUBLIC_ACTIONS.get(ACCEPTED));
//        verifyResourceActions(unprivilegedUsers, Scope.DEPARTMENT, boardId, operations, PUBLIC_ACTIONS.get(ACCEPTED));
//        verifyResourceActions(adminUser, Scope.DEPARTMENT, boardId, operations, ADMIN_ACTIONS.get(ACCEPTED));
//    }
//
//    private void verifyUnprivilegedDepartmentUser(List<String> departmentNames) {
//        TestHelper.verifyResources(
//            departmentApi.getDepartments(null, null),
//            Collections.emptyList(),
//            null);
//
//        TestHelper.verifyResources(
//            departmentApi.getDepartments(true, null),
//            departmentNames,
//            new TestHelper.ExpectedActions()
//                .add(Lists.newArrayList(PUBLIC_ACTIONS.get(ACCEPTED))));
//    }
//
//    private void verifyPrivilegedDepartmentUser(List<String> departmentNames, List<String> adminDepartmentNames) {
//        List<Action> adminActions = Lists.newArrayList(ADMIN_ACTIONS.get(ACCEPTED));
//
//        TestHelper.verifyResources(
//            departmentApi.getDepartments(null, null),
//            adminDepartmentNames,
//            new TestHelper.ExpectedActions()
//                .addAll(adminDepartmentNames, adminActions));
//
//        TestHelper.verifyResources(
//            departmentApi.getDepartments(true, null),
//            departmentNames,
//            new TestHelper.ExpectedActions()
//                .add(Lists.newArrayList(PUBLIC_ACTIONS.get(ACCEPTED)))
//                .addAll(adminDepartmentNames, adminActions));
//    }
//
//    private void verifySuggestedDepartment(String expectedName, DepartmentRepresentation departmentR) {
//        Assert.assertEquals(expectedName, departmentR.getName());
//
//        String departmentIdString = departmentR.getId().toString();
//        DocumentRepresentation documentLogoR = departmentR.getDocumentLogo();
//        Assert.assertEquals(departmentIdString, documentLogoR.getCloudinaryId());
//        Assert.assertEquals(departmentIdString, documentLogoR.getCloudinaryUrl());
//        Assert.assertEquals(departmentIdString, documentLogoR.getFileName());
//    }
//
//    private void verifyMember(String expectedEmail, LocalDate expectedExpiryDate, MemberCategory expectedMemberCategory, MemberRepresentation actual) {
//        Assert.assertEquals(obfuscateEmail(expectedEmail), actual.getUser().getEmail());
//        Assert.assertEquals(expectedMemberCategory, actual.getMemberCategory());
//        Assert.assertEquals(expectedExpiryDate, actual.getExpiryDate());
//    }
//
//    private void verifyNewDepartmentBoards(Long departmentId) {
//        List<BoardRepresentation> boardRs = boardApi.getBoards(departmentId, null, null, null, null);
//        Assert.assertEquals(boardRs.size(), 2);
//
//        BoardRepresentation boardR1 = boardRs.get(0);
//        Assert.assertEquals("Career Opportunities", boardR1.getName());
//        Assert.assertEquals("career-opportunities", boardR1.getHandle());
//        Assert.assertEquals(ImmutableList.of("Employment", "Internship", "Volunteering"), boardR1.getPostCategories());
//        Assert.assertEquals(ACCEPTED, boardR1.getState());
//
//        BoardRepresentation boardR2 = boardRs.get(1);
//        Assert.assertEquals("Research Opportunities", boardR2.getName());
//        Assert.assertEquals("research-opportunities", boardR2.getHandle());
//        Assert.assertEquals(ImmutableList.of("MRes", "PhD", "Postdoc"), boardR2.getPostCategories());
//        Assert.assertEquals(ACCEPTED, boardR2.getState());
//    }
//
//    private void verifySuggestedUser(String expectedGivenName, String expectedSurname, String expectedEmail, UserRepresentation userR) {
//        Assert.assertEquals(expectedGivenName, userR.getGivenName());
//        Assert.assertEquals(expectedSurname, userR.getSurname());
//        Assert.assertEquals(obfuscateEmail(expectedEmail), userR.getEmail());
//
//        String userIdString = userR.getId().toString();
//        DocumentRepresentation documentImageR = userR.getDocumentImage();
//        Assert.assertEquals(userIdString, documentImageR.getCloudinaryId());
//        Assert.assertEquals(userIdString, documentImageR.getCloudinaryUrl());
//        Assert.assertEquals(userIdString, documentImageR.getFileName());
//    }
//
//}
