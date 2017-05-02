package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.*;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.TestUserService;
import hr.prism.board.util.ObjectUtils;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class DepartmentApiIT extends AbstractIT {
    
    @Inject
    private DepartmentApi departmentApi;
    
    @Inject
    private BoardApi boardApi;
    
    @Inject
    private TestUserService testUserService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Test
    public void shouldCreateBoard() {
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardDTO fullBoardDTO = new BoardDTO()
            .setName("other board")
            .setDescription("description")
            .setPostCategories(ImmutableList.of("a", "b"))
            .setDepartment(new DepartmentDTO()
                .setName("other department")
                .setMemberCategories(ImmutableList.of("c", "d")));
    
        User user = testUserService.authenticate();
        verifyPostDepartment(user, boardDTO, "department");
        verifyPostDepartment(user, fullBoardDTO, "other-department");
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentHandle() {
        User user = testUserService.authenticate();
        verifyPostDepartment(user,
            new BoardDTO()
                .setName("new board")
                .setDepartment(new DepartmentDTO()
                    .setName("new department with long name")),
            "new-department-with");
    
        Long departmentId = verifyPostDepartment(user,
            new BoardDTO()
                .setName("new board")
                .setDepartment(new DepartmentDTO()
                    .setName("new department with long name too")),
            "new-department-with-2").getId();
        
        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-with-long")));
            Assert.assertEquals("new-department-with-long", departmentR.getHandle());
            return null;
        });
    
        verifyPostDepartment(user,
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
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    departmentApi.updateDepartment(departmentRs.getKey().getId(),
                        new DepartmentPatchDTO()
                            .setName(Optional.of(departmentRs.getValue().getName()))),
                ExceptionCode.DUPLICATE_DEPARTMENT, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentHandlesByUpdating() {
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = verifyPostTwoDepartments();
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    departmentApi.updateDepartment(departmentRs.getKey().getId(),
                        new DepartmentPatchDTO()
                            .setHandle(Optional.of(departmentRs.getValue().getHandle()))),
                ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE, status);
            return null;
        });
    }
    
    @Test
    public void shouldSupportDepartmentLifecycleAndPermissions() {
        // Create department and board
        User departmentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.smallSampleBoard();
        BoardRepresentation boardR = verifyPostDepartment(departmentUser, boardDTO, "department");
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();
    
        User boardUser = testUserService.authenticate();
        Board board = boardService.getBoard(boardId);
        transactionTemplate.execute(status -> {
            userRoleService.createUserRole(board, boardUser, Role.ADMINISTRATOR);
            return null;
        });
    
        // Create post
        User postUser = testUserService.authenticate();
        PostRepresentation postR = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.samplePost()));
        Assert.assertEquals(State.DRAFT, postR.getState());
    
        // Create unprivileged users
        List<User> unprivilegedUsers = makeUnprivilegedUsers(departmentId, boardId, TestHelper.samplePost());
        unprivilegedUsers.add(boardUser);
        unprivilegedUsers.add(postUser);
    
        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.AUDIT, () -> departmentApi.getDepartmentOperations(departmentId))
            .put(Action.EDIT, () -> departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO()))
            .build();
    
        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        
        // Check that we do not audit viewing
        transactionTemplate.execute(status -> {
            departmentApi.getDepartment(departmentId);
            return null;
        });
    
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
                .setHandle(Optional.of("department-3"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
                .setMemberCategories(Optional.of(ImmutableList.of("m1", "m2"))),
            State.ACCEPTED);
    
        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        
        // Check that we can make further changes and change nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setName(Optional.of("department 4"))
                .setHandle(Optional.of("department-4"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                .setMemberCategories(Optional.of(ImmutableList.of("m2", "m1"))),
            State.ACCEPTED);
    
        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
        
        // Check that we can clear nullable values
        verifyPatchDepartment(departmentUser, departmentId,
            new DepartmentPatchDTO()
                .setDocumentLogo(Optional.empty())
                .setMemberCategories(Optional.empty()),
            State.ACCEPTED);
    
        verifyDepartmentActions(departmentUser, unprivilegedUsers, departmentId, operations);
    
        testUserService.setAuthentication(departmentUser.getStormpathId());
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> departmentApi.getDepartment(departmentId));
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> departmentApi.getDepartmentOperations(departmentId));
        Assert.assertEquals(5, resourceOperationRs.size());
    
        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        ResourceOperationRepresentation resourceOperationR0 = resourceOperationRs.get(0);
        ResourceOperationRepresentation resourceOperationR4 = resourceOperationRs.get(4);
    
        TestHelper.verifyResourceOperation(resourceOperationR0, Action.EXTEND, boardUser, null);
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "department", "department 2")
                .put("handle", "department", "department-2"));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "department 2", "department 3")
                .put("handle", "department-2", "department-3")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", null, Arrays.asList("m1", "m2")));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "department 3", "department 4")
                .put("handle", "department-3", "department-4")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"),
                    ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
                .put("memberCategories", Arrays.asList("m1", "m2"), Arrays.asList("m2", "m1")));
    
        TestHelper.verifyResourceOperation(resourceOperationR4, Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("documentLogo", ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"), null)
                .put("memberCategories", Arrays.asList("m2", "m1"), null));
        
        Assert.assertEquals(resourceOperationR0.getCreatedTimestamp(), departmentR.getCreatedTimestamp());
        Assert.assertEquals(resourceOperationR4.getCreatedTimestamp(), departmentR.getUpdatedTimestamp());
    }
    
    private Pair<DepartmentRepresentation, DepartmentRepresentation> verifyPostTwoDepartments() {
        User user = testUserService.authenticate();
        DepartmentRepresentation departmentR1 = verifyPostDepartment(user, TestHelper.smallSampleBoard(), "department").getDepartment();
        DepartmentRepresentation departmentR2 = verifyPostDepartment(user,
            new BoardDTO()
                .setName("board")
                .setDepartment(new DepartmentDTO()
                    .setName("department 2")),
            "department-2").getDepartment();
        
        return new Pair<>(departmentR1, departmentR2);
    }
    
    private BoardRepresentation verifyPostDepartment(User user, BoardDTO boardDTO, String expectedHandle) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            DepartmentRepresentation departmentR = boardR.getDepartment();
    
            DepartmentDTO departmentDTO = boardDTO.getDepartment();
            Assert.assertEquals(departmentDTO.getName(), departmentR.getName());
            Assert.assertEquals(expectedHandle, departmentR.getHandle());
            Assert.assertEquals(departmentDTO.getMemberCategories(), departmentR.getMemberCategories());
    
            Department department = departmentService.getDepartment(departmentR.getId());
            Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));
    
            Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));
            Assert.assertTrue(userRoleService.hasUserRole(department, user, Role.ADMINISTRATOR));
            Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND));
            return boardR;
        });
    }
    
    private DepartmentRepresentation verifyPatchDepartment(User user, Long departmentId, DepartmentPatchDTO departmentDTO, State expectedState) {
        testUserService.setAuthentication(user.getStormpathId());
        return transactionTemplate.execute(status -> {
            Department department = departmentService.getDepartment(departmentId);
            DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId, departmentDTO);
            
            Optional<String> nameOptional = departmentDTO.getName();
            Assert.assertEquals(nameOptional == null ? department.getName() : nameOptional.orElse(null), departmentR.getName());
            
            Optional<DocumentDTO> documentLogoOptional = departmentDTO.getDocumentLogo();
            verifyDocument(documentLogoOptional == null ? department.getDocumentLogo() : departmentDTO.getDocumentLogo().orElse(null), departmentR.getDocumentLogo());
            
            Optional<String> handleOptional = departmentDTO.getHandle();
            Assert.assertEquals(handleOptional == null ? department.getHandle() : handleOptional.orElse(null), departmentR.getHandle());
            
            Optional<List<String>> memberCategoriesOptional = departmentDTO.getMemberCategories();
            Assert.assertEquals(memberCategoriesOptional == null ? resourceService.getCategories(department, CategoryType.MEMBER) : memberCategoriesOptional.orElse(null),
                departmentR.getMemberCategories());
            
            Assert.assertEquals(expectedState, departmentR.getState());
            return departmentR;
        });
    }
    
    private void verifyDepartmentActions(User departmentUser, List<User> unprivilegedUsers, Long departmentId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.DEPARTMENT, departmentId, operations, Action.VIEW, Action.EXTEND);
        verifyResourceActions(unprivilegedUsers, Scope.DEPARTMENT, departmentId, operations, Action.VIEW, Action.EXTEND);
        verifyResourceActions(departmentUser, Scope.DEPARTMENT, departmentId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND);
    }
    
}
