package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.TestUserService;
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
    private DepartmentBoardHelper departmentBoardHelper;
    
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
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
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
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
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
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
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
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
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
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR1, true);
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
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            return boardR.getDepartment();
        });
        
        return new Pair<>(departmentR1, departmentR2);
    }
    
}
