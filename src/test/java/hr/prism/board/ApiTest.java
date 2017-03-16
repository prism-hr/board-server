package hr.prism.board;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.UserTestService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ApiTest {
    
    @Inject
    private Api api;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private UserTestService userTestService;
    
    @Inject
    private PlatformTransactionManager platformTransactionManager;
    
    private TransactionTemplate transactionTemplate;
    
    @Before
    public void setUp() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }
    
    @Test
    public void shouldCreateBoardWithAllPossibleFieldsSet() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldCreateBoard Board")
                .setPurpose("Purpose")
                .setHandle("scb")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldCreateBoard Department")
                    .setHandle("scb")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4"))
                    .setDefaultPostVisibility(PostVisibility.PART_PRIVATE));
            
            BoardRepresentation boardR = api.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
            
            return null;
        });
    }
    
    @Test
    public void shouldCreateBoardWithDefaultPostVisibilityLevel() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldCreateBoardDefaultPostVisibility Board")
                .setPurpose("Purpose")
                .setHandle("scbdpv")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldCreateBoardDefaultPostVisibility Department")
                    .setHandle("scbdpv")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            
            BoardRepresentation boardR = api.postBoard(boardDTO);
            boardDTO.getSettings().setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
            verifyBoard(user, boardDTO, boardR, true);
            
            return null;
        });
    }
    
    @Test
    public void shouldNotPermitCreationOfDuplicateBoard() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldNotPermitCreationOf Board")
                .setPurpose("Purpose")
                .setHandle("snpco")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotPermitCreationOf Department")
                    .setHandle("snpco")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4")));
    
            BoardRepresentation boardR = api.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            ApiException apiException = null;
            try {
                boardR = api.postBoard(boardDTO);
            } catch (ApiException e) {
                apiException = e;
            }
    
            Assert.assertEquals(ExceptionCode.DUPLICATE_BOARD, apiException.getExceptionCode());
            transactionStatus.setRollbackOnly();
            return null;
        });
    }
    
    @Test
    public void shouldNotPermitCreationOfDuplicateBoardByNameByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotPermitCreationOfByUpdating Board 1")
                .setPurpose("Purpose")
                .setHandle("snpcobu1")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotPermitCreationOfByUpdating Department")
                    .setHandle("snpcobu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = api.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
            
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotPermitCreationOfByUpdating Board 2")
                .setPurpose("Purpose")
                .setHandle("snpcobu2")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotPermitCreationOfByUpdating Department")
                    .setHandle("snpcobu2")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR2 = api.postBoard(boardDTO2);
            verifyBoard(user, boardDTO2, boardR2, true);
            
            ApiException apiException = null;
            try {
                api.updateBoard(boardDTO1
                    .setId(boardR1.getId())
                    .setName(boardDTO2.getName())
                    .setHandle(boardDTO2.getHandle()));
            } catch (ApiException e) {
                apiException = e;
            }
            
            Assert.assertNotNull(apiException);
            Assert.assertEquals(ExceptionCode.DUPLICATE_BOARD, apiException.getExceptionCode());
            
            transactionStatus.setRollbackOnly();
            return null;
        });
    }
    
    @Test
    public void shouldCreateTwoBoardsWithinOneDepartment() {
        User user = userTestService.authenticate();
        Long createdDepartmentId = transactionTemplate.execute(transactionStatus -> {
            BoardSettingsDTO settingsDTO = new BoardSettingsDTO()
                .setPostCategories(new ArrayList<>())
                .setDefaultPostVisibility(PostVisibility.PRIVATE);
    
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose 1")
                .setHandle("Handle1")
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setHandle("Handle1")
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(settingsDTO);
    
            BoardRepresentation boardR = api.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            Long departmentId = boardR.getDepartment().getId();
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose 2")
                .setHandle("Handle2")
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId))
                .setSettings(settingsDTO);
    
            BoardRepresentation boardR2 = api.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());
    
            verifyBoard(user, boardDTO2, boardR2, true);
            return boardR.getDepartment().getId();
        });
    
        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = api.getDepartment(createdDepartmentId);
            Assert.assertEquals(2, departmentR.getBoards().size());
        
            List<String> boardNames = departmentR.getBoards().stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("Board 1", "Board 2"));
            return null;
        });
    }
    
    @Test
    public void shouldUpdateDepartment() {
        User user = userTestService.authenticate();
        Long departmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 3")
                .setPurpose("Purpose 3")
                .setHandle("Handle3")
                .setDepartment(new DepartmentDTO()
                    .setName("Department 3")
                    .setHandle("Handle3")
                    .setMemberCategories(ImmutableList.of("a", "b")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(new ArrayList<>()));
    
            BoardRepresentation boardR = api.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            api.updateDepartment(new DepartmentDTO()
                .setId(boardR.getDepartment().getId())
                .setName("Another name 3")
                .setHandle("AnotherHandle3")
                .setMemberCategories(ImmutableList.of("c")));
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = api.getDepartment(departmentId);
            Assert.assertEquals("Another name 3", departmentR.getName());
            Assert.assertEquals("AnotherHandle3", departmentR.getHandle());
            Assert.assertThat(departmentR.getMemberCategories(), Matchers.contains("c"));
            return null;
        });
    }
    
    @Test
    public void shouldUpdateBoardSettings() {
        User user = userTestService.authenticate();
        Long boardId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldUpdateBoardSettings Board")
                .setPurpose("Purpose")
                .setHandle("subs")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldUpdateBoardSettings Department")
                    .setHandle("subs")
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("a", "b")));
            BoardRepresentation boardR = api.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            api.updateBoardSettings(boardR.getId(), new BoardSettingsDTO()
                .setPostCategories(ImmutableList.of("c"))
                .setDefaultPostVisibility(PostVisibility.PUBLIC));
            return boardR.getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = api.getBoard(boardId);
            Assert.assertThat(boardR.getPostCategories(), Matchers.contains("c"));
            Assert.assertEquals(PostVisibility.PUBLIC, boardR.getDefaultPostVisibility());
            return null;
        });
    }
    
    @Test
    public void shouldGetDefinitions() {
        TreeMap<String, List<String>> definitions = api.getDefinitions();
        List<String> postVisibility = definitions.get("postVisibility");
        Assert.assertThat(postVisibility, Matchers.containsInAnyOrder("PART_PRIVATE", "PUBLIC", "PRIVATE"));
    }
    
    @Test
    public void shouldNotCreateDuplicateDepartmentsByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartment Board 1")
                .setPurpose("Purpose")
                .setHandle("sncdd1")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartment Department 1")
                    .setHandle("sncdd1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = api.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
            
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateDepartment Board 2")
                .setPurpose("Purpose")
                .setHandle("sncdd2")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateDepartment Department 2")
                    .setHandle("sncdd2")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR2 = api.postBoard(boardDTO2);
            verifyBoard(user, boardDTO2, boardR2, true);
            
            ApiException apiException = null;
            try {
                api.updateDepartment(new DepartmentDTO()
                    .setId(boardR1.getDepartment().getId())
                    .setName(boardDTO2.getDepartment().getName())
                    .setHandle(boardDTO2.getDepartment().getHandle())
                    .setMemberCategories(boardDTO1.getDepartment().getMemberCategories()));
            } catch (ApiException e) {
                apiException = e;
            }
            
            Assert.assertEquals(ExceptionCode.DUPLICATE_DEPARTMENT, apiException.getExceptionCode());
            transactionStatus.setRollbackOnly();
            return null;
        });
    }
    
    private void verifyBoard(User user, BoardDTO boardDTO, BoardRepresentation boardR, boolean expectDepartmentAdministrator) {
        Assert.assertEquals(boardDTO.getName(), boardR.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardR.getPurpose());
        Assert.assertEquals(boardDTO.getHandle(), boardR.getHandle());
        Assert.assertThat(boardR.getPostCategories(), Matchers.containsInAnyOrder(boardDTO.getSettings().getPostCategories().stream().toArray(String[]::new)));
        Assert.assertEquals(boardDTO.getSettings().getDefaultPostVisibility(), boardR.getDefaultPostVisibility());
        
        DepartmentRepresentation departmentR = boardR.getDepartment();
        Assert.assertEquals(boardDTO.getDepartment().getName(), departmentR.getName());
        Assert.assertEquals(boardDTO.getDepartment().getHandle(), departmentR.getHandle());
        Assert.assertThat(departmentR.getMemberCategories(), Matchers.containsInAnyOrder(boardDTO.getDepartment().getMemberCategories().stream().toArray(String[]::new)));
        
        transactionTemplate.execute(transactionStatus -> {
            Board board = boardService.findOne(boardR.getId());
            Department department = departmentService.findOne(departmentR.getId());
            
            Assert.assertThat(board.getParents().stream().map(p -> p.getResource1()).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
            Assert.assertTrue(userRoleService.hasUserRole(board, user, Role.ADMINISTRATOR));
            
            Assert.assertThat(department.getParents().stream().map(b -> b.getResource1()).collect(Collectors.toList()), Matchers.contains(department));
            if (expectDepartmentAdministrator) {
                Assert.assertTrue(userRoleService.hasUserRole(department, user, Role.ADMINISTRATOR));
            }
            
            return null;
        });
    }
    
}
