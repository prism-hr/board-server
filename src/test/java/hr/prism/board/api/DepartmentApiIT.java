package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.ObjectUtils;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
public class DepartmentApiIT extends AbstractIT {

    private static LinkedHashMultimap<State, Action> ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.EXTEND));

        PUBLIC_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EXTEND));
    }

    @Test
    public void shouldCreateDepartment() {
        testUserService.authenticate();
        DepartmentDTO department =
            ((DepartmentDTO) TestHelper.sampleDepartment()
                .setSummary("summary")).setDocumentLogo(
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
            ((BoardDTO) new BoardDTO()
                .setName("new board"))
                .setDepartment((DepartmentDTO) new DepartmentDTO()
                    .setName("new department with long name")),
            "new-department-with");

        Long departmentId = verifyPostDepartment(
            ((BoardDTO) new BoardDTO()
                .setName("new board"))
                .setDepartment((DepartmentDTO) new DepartmentDTO()
                    .setName("new department with long name too")),
            "new-department-with-2").getDepartment().getId();

        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-with-long")));
            Assert.assertEquals("new-department-with-long", departmentR.getHandle());
            return null;
        });

        verifyPostDepartment(
            ((BoardDTO) new BoardDTO()
                .setName("new board"))
                .setDepartment((DepartmentDTO) new DepartmentDTO()
                    .setName("new department with long name also")),
            "new-department-with-2");
    }

    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyDuplicateException(
                () -> departmentApi.updateDepartment(departmentRs.getKey().getId(),
                    (DepartmentPatchDTO) new DepartmentPatchDTO()
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
                () -> departmentApi.updateDepartment(departmentRs.getKey().getId(),
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
        transactionTemplate.execute(status -> {
            Board board = boardService.getBoard(boardId);
            userRoleService.createUserRole(board, boardUser, Role.ADMINISTRATOR);
            return null;
        });

        // Create post
        User postUser = testUserService.authenticate();
        transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost()));

        // Create unprivileged users
        List<User> unprivilegedUsers = Lists.newArrayList(makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.smallSamplePost()).values());
        unprivilegedUsers.add(boardUser);
        unprivilegedUsers.add(postUser);

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.AUDIT, () -> departmentApi.getDepartmentOperations(departmentId))
            .put(Action.EDIT, () -> departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO()))
            .build();

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we do not audit viewing
        transactionTemplate.execute(status -> departmentApi.getDepartment(departmentId));

        // Check that we can make changes and leave nullable values null
        verifyPatchDepartment(departmentUser, departmentId,
            ((DepartmentPatchDTO) new DepartmentPatchDTO()
                .setName(Optional.of("department 2")))
                .setHandle(Optional.of("department-2")),
            State.ACCEPTED);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and set nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            ((DepartmentPatchDTO) new DepartmentPatchDTO()
                .setName(Optional.of("department 3"))
                .setSummary(Optional.of("department 3 summary")))
                .setHandle(Optional.of("department-3"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
                .setMemberCategories(Optional.of(ImmutableList.of(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))),
            State.ACCEPTED);

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        // Check that we can make further changes and change nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            ((DepartmentPatchDTO) new DepartmentPatchDTO()
                .setName(Optional.of("department 4"))
                .setSummary(Optional.of("department 4 summary")))
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
                new ResourceUserDTO()
                    .setUser(new UserDTO()
                        .setGivenName("admin1")
                        .setSurname("admin1")
                        .setEmail("admin1@admin1.com"))
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.ADMINISTRATOR)))).getUser().getId());

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_DEPARTMENT_NOTIFICATION, userCacheService.findOne(departmentUser2Id),
            ImmutableMap.<String, String>builder().put("recipient", "admin1").put("department", "department 4")
                .put("resourceRedirect", environment.getProperty("server.url") + "/redirect?resource=" + departmentId).put("modal", "Register").build()));


        transactionTemplate.execute(status ->
            resourceApi.updateResourceUser(Scope.DEPARTMENT, departmentId, departmentUser2Id,
                new ResourceUserDTO()
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.AUTHOR)))));

        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        testNotificationService.verify();
        testNotificationService.stop();

        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> departmentApi.getDepartmentOperations(departmentId));
        Assert.assertEquals(5, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, departmentUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "department", "department 2")
                .put("handle", "department", "department-2"));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "department 2", "department 3")
                .put("summary", null, "department 3 summary")
                .put("handle", "department-2", "department-3")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", new ArrayList<>(), Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "department 3", "department 4")
                .put("summary", "department 3 summary", "department 4 summary")
                .put("handle", "department-3", "department-4")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"),
                    ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
                .put("memberCategories", Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT"), Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
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

    private Pair<DepartmentRepresentation, DepartmentRepresentation> verifyPostTwoDepartments() {
        testUserService.authenticate();
        DepartmentRepresentation departmentR1 = verifyPostDepartment(TestHelper.smallSampleBoard(), "department").getDepartment();
        DepartmentRepresentation departmentR2 = verifyPostDepartment(
            ((BoardDTO) new BoardDTO()
                .setName("board"))
                .setDepartment((DepartmentDTO) new DepartmentDTO()
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
            Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));

            Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));
            return boardR;
        });
    }

    private DepartmentRepresentation verifyPatchDepartment(User user, Long departmentId, DepartmentPatchDTO departmentDTO, State expectedState) {
        testUserService.setAuthentication(user.getId());
        Department department = transactionTemplate.execute(status -> departmentService.getDepartment(departmentId));
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> departmentApi.updateDepartment(departmentId, departmentDTO));

        return transactionTemplate.execute(status -> {
            Optional<String> nameOptional = departmentDTO.getName();
            Assert.assertEquals(nameOptional == null ? department.getName() : nameOptional.orElse(null), departmentR.getName());

            Optional<String> summaryOptional = departmentDTO.getSummary();
            Assert.assertEquals(summaryOptional == null ? department.getSummary() : summaryOptional.orElse(null), departmentR.getSummary());

            Optional<DocumentDTO> documentLogoOptional = departmentDTO.getDocumentLogo();
            verifyDocument(documentLogoOptional == null ? department.getDocumentLogo() : departmentDTO.getDocumentLogo().orElse(null), departmentR.getDocumentLogo());

            Optional<String> handleOptional = departmentDTO.getHandle();
            Assert.assertEquals(handleOptional == null ? department.getHandle() : handleOptional.orElse(null), departmentR.getHandle());

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
            transactionTemplate.execute(status -> departmentApi.getDepartments(null)),
            Collections.emptyList(),
            null);

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(true)),
            departmentNames,
            new TestHelper.ExpectedActions()
                .add(Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED))));
    }

    private void verifyPrivilegedDepartmentUser(List<String> departmentNames, List<String> adminDepartmentNames) {
        List<Action> adminActions = Lists.newArrayList(ADMIN_ACTIONS.get(State.ACCEPTED));

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(null)),
            adminDepartmentNames,
            new TestHelper.ExpectedActions()
                .addAll(adminDepartmentNames, adminActions));

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> departmentApi.getDepartments(true)),
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

}
