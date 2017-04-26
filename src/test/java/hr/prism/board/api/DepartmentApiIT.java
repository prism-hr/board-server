package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.VerificationHelper;
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
import java.util.ArrayList;
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
    private VerificationHelper verificationHelper;
    
    @Test
    public void shouldUpdateDepartment() {
        User user = testUserService.authenticate();
        Long departmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setDocumentLogo(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                    .setMemberCategories(ImmutableList.of("a", "b")));
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            verificationHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department", boardR.getDepartment().getHandle());
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setName(Optional.of("Old Department"))
                    .setHandle(Optional.of("new-department"))
                    .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                    .setMemberCategories(Optional.of(ImmutableList.of("c"))));
            
            Assert.assertEquals("Old Department", departmentR.getName());
            Assert.assertEquals("new-department", departmentR.getHandle());
            Assert.assertEquals("c2", departmentR.getDocumentLogo().getCloudinaryId());
            Assert.assertThat(departmentR.getMemberCategories(), Matchers.contains("c"));
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentHandle() {
        User user = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department With Long Name")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            verificationHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department-with", boardR.getDepartment().getHandle());
            return null;
        });
        
        
        Long departmentId = transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department With Long Name Too")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            verificationHelper.verifyBoard(user, boardDTO, boardR, true);
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
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department With Long Name Also")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            verificationHelper.verifyBoard(user, boardDTO, boardR, true);
            Assert.assertEquals("new-board", boardR.getHandle());
            Assert.assertEquals("new-department-with-2", boardR.getDepartment().getHandle());
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        User user = testUserService.authenticate();
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = createTwoDepartments(user);
        
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
        User user = testUserService.authenticate();
        Pair<DepartmentRepresentation, DepartmentRepresentation> departmentRs = createTwoDepartments(user);
        
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
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setDocumentLogo(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                    .setMemberCategories(ImmutableList.of("a", "b")));
            
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
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setDocumentLogo(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                    .setMemberCategories(ImmutableList.of("a", "b")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            return boardR.getDepartment().getId();
        });
        
        User boardUser = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("Other New Board")
                    .setDescription("Purpose")
                    .setPostCategories(new ArrayList<>())
                    .setDepartment(new DepartmentDTO()
                        .setId(departmentId)));
            return null;
        });
        
        // Test that we do not audit viewing
        transactionTemplate.execute(status -> {
            departmentApi.getDepartment(departmentId);
            return null;
        });
    
        testUserService.setAuthentication(departmentUser.getStormpathId());
        transactionTemplate.execute(status -> {
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setName(Optional.of("New Department 2"))
                    .setHandle(Optional.of("new-department-2"))
                    .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c2").setCloudinaryUrl("u2").setFileName("f2")))
                    .setMemberCategories(Optional.of(ImmutableList.of("c", "d"))));
            return null;
        });
    
        testUserService.setAuthentication(departmentUser.getStormpathId());
        transactionTemplate.execute(status -> {
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setName(Optional.of("New Department 3"))
                    .setHandle(Optional.of("new-department-3"))
                    .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("c3").setCloudinaryUrl("u3").setFileName("f3")))
                    .setMemberCategories(Optional.of(ImmutableList.of("e", "f"))));
            return null;
        });
    
        DepartmentRepresentation departmentR = transactionTemplate.execute(status -> departmentApi.getDepartment(departmentId));
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> departmentApi.getDepartmentOperations(departmentId));
        Assert.assertEquals(3, resourceOperationRs.size());
    
        ResourceOperationRepresentation resourceOperationR1 = resourceOperationRs.get(0);
        Assert.assertEquals(Action.EDIT, resourceOperationR1.getAction());
        verificationHelper.verifyUser(departmentUser, resourceOperationR1.getUser());
        
        ResourceChangeListRepresentation resourceChangeListR1 = resourceOperationR1.getChangeList();
        Assert.assertEquals(4, resourceChangeListR1.size());
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("New Department 2").setNewValue("New Department 3"),
            resourceChangeListR1.get("name"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("new-department-2").setNewValue("new-department-3"),
            resourceChangeListR1.get("handle"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation()
                .setOldValue(ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2"))
                .setNewValue(ObjectUtils.orderedMap("cloudinaryId", "c3", "cloudinaryUrl", "u3", "fileName", "f3")),
            resourceChangeListR1.get("documentLogo"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue(Lists.newArrayList("c", "d")).setNewValue(Lists.newArrayList("e", "f")),
            resourceChangeListR1.get("memberCategories"));
    
        ResourceOperationRepresentation resourceOperationR2 = resourceOperationRs.get(1);
        Assert.assertEquals(Action.EDIT, resourceOperationR2.getAction());
        verificationHelper.verifyUser(departmentUser, resourceOperationR2.getUser());
        
        ResourceChangeListRepresentation resourceChangeListR2 = resourceOperationR2.getChangeList();
        Assert.assertEquals(4, resourceChangeListR1.size());
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("New Department").setNewValue("New Department 2"),
            resourceChangeListR2.get("name"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("new-department").setNewValue("new-department-2"),
            resourceChangeListR2.get("handle"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation()
                .setOldValue(ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .setNewValue(ObjectUtils.orderedMap("cloudinaryId", "c2", "cloudinaryUrl", "u2", "fileName", "f2")),
            resourceChangeListR2.get("documentLogo"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue(Lists.newArrayList("a", "b")).setNewValue(Lists.newArrayList("c", "d")),
            resourceChangeListR2.get("memberCategories"));
    
        ResourceOperationRepresentation resourceOperationR3 = resourceOperationRs.get(2);
        Assert.assertEquals(Action.EXTEND, resourceOperationR3.getAction());
        verificationHelper.verifyUser(departmentUser, resourceOperationR3.getUser());
        Assert.assertNull(resourceOperationR3.getChangeList());
    
        Assert.assertEquals(resourceOperationR1.getCreatedTimestamp(), departmentR.getUpdatedTimestamp());
        Assert.assertEquals(resourceOperationR3.getCreatedTimestamp(), departmentR.getCreatedTimestamp());
        
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
    
    private Pair<DepartmentRepresentation, DepartmentRepresentation> createTwoDepartments(User user) {
        DepartmentRepresentation departmentR1 = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
    
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO);
            verificationHelper.verifyBoard(user, boardDTO, boardR1, true);
            return boardR1.getDepartment();
        });
        
        DepartmentRepresentation departmentR2 = transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department Two")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            verificationHelper.verifyBoard(user, boardDTO, boardR, true);
            return boardR.getDepartment();
        });
        
        return new Pair<>(departmentR1, departmentR2);
    }
    
}
