package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.UserTestService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.ArrayList;

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
    private BoardService boardService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private UserTestService userTestService;
    
    @Inject
    private DepartmentBoardHelper departmentBoardHelper;
    
    @Test
    public void shouldUpdateDepartment() {
        User user = userTestService.authenticate();
        Long departmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 3")
                .setPurpose("Purpose 3")
                .setDepartment(new DepartmentDTO()
                    .setName("Department 3")
                    .setHandle("Handle3")
                    .setMemberCategories(ImmutableList.of("a", "b")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("Handle3")
                    .setPostCategories(new ArrayList<>()));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            
            departmentApi.updateDepartment(boardR.getDepartment().getId(),
                new DepartmentDTO()
                    .setName("Another name 3")
                    .setHandle("AnotherHandle3")
                    .setMemberCategories(ImmutableList.of("c")));
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = departmentBoardHelper.verifyGetDepartment(departmentId);
            Assert.assertEquals("Another name 3", departmentR.getName());
            Assert.assertEquals("AnotherHandle3", departmentR.getHandle());
            Assert.assertThat(departmentR.getMemberCategories(), Matchers.contains("c"));
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentHandle() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartmentHandle Board 1")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartmentHandle Department 1")
                    .setHandle("sncddh1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncddh1")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);
            
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartmentHandle Board 2")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartmentHandle Department 2")
                    .setHandle("sncddh1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncddh1")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            
            ExceptionUtil.verifyApiException(() -> boardApi.postBoard(boardDTO2), ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartmentsByUpdating Board 1")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartmentsByUpdating Department 1")
                    .setHandle("sncddbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncddbu1")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);
            
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartmentsByUpdating Board 2")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartmentsByUpdating Department 2")
                    .setHandle("sncddbu2")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncddbu2")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);
            
            ExceptionUtil.verifyApiException(() ->
                    departmentApi.updateDepartment(boardR1.getDepartment().getId(),
                        new DepartmentDTO()
                            .setName(boardDTO2.getDepartment().getName())
                            .setHandle(boardDTO2.getDepartment().getHandle())
                            .setMemberCategories(boardDTO1.getDepartment().getMemberCategories())),
                ExceptionCode.DUPLICATE_DEPARTMENT, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentHandlesByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartmentHandlesByUpdating Board 1")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartmentHandlesByUpdating Department 1")
                    .setHandle("sncddhbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncddhbu1")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);
            
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartmentHandlesByUpdating Board 2")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartmentHandlesByUpdating Department 2")
                    .setHandle("sncddhbu2")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncddhbu2")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);
            
            ExceptionUtil.verifyApiException(() ->
                    departmentApi.updateDepartment(boardR1.getDepartment().getId(),
                        new DepartmentDTO()
                            .setName(boardDTO1.getDepartment().getName())
                            .setHandle(boardDTO2.getDepartment().getHandle())
                            .setMemberCategories(boardDTO1.getDepartment().getMemberCategories())),
                ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE, transactionStatus);
            return null;
        });
    }
    
}
