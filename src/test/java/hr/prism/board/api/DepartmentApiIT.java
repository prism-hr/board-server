package hr.prism.board.api;

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
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.TestUserActivityService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.util.ObjectUtils;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
public class DepartmentApiIT extends AbstractIT {

    private static LinkedHashMultimap<State, Action> ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.EXTEND));

        PUBLIC_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EXTEND));
    }

    @Test
    public void shouldCreateDepartment() {
        testUserService.authenticate();
        DepartmentDTO department =
            TestHelper.sampleDepartment()
                .setSummary("summary").setDocumentLogo(
                new DocumentDTO()
                    .setCloudinaryId("c")
                    .setCloudinaryUrl("u")
                    .setFileName("f"));

        DepartmentRepresentation departmentR = departmentApi.postDepartment(department);

        String departmentName = department.getName();
        Assert.assertEquals(departmentName, departmentR.getName());
        Assert.assertEquals(departmentName, departmentR.getHandle());
        Assert.assertEquals("summary", departmentR.getSummary());
        Assert.assertEquals(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT), departmentR.getMemberCategories());

        DocumentRepresentation documentR = departmentR.getDocumentLogo();
        Assert.assertEquals("c", documentR.getCloudinaryId());
        Assert.assertEquals("u", documentR.getCloudinaryUrl());
        Assert.assertEquals("f", documentR.getFileName());

        Long departmentId = departmentR.getId();
        ExceptionUtils.verifyDuplicateException(() -> departmentApi.postDepartment(department), ExceptionCode.DUPLICATE_DEPARTMENT, departmentId, null);
    }

    @Test
    public void shouldCreateAndListDepartments() {
        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();

        User user1 = testUserService.authenticate();
        BoardDTO boardDTO1 = TestHelper.sampleBoard();
        boardDTO1.getDepartment().setName("department1");
        BoardRepresentation boardR1 = verifyPostDepartment(boardDTO1, "department1");
        unprivilegedUsers.put("department1", makeUnprivilegedUsers(boardR1.getDepartment().getId(), boardR1.getId(), 10, 2,
            TestHelper.samplePost()));

        testUserService.setAuthentication(user1.getId());
        BoardDTO boardDTO2 = TestHelper.smallSampleBoard();
        boardDTO2.getDepartment().setName("department2");
        BoardRepresentation boardR2 = verifyPostDepartment(boardDTO2, "department2");
        unprivilegedUsers.put("department2", makeUnprivilegedUsers(boardR2.getDepartment().getId(), boardR2.getId(), 20, 2,
            TestHelper.smallSamplePost()));

        User user2 = testUserService.authenticate();
        BoardDTO boardDTO3 = TestHelper.sampleBoard();
        boardDTO3.getDepartment().setName("department3");
        BoardRepresentation boardR3 = verifyPostDepartment(boardDTO3, "department3");
        unprivilegedUsers.put("department3", makeUnprivilegedUsers(boardR3.getDepartment().getId(), boardR3.getId(), 30, 2,
            TestHelper.samplePost()));

        testUserService.setAuthentication(user2.getId());
        BoardDTO boardDTO4 = TestHelper.smallSampleBoard();
        boardDTO4.getDepartment().setName("department4");
        BoardRepresentation boardR4 = verifyPostDepartment(boardDTO4, "department4");
        unprivilegedUsers.put("department4", makeUnprivilegedUsers(boardR4.getDepartment().getId(), boardR4.getId(), 40, 2,
            TestHelper.smallSamplePost()));

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
        verifyPostDepartment(
            new BoardDTO()
                .setName("new board")
                .setDepartment(new DepartmentDTO()
                    .setName("new department with long name")),
            "new-department-with");

        Long departmentId = verifyPostDepartment(
            new BoardDTO()
                .setName("new board")
                .setDepartment(new DepartmentDTO()
                    .setName("new department with long name too")),
            "new-department-with-2").getDepartment().getId();

        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = departmentApi.patchDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-with-long")));
            Assert.assertEquals("new-department-with-long", departmentR.getHandle());
            return null;
        });

        verifyPostDepartment(
            new BoardDTO()
                .setName("new board")
                .setDepartment(new DepartmentDTO()
                    .setName("new department with long name also")),
            "new-department-with-2");
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyDuplicateException(
                () -> departmentApi.patchDepartment(departmentRs.getKey().getId(),
                    new DepartmentPatchDTO()
                        .setName(Optional.of(departmentRs.getValue().getName()))),
                ExceptionCode.DUPLICATE_DEPARTMENT, departmentRs.getValue().getId(), status);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentHandlesByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(
                BoardException.class,
                () -> departmentApi.patchDepartment(departmentRs.getKey().getId(),
                    new DepartmentPatchDTO()
                        .setHandle(Optional.of(departmentRs.getValue().getHandle()))),
                ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE, status);
            return null;
        });
    }

    @Test
    public void shouldSupportDepartmentLifecycleAndPermissions() throws IOException {
        // Create department and board
        User departmentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.smallSampleBoard();
        BoardRepresentation boardR = verifyPostDepartment(boardDTO, "department");
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();

        User boardUser = testUserService.authenticate();
        Long boardUserId = boardUser.getId();

        listenForNewActivities(boardUserId);
        testUserActivityService.record();

        testUserService.setAuthentication(departmentUser.getId());
        transactionTemplate.execute(status -> resourceApi.createResourceUser(Scope.BOARD, boardId,
            new UserRoleDTO()
                .setUser(new UserDTO().setId(boardUserId))
                .setRole(Role.ADMINISTRATOR)));

        testUserActivityService.verify(boardUserId, new TestUserActivityService.ActivityInstance(boardId, Activity.JOIN_BOARD_ACTIVITY));
        testUserActivityService.stop();

        // Create post
        User postUser = testUserService.authenticate();
        transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost()));

        // Create unprivileged users
        List<User> unprivilegedUsers = Lists.newArrayList(makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.smallSamplePost()).values());
        unprivilegedUsers.add(boardUser);
        unprivilegedUsers.add(postUser);

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.EDIT, () -> departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO()))
            .build();

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we do not audit viewing
        transactionTemplate.execute(status -> departmentApi.getDepartment(departmentId));

        // Check that we can make changes and leave nullable values null
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 2"))
                .setHandle(Optional.of("department-2")),
            State.ACCEPTED);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and set nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 3"))
                .setSummary(Optional.of("department 3 summary"))
                .setHandle(Optional.of("department-3"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
                .setMemberCategories(Optional.of(ImmutableList.of(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))),
            State.ACCEPTED);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and change nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 4"))
                .setSummary(Optional.of("department 4 summary"))
                .setHandle(Optional.of("department-4"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                .setMemberCategories(Optional.of(ImmutableList.of(MemberCategory.MASTER_STUDENT, MemberCategory.UNDERGRADUATE_STUDENT))),
            State.ACCEPTED);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can clear nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setDocumentLogo(Optional.empty())
                .setMemberCategories(Optional.empty()),
            State.ACCEPTED);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        testNotificationService.record();
        testUserService.setAuthentication(departmentUser.getId());
        Long departmentUser2Id = transactionTemplate.execute(status ->
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new UserRoleDTO()
                    .setUser(new UserDTO()
                        .setGivenName("admin1")
                        .setSurname("admin1")
                        .setEmail("admin1@admin1.com"))
                    .setRole(Role.ADMINISTRATOR)).getUser().getId());

        User departmentUser2 = userCacheService.findOne(departmentUser2Id);
        UserRole department2UserRole = userRoleService.findByResourceAndUserAndRole(resourceService.findOne(departmentId), departmentUser2, Role.ADMINISTRATOR);
        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_NOTIFICATION, userCacheService.findOne(departmentUser2Id),
            ImmutableMap.<String, String>builder().put("recipient", "admin1").put("department", "department 4").put("resourceRedirect", serverUrl + "/redirect?resource=" + departmentId)
                .put("modal", "register").put("invitationUuid", department2UserRole.getUuid()).put("logo", "http://www.donotfetch.com")
                .put("globalLogo", "http://www.donotfetch.com").build()));

        testUserService.setAuthentication(departmentUser.getId());
        transactionTemplate.execute(status ->
            resourceApi.updateResourceUser(Scope.DEPARTMENT, departmentId, departmentUser2Id,
                new UserRoleDTO().setRole(Role.AUTHOR)));

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        testNotificationService.verify();
        testNotificationService.stop();

        testUserService.setAuthentication(departmentUser.getId());
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> departmentApi.getDepartmentOperations(departmentId));
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
                .put("summary", null, "department 3 summary")
                .put("handle", "department-2", "department-3")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", new ArrayList<>(), Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("name", "department 3", "department 4")
                .put("summary", "department 3 summary", "department 4 summary")
                .put("handle", "department-3", "department-4")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"),
                    ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
                .put("memberCategories", Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT"), Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("documentLogo", ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"), null)
                .put("memberCategories", Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT"), null));
    }

    @Test
    @Sql("classpath:data/department_autosuggest_setup.sql")
    public void shouldSuggestDepartments() {
        List<DepartmentRepresentation> departmentRs = departmentApi.lookupDepartments("Computer");
        Assert.assertEquals(3, departmentRs.size());

        verifySuggestedDepartment("Computer Science Department", departmentRs.get(0));
        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(1));
        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(2));

        departmentRs = departmentApi.lookupDepartments("Computer Science Laboratory");
        Assert.assertEquals(3, departmentRs.size());

        verifySuggestedDepartment("Laboratory for the Foundations of Computer Science", departmentRs.get(0));
        verifySuggestedDepartment("Computer Science Department", departmentRs.get(1));
        verifySuggestedDepartment("Department of Computer Science", departmentRs.get(2));

        departmentRs = departmentApi.lookupDepartments("School of Informatics");
        Assert.assertEquals(1, departmentRs.size());

        verifySuggestedDepartment("School of Informatics", departmentRs.get(0));

        departmentRs = departmentApi.lookupDepartments("Physics");
        Assert.assertEquals(0, departmentRs.size());

        departmentRs = departmentApi.lookupDepartments("Mathematics");
        Assert.assertEquals(0, departmentRs.size());
    }

    @Test
    public void shouldPostMembers() throws InterruptedException {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        DepartmentRepresentation departmentR = boardApi.postBoard(boardDTO).getDepartment();
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
        BoardDTO boardDTO = TestHelper.sampleBoard();

        Long departmentId = boardApi.postBoard(boardDTO).getDepartment().getId();
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

        Long memberId = transactionTemplate.execute(status -> userRepository.findByEmail("alastair@knowles.com")).getId();
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
        User boardUser = testUserService.authenticate();
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard())).getDepartment();
        Long departmentId = departmentR.getId();

        Long boardUserId = boardUser.getId();
        listenForNewActivities(boardUserId);

        testUserActivityService.record();
        testNotificationService.record();
        User boardMember = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            departmentApi.postMembershipRequest(departmentId,
                new UserRoleDTO().setUser(new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.SIXTYFIVE_PLUS).setLocationNationality(
                    new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                    .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(3).setExpiryDate(LocalDate.now().plusYears(2)));
            return null;
        });

        Long boardMemberId = boardMember.getId();
        testUserActivityService.verify(boardUserId,
            new TestUserActivityService.ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUser.getGivenName()).put("department", departmentR.getName())
                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
                .put("modal", "login").put("logo", "http://www.donotfetch.com").put("globalLogo", "http://www.donotfetch.com").build()));

        testUserService.setAuthentication(boardMemberId);
        transactionTemplate.execute(status -> ExceptionUtils.verifyException(
            BoardForbiddenException.class,
            () -> departmentApi.putMembershipRequest(departmentId, boardMemberId, "accepted"),
            ExceptionCode.FORBIDDEN_ACTION,
            status));

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> {
            departmentApi.putMembershipRequest(departmentId, boardMemberId, "accepted");
            return null;
        });

        verifyActivitiesEmpty(boardUserId);
        Resource department = transactionTemplate.execute(status -> resourceService.findOne(departmentId));
        UserRole userRole = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER));
        Assert.assertEquals(State.ACCEPTED, userRole.getState());

        testUserActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(boardMemberId);
        transactionTemplate.execute(status ->
            ExceptionUtils.verifyException(
                BoardException.class,
                () -> departmentApi.postMembershipRequest(departmentId,
                    new UserRoleDTO().setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setExpiryDate(LocalDate.now().plusYears(2))),
                ExceptionCode.DUPLICATE_PERMISSION,
                status));
    }

    @Test
    public void shouldRequestAndRejectMembership() {
        User boardUser = testUserService.authenticate();
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard())).getDepartment();
        Long departmentId = departmentR.getId();

        Long boardUserId = boardUser.getId();
        listenForNewActivities(boardUserId);

        testUserActivityService.record();
        testNotificationService.record();
        User boardMember = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            departmentApi.postMembershipRequest(departmentId, new UserRoleDTO()
                .setUser(new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.FIFTY_SIXTYFOUR).setLocationNationality(
                    new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2).setExpiryDate(LocalDate.now().plusYears(2)));
            return null;
        });

        Long boardMemberId = boardMember.getId();
        testUserActivityService.verify(boardUserId,
            new TestUserActivityService.ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUser.getGivenName()).put("department", departmentR.getName())
                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
                .put("modal", "login").put("logo", "http://www.donotfetch.com").put("globalLogo", "http://www.donotfetch.com").build()));

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> {
            departmentApi.putMembershipRequest(departmentId, boardMemberId, "rejected");
            return null;
        });

        verifyActivitiesEmpty(boardUserId);
        Resource department = transactionTemplate.execute(status -> resourceService.findOne(departmentId));
        UserRole userRole = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER));
        Assert.assertEquals(State.REJECTED, userRole.getState());

        testUserActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(boardMemberId);
        transactionTemplate.execute(status ->
            ExceptionUtils.verifyException(
                BoardForbiddenException.class,
                () -> departmentApi.postMembershipRequest(departmentId,
                    new UserRoleDTO().setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setExpiryDate(LocalDate.now().plusYears(2))),
                ExceptionCode.FORBIDDEN_PERMISSION,
                status));
    }

    @Test
    public void shouldRequestAndDismissMembership() {
        User boardUser = testUserService.authenticate();
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard())).getDepartment();
        Long departmentId = departmentR.getId();

        Long boardUserId = boardUser.getId();
        listenForNewActivities(boardUserId);

        testUserActivityService.record();
        testNotificationService.record();
        User boardMember = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            departmentApi.postMembershipRequest(departmentId,
                new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.FIFTY_SIXTYFOUR).setLocationNationality(
                    new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                    .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(1).setExpiryDate(LocalDate.now().plusYears(2)));
            return null;
        });

        Long boardMemberId = boardMember.getId();
        testUserActivityService.verify(boardUserId,
            new TestUserActivityService.ActivityInstance(departmentId, boardMemberId, Role.MEMBER, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUser.getGivenName()).put("department", departmentR.getName())
                .put("resourceUserRedirect", serverUrl + "/redirect?resource=" + departmentId + "&view=users&fragment=memberRequests")
                .put("modal", "login").put("logo", "http://www.donotfetch.com").put("globalLogo", "http://www.donotfetch.com").build()));

        Long activityId = transactionTemplate.execute(status -> {
            Resource department = resourceService.findOne(departmentId);
            UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, boardMember, Role.MEMBER);
            return activityService.findByUserRoleAndActivity(userRole, Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY).getId();
        });

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> {
            userApi.dismissActivity(activityId);
            return null;
        });

        verifyActivitiesEmpty(boardUserId);
        testUserActivityService.stop();
        testNotificationService.stop();
    }

    @Test
    public void shouldCountBoardsAndMembers() {
        Long departmentUserId = testUserService.authenticate().getId();
        Long departmentId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard())).getDepartment().getId();
        transactionTemplate.execute(status -> departmentApi.postDepartment(new DepartmentDTO().setName("other")));

        testUserService.authenticate();
        Long board1Id = transactionTemplate.execute(status -> boardApi.postBoard(
            TestHelper.smallSampleBoard()
                .setName("board1")
                .setDepartment(new DepartmentDTO().setId(departmentId)))).getId();

        Long board2Id = transactionTemplate.execute(status -> boardApi.postBoard(
            TestHelper.smallSampleBoard()
                .setName("board2")
                .setDepartment(new DepartmentDTO().setId(departmentId)))).getId();
        verifyBoardAndMemberCount(departmentId, 1L, 0L);

        testUserService.setAuthentication(departmentUserId);
        transactionTemplate.execute(status -> boardApi.executeAction(board1Id, "accept", new BoardPatchDTO()));
        verifyBoardAndMemberCount(departmentId, 2L, 0L);

        transactionTemplate.execute(status -> boardApi.executeAction(board2Id, "accept", new BoardPatchDTO()));
        verifyBoardAndMemberCount(departmentId, 3L, 0L);

        transactionTemplate.execute(status -> boardApi.executeAction(board2Id, "reject", new BoardPatchDTO().setComment("comment")));
        verifyBoardAndMemberCount(departmentId, 2L, 0L);

        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO()
                .setUser(new UserDTO().setGivenName("one").setSurname("one").setEmail("one@one.com"))
                .setRole(Role.MEMBER));
        verifyBoardAndMemberCount(departmentId, 2L, 1L);

        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO()
                .setUser(new UserDTO().setGivenName("two").setSurname("two").setEmail("two@two.com"))
                .setRole(Role.MEMBER));
        verifyBoardAndMemberCount(departmentId, 2L, 2L);

        Long memberUser1Id = testUserService.authenticate().getId();
        transactionTemplate.execute(status -> {
            departmentApi.postMembershipRequest(departmentId,
                new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                    new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));
            return null;
        });

        Long memberUser2Id = testUserService.authenticate().getId();
        transactionTemplate.execute(status -> {
            departmentApi.postMembershipRequest(departmentId, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));
            return null;
        });

        verifyBoardAndMemberCount(departmentId, 2L, 4L);

        testUserService.setAuthentication(departmentUserId);
        transactionTemplate.execute(status -> {
            departmentApi.putMembershipRequest(departmentId, memberUser1Id, "accepted");
            return null;
        });

        verifyBoardAndMemberCount(departmentId, 2L, 4L);

        transactionTemplate.execute(status -> {
            departmentApi.putMembershipRequest(departmentId, memberUser2Id, "rejected");
            return null;
        });

        verifyBoardAndMemberCount(departmentId, 2L, 3L);
    }

    private Pair<DepartmentRepresentation, DepartmentRepresentation> verifyPostTwoDepartments() {
        testUserService.authenticate();
        DepartmentRepresentation departmentR1 = verifyPostDepartment(TestHelper.smallSampleBoard(), "department").getDepartment();
        DepartmentRepresentation departmentR2 = verifyPostDepartment(
            new BoardDTO()
                .setName("board")
                .setDepartment(new DepartmentDTO()
                    .setName("department 2")),
            "department-2").getDepartment();

        return new Pair<>(departmentR1, departmentR2);
    }

    private BoardRepresentation verifyPostDepartment(BoardDTO boardDTO, String expectedHandle) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            DepartmentRepresentation departmentR = boardR.getDepartment();

            DepartmentDTO departmentDTO = boardDTO.getDepartment();
            Assert.assertEquals(departmentDTO.getName(), departmentR.getName());
            Assert.assertEquals(expectedHandle, departmentR.getHandle());
            Assert.assertEquals(Optional.ofNullable(departmentDTO.getMemberCategories()).orElse(new ArrayList<>()), departmentR.getMemberCategories());

            Department department = departmentService.getDepartment(departmentR.getId());
            University university = universityService.getUniversity(departmentR.getUniversity().getId());
            Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(university, department));
            return boardR;
        });
    }

    private DepartmentRepresentation verifyPatchDepartment(User user, Long departmentId, DepartmentPatchDTO departmentDTO, State expectedState) {
        testUserService.setAuthentication(user.getId());
        Department department = transactionTemplate.execute(status -> departmentService.getDepartment(departmentId));
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> departmentApi.patchDepartment(departmentId, departmentDTO));

        return transactionTemplate.execute(status -> {
            Optional<String> nameOptional = departmentDTO.getName();
            Assert.assertEquals(nameOptional == null ? department.getName() : nameOptional.orElse(null), departmentR.getName());

            Optional<String> summaryOptional = departmentDTO.getSummary();
            Assert.assertEquals(summaryOptional == null ? department.getSummary() : summaryOptional.orElse(null), departmentR.getSummary());

            Optional<DocumentDTO> documentLogoOptional = departmentDTO.getDocumentLogo();
            verifyDocument(documentLogoOptional == null ? department.getDocumentLogo() : departmentDTO.getDocumentLogo().orElse(null), departmentR.getDocumentLogo());

            Optional<String> handleOptional = departmentDTO.getHandle();
            Assert.assertEquals(handleOptional == null ? department.getHandle().split("/")[1] : handleOptional.orElse(null), departmentR.getHandle());

            Optional<List<MemberCategory>> memberCategoriesOptional = departmentDTO.getMemberCategories();
            Assert.assertEquals(memberCategoriesOptional == null ? resourceService.getCategories(department, CategoryType.MEMBER) :
                memberCategoriesOptional.orElse(new ArrayList<>()), departmentR.getMemberCategories());

            Assert.assertEquals(expectedState, departmentR.getState());
            return departmentR;
        });
    }

    private void verifyDepartmentActions(User adminUser, Collection<User> unprivilegedUsers, Long boardId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.DEPARTMENT, boardId, operations, PUBLIC_ACTIONS.get(State.ACCEPTED));
        verifyResourceActions(unprivilegedUsers, Scope.DEPARTMENT, boardId, operations, PUBLIC_ACTIONS.get(State.ACCEPTED));
        verifyResourceActions(adminUser, Scope.DEPARTMENT, boardId, operations, ADMIN_ACTIONS.get(State.ACCEPTED));
    }

    private void verifyUnprivilegedDepartmentUser(List<String> departmentNames) {
        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(null, null)),
            Collections.emptyList(),
            null);

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(true, null)),
            departmentNames,
            new TestHelper.ExpectedActions()
                .add(Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED))));
    }

    private void verifyPrivilegedDepartmentUser(List<String> departmentNames, List<String> adminDepartmentNames) {
        List<Action> adminActions = Lists.newArrayList(ADMIN_ACTIONS.get(State.ACCEPTED));

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(null, null)),
            adminDepartmentNames,
            new TestHelper.ExpectedActions()
                .addAll(adminDepartmentNames, adminActions));

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(true, null)),
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

    private void verifyBoardAndMemberCount(Long departmentId, Long boardCount, Long memberCount) {
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> departmentApi.getDepartment(departmentId));
        TestHelper.verifyNullableCount(boardCount, departmentR.getBoardCount());
        TestHelper.verifyNullableCount(memberCount, departmentR.getMemberCount());

        List<DepartmentRepresentation> departmentRs = transactionTemplate.execute(status -> departmentApi.getDepartments(true, null));
        TestHelper.verifyNullableCount(boardCount, departmentRs.get(0).getBoardCount());
        TestHelper.verifyNullableCount(memberCount, departmentRs.get(0).getMemberCount());

        TestHelper.verifyNullableCount(0L, departmentRs.get(1).getBoardCount());
        TestHelper.verifyNullableCount(0L, departmentRs.get(1).getMemberCount());
    }

    private void verifyMember(String expectedEmail, LocalDate expectedExpiryDate, MemberCategory expectedMemberCategory, UserRoleRepresentation actual) {
        Assert.assertEquals(BoardUtils.obfuscateEmail(expectedEmail), actual.getUser().getEmail());
        Assert.assertEquals(expectedExpiryDate, actual.getExpiryDate());
        Assert.assertEquals(expectedMemberCategory, actual.getMemberCategory());
    }

}
