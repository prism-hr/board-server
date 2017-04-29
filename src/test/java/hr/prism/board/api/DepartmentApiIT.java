package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.TestUserService;
import hr.prism.board.util.ObjectUtils;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private TestHelper testHelper;
    
    @Test
    public void shouldCreateDepartmentWithAllPropertiesSet() {
        User user = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setDocumentLogo(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                    .setMemberCategories(ImmutableList.of("a", "b")));
        
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            testHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department", boardR.getDepartment().getHandle());
            return boardR.getDepartment().getId();
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentHandle() {
        User user = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department With Long Name")
                    .setMemberCategories(ImmutableList.of("a", "b")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            testHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department-with", boardR.getDepartment().getHandle());
            return null;
        });
        
        
        Long departmentId = transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department With Long Name Too")
                    .setMemberCategories(ImmutableList.of("a", "b")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            testHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department-with-2", boardR.getDepartment().getHandle());
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-with-long")));
            Assert.assertEquals("new-department-with-long", departmentR.getHandle());
            return null;
        });
        
        transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department With Long Name Also")
                    .setMemberCategories(ImmutableList.of("a", "b")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            testHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department-with-2", boardR.getDepartment().getHandle());
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        testUserService.authenticate();
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = createTwoDepartments();
        
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
        testUserService.authenticate();
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = createTwoDepartments();
        
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
    public void shouldNotBeAbleToCorruptDepartmentByPatching() {
        testUserService.authenticate();
        Long departmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = TestHelper.sampleBoard();
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO().setName(Optional.empty())),
                ExceptionCode.MISSING_DEPARTMENT_NAME, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
                ExceptionCode.MISSING_DEPARTMENT_HANDLE, null);
            status.setRollbackOnly();
            return null;
        });
    }
    
    @Test
    public void shouldAuditDepartmentAndMakeChangesPrivatelyVisible() {
        User departmentUser = testUserService.authenticate();
        Long departmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = TestHelper.sampleBoard();
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            return boardR.getDepartment().getId();
        });
        
        User boardUser = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("other board")
                    .setDepartment(new DepartmentDTO()
                        .setId(departmentId)));
            return null;
        });
        
        // Test that we do not audit viewing
        transactionTemplate.execute(status -> {
            departmentApi.getDepartment(departmentId);
            return null;
        });
    
        // Check that we can make changes and leave nullable values null
        testUserService.setAuthentication(departmentUser.getStormpathId());
        transactionTemplate.execute(status -> {
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setName(Optional.of("department 2"))
                    .setHandle(Optional.of("department-2")));
            return null;
        });
    
        // Check that we can make further changes and set nullable values
        testUserService.setAuthentication(departmentUser.getStormpathId());
        transactionTemplate.execute(status -> {
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setName(Optional.of("department 3"))
                    .setHandle(Optional.of("department-3"))
                    .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
                    .setMemberCategories(Optional.of(ImmutableList.of("a", "b"))));
            return null;
        });
    
        // Check that we can make further changes and change nullable values
        transactionTemplate.execute(status -> {
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setName(Optional.of("department 4"))
                    .setHandle(Optional.of("department-4"))
                    .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                    .setMemberCategories(Optional.of(ImmutableList.of("b2", "a2"))));
            return null;
        });
    
        // Check that we can clear nullable values
        transactionTemplate.execute(status -> {
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setDocumentLogo(Optional.empty())
                    .setMemberCategories(Optional.empty()));
            return null;
        });
    
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> departmentApi.getDepartment(departmentId));
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> departmentApi.getDepartmentOperations(departmentId));
        Assert.assertEquals(5, resourceOperationRs.size());
    
        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        ResourceOperationRepresentation resourceOperationR0 = resourceOperationRs.get(0);
        ResourceOperationRepresentation resourceOperationR4 = resourceOperationRs.get(4);
    
        testHelper.verifyResourceOperation(resourceOperationR0, Action.EXTEND, departmentUser, null);
    
        testHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "department", "department 2")
                .put("handle", "department", "department-2"));
        
        testHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "department 2", "department 3")
                .put("handle", "department-2", "department-3")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", null, Arrays.asList("a", "b")));
    
        testHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "department 3", "department 4")
                .put("handle", "department-3", "department-4")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"),
                    ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
                .put("memberCategories", Arrays.asList("a", "b"), Arrays.asList("b2", "a2")));
    
        testHelper.verifyResourceOperation(resourceOperationR4, Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("documentLogo", ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"), null)
                .put("memberCategories", Arrays.asList("b2", "a2"), null));
    
        Assert.assertEquals(resourceOperationR0.getCreatedTimestamp(), departmentR.getCreatedTimestamp());
        Assert.assertEquals(resourceOperationR4.getCreatedTimestamp(), departmentR.getUpdatedTimestamp());
        
        // Test that other board administrator cannot view audit trail
        testUserService.setAuthentication(boardUser.getStormpathId());
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> departmentApi.getDepartmentOperations(departmentId), ExceptionCode.FORBIDDEN_ACTION, status);
            return null;
        });
        
        // Test that a member of the public cannot view audit trail
        testUserService.unauthenticate();
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> departmentApi.getDepartmentOperations(departmentId), ExceptionCode.UNAUTHENTICATED_USER, status);
            return null;
        });
    }
    
    private Pair<DepartmentRepresentation, DepartmentRepresentation> createTwoDepartments() {
        DepartmentRepresentation departmentR1 = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("board")
                .setDepartment(new DepartmentDTO()
                    .setName("department"));
    
            return boardApi.postBoard(boardDTO).getDepartment();
        });
        
        DepartmentRepresentation departmentR2 = transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("board")
                .setDepartment(new DepartmentDTO()
                    .setName("department 2"));
    
            return boardApi.postBoard(boardDTO).getDepartment();
        });
        
        return new Pair<>(departmentR1, departmentR2);
    }
    
}
