package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.enums.PostVisibility;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class DepartmentBoardApiIT extends AbstractIT {
    
    @Inject
    private DepartmentBoardApi departmentBoardApi;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private UserTestService userTestService;
    
    @Test
    public void shouldCreateBoardWithAllPossibleFieldsSet() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldCreateBoard Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldCreateBoard Department")
                    .setHandle("scb")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("scb")
                    .setPostCategories(ImmutableList.of("category3", "category4"))
                    .setDefaultPostVisibility(PostVisibility.PART_PRIVATE));
    
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
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
                .setDepartment(new DepartmentDTO()
                    .setName("shouldCreateBoardDefaultPostVisibility Department")
                    .setHandle("scbdpv")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("scbdpv")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
    
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            boardDTO.getSettings().setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
            verifyBoard(user, boardDTO, boardR, true);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoard() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoard Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoard Department")
                    .setHandle("sncdb")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncdb")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
    
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            ExceptionUtil.verifyApiException(() -> departmentBoardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        User user = userTestService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("shouldNotCreateDuplicateBoardHandle Board 1")
            .setPurpose("Purpose")
            .setDepartment(new DepartmentDTO()
                .setName("shouldNotCreateDuplicateBoardHandle Department")
                .setHandle("sncdbh")
                .setMemberCategories(ImmutableList.of("category1", "category2")))
            .setSettings(new BoardSettingsDTO()
                .setHandle("sncdbh")
                .setPostCategories(ImmutableList.of("category3", "category4")));
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            ExceptionUtil.verifyApiException(() -> departmentBoardApi.postBoard(boardDTO.setName("shouldNotCreateDuplicateBoardHandle Board 2")),
                ExceptionCode.DUPLICATE_BOARD_HANDLE, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardByUpdating Board 1")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardByUpdating Department")
                    .setHandle("sncdbbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncdbbu1")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = departmentBoardApi.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
    
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardByUpdating Board 2")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardByUpdating Department")
                    .setHandle("sncdbbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncdbbu2")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR2 = departmentBoardApi.postBoard(boardDTO2);
            verifyBoard(user, boardDTO2, boardR2, true);
    
            boardDTO1.setName(boardDTO2.getName());
            boardDTO1.getSettings().setHandle(boardDTO2.getSettings().getHandle());
            ExceptionUtil.verifyApiException(() -> departmentBoardApi.updateBoard(boardR1.getId(), boardDTO1), ExceptionCode.DUPLICATE_BOARD, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardHandleByUpdating Board 1")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardHandleByUpdating Department")
                    .setHandle("sncdbhbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncdbhbu1")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR1 = departmentBoardApi.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
    
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardHandleByUpdating Board 2")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardHandleByUpdating Department")
                    .setHandle("sncdbhbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sncdbhbu2")
                    .setPostCategories(ImmutableList.of("category3", "category4")));
            BoardRepresentation boardR2 = departmentBoardApi.postBoard(boardDTO2);
            verifyBoard(user, boardDTO2, boardR2, true);
    
            ExceptionUtil.verifyApiException(() -> departmentBoardApi.updateBoardSettings(boardR1.getId(), boardDTO1.getSettings().setHandle(boardDTO2.getSettings().getHandle())),
                ExceptionCode.DUPLICATE_BOARD_HANDLE, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldReturnCorrectDepartmentAndBoardListsForUsers() {
        User user = userTestService.authenticate();
        User otherUser = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            userTestService.setAuthentication(user.getStormpathId());
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose 1")
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setHandle("Handle1")
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("Handle1")
                    .setPostCategories(new ArrayList<>())
                    .setDefaultPostVisibility(PostVisibility.PRIVATE));
            
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
            
            Long departmentId = boardR.getDepartment().getId();
            userTestService.setAuthentication(otherUser.getStormpathId());
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose 2")
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("Handle2")
                    .setPostCategories(new ArrayList<>())
                    .setDefaultPostVisibility(PostVisibility.PRIVATE));
            
            BoardRepresentation boardR2 = departmentBoardApi.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());
            
            verifyBoard(otherUser, boardDTO2, boardR2, false);
            return null;
        });
        
        transactionTemplate.execute(TransactionStatus -> {
            userTestService.setAuthentication(user.getStormpathId());
            List<BoardRepresentation> boards = departmentBoardApi.getBoards();
            List<DepartmentRepresentation> departments = departmentBoardApi.getDepartments();
            
            Assert.assertEquals(2, boards.size());
            Assert.assertEquals(1, departments.size());
            
            userTestService.setAuthentication(otherUser.getStormpathId());
            boards = departmentBoardApi.getBoards();
            departments = departmentBoardApi.getDepartments();
            
            Assert.assertEquals(1, boards.size());
            Assert.assertEquals("Board 2", boards.get(0).getName());
            Assert.assertEquals(0, departments.size());
            return null;
        });
    }
    
    @Test
    public void shouldCreateAndUpdateTwoBoardsWithinOneDepartment() {
        User user = userTestService.authenticate();
        Long createdDepartmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose 1")
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setHandle("Handle1")
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("Handle1")
                    .setPostCategories(new ArrayList<>())
                    .setDefaultPostVisibility(PostVisibility.PRIVATE));
    
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            Long departmentId = boardR.getDepartment().getId();
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose 2")
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("Handle2")
                    .setPostCategories(new ArrayList<>())
                    .setDefaultPostVisibility(PostVisibility.PRIVATE));
    
            BoardRepresentation boardR2 = departmentBoardApi.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());
    
            verifyBoard(user, boardDTO2, boardR2, true);
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = verifyGetDepartment(createdDepartmentId);
            Assert.assertEquals(2, departmentR.getBoards().size());
            
            List<String> boardNames = departmentR.getBoards().stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("Board 1", "Board 2"));
            
            departmentBoardApi.updateDepartment(departmentR.getId(),
                new DepartmentDTO()
                    .setName(departmentR.getName())
                    .setHandle("Handle2")
                    .setMemberCategories(departmentR.getMemberCategories()));
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            Department department = departmentService.getDepartment(createdDepartmentId);
            Assert.assertEquals("Handle2", department.getHandle());
            
            int index = 1;
            Iterable<Board> boards = boardService.findByDepartment(department);
            for (Board board : boards) {
                Assert.assertEquals("Handle2/Handle" + index, board.getHandle());
                index++;
            }
            
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
                .setDepartment(new DepartmentDTO()
                    .setName("Department 3")
                    .setHandle("Handle3")
                    .setMemberCategories(ImmutableList.of("a", "b")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("Handle3")
                    .setPostCategories(new ArrayList<>()));
    
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            departmentBoardApi.updateDepartment(boardR.getDepartment().getId(),
                new DepartmentDTO()
                    .setName("Another name 3")
                    .setHandle("AnotherHandle3")
                    .setMemberCategories(ImmutableList.of("c")));
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = verifyGetDepartment(departmentId);
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
                .setDepartment(new DepartmentDTO()
                    .setName("shouldUpdateBoardSettings Department")
                    .setHandle("subs")
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("subs")
                    .setPostCategories(ImmutableList.of("a", "b")));
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
            verifyBoard(user, boardDTO, boardR, true);
    
            departmentBoardApi.updateBoardSettings(boardR.getId(), new BoardSettingsDTO()
                .setHandle("subs2")
                .setPostCategories(ImmutableList.of("c"))
                .setDefaultPostVisibility(PostVisibility.PUBLIC));
            return boardR.getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = verifyGetBoard(boardId);
            Assert.assertThat(boardR.getPostCategories(), Matchers.contains("c"));
            Assert.assertEquals(PostVisibility.PUBLIC, boardR.getDefaultPostVisibility());
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
            BoardRepresentation boardR1 = departmentBoardApi.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
    
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
    
            ExceptionUtil.verifyApiException(() -> departmentBoardApi.postBoard(boardDTO2), ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE, transactionStatus);
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
            BoardRepresentation boardR1 = departmentBoardApi.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
    
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
            BoardRepresentation boardR2 = departmentBoardApi.postBoard(boardDTO2);
            verifyBoard(user, boardDTO2, boardR2, true);
    
            ExceptionUtil.verifyApiException(() ->
                    departmentBoardApi.updateDepartment(boardR1.getDepartment().getId(),
                        new DepartmentDTO()
                            .setName(boardDTO2.getDepartment().getName())
                            .setHandle(boardDTO2.getDepartment().getHandle())
                            .setMemberCategories(boardDTO1.getDepartment().getMemberCategories())),
                ExceptionCode.DUPLICATE_DEPARTMENT, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldUpdateCategories() {
        userTestService.authenticate();
        BoardSettingsDTO settingsDTO = new BoardSettingsDTO()
            .setHandle("sudc")
            .setPostCategories(ImmutableList.of("b1", "b2"));
        DepartmentDTO departmentDTO = new DepartmentDTO()
            .setName("shouldUpdateDepartmentCategories Department 1")
            .setHandle("sudc")
            .setMemberCategories(ImmutableList.of("d1", "d2"));
        
        BoardRepresentation savedBoard = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldUpdateDepartmentCategories Board 1")
                .setPurpose("Purpose")
                .setDepartment(departmentDTO)
                .setSettings(settingsDTO);
            BoardRepresentation board = departmentBoardApi.postBoard(boardDTO1);
            departmentBoardApi.updateBoardSettings(board.getId(), settingsDTO.setPostCategories(ImmutableList.of("b2", "b3")));
            departmentBoardApi.updateDepartment(board.getDepartment().getId(), departmentDTO.setMemberCategories(ImmutableList.of("d1", "d3")));
            return board;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation updatedBoard = departmentBoardApi.getBoard(savedBoard.getId());
            Assert.assertThat(updatedBoard.getPostCategories(), Matchers.containsInAnyOrder("b2", "b3"));
            Assert.assertThat(updatedBoard.getDepartment().getMemberCategories(), Matchers.containsInAnyOrder("d1", "d3"));
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
            BoardRepresentation boardR1 = departmentBoardApi.postBoard(boardDTO1);
            verifyBoard(user, boardDTO1, boardR1, true);
    
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
            BoardRepresentation boardR2 = departmentBoardApi.postBoard(boardDTO2);
            verifyBoard(user, boardDTO2, boardR2, true);
    
            ExceptionUtil.verifyApiException(() ->
                    departmentBoardApi.updateDepartment(boardR1.getDepartment().getId(),
                        new DepartmentDTO()
                            .setName(boardDTO1.getDepartment().getName())
                            .setHandle(boardDTO2.getDepartment().getHandle())
                            .setMemberCategories(boardDTO1.getDepartment().getMemberCategories())),
                ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldGetDepartmentsAndBoards() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            for (int i = 1; i < 4; i++) {
                String departmentName = "shouldGetDepartmentsAndBoards " + i;
                String departmentHandle = "sgdab" + i;
                for (int j = 1; j < 4; j++) {
                    String boardSuffix = i + " " + j;
                    BoardDTO boardDTO = new BoardDTO()
                        .setName("shouldGetDepartmentsAndBoards " + boardSuffix)
                        .setPurpose("Purpose")
                        .setDepartment(new DepartmentDTO()
                            .setName(departmentName)
                            .setHandle(departmentHandle)
                            .setMemberCategories(ImmutableList.of("category1", "category2")))
                        .setSettings(new BoardSettingsDTO()
                            .setHandle("sgdab" + boardSuffix)
                            .setPostCategories(ImmutableList.of("category3", "category4")));
                    BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);
                    verifyBoard(user, boardDTO, boardR, true);
                }
            }
    
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            List<DepartmentRepresentation> departmentRepresentations = departmentBoardApi.getDepartments();
            Assert.assertEquals(Arrays.asList(
                "shouldGetDepartmentsAndBoards 1", "shouldGetDepartmentsAndBoards 2", "shouldGetDepartmentsAndBoards 3"),
                departmentRepresentations.stream().map(DepartmentRepresentation::getName).collect(Collectors.toList()));
            
            List<BoardRepresentation> boardRepresentations = departmentBoardApi.getBoards();
            Assert.assertEquals(Arrays.asList(
                "shouldGetDepartmentsAndBoards 1 1", "shouldGetDepartmentsAndBoards 1 2", "shouldGetDepartmentsAndBoards 1 3",
                "shouldGetDepartmentsAndBoards 2 1", "shouldGetDepartmentsAndBoards 2 2", "shouldGetDepartmentsAndBoards 2 3",
                "shouldGetDepartmentsAndBoards 3 1", "shouldGetDepartmentsAndBoards 3 2", "shouldGetDepartmentsAndBoards 3 3"),
                boardRepresentations.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
            return null;
        });
    }
    
    private void verifyBoard(User user, BoardDTO boardDTO, BoardRepresentation boardR, boolean expectDepartmentAdministrator) {
        Assert.assertEquals(boardDTO.getName(), boardR.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardR.getPurpose());
        Assert.assertEquals(boardDTO.getSettings().getHandle(), boardR.getHandle());
        Assert.assertThat(boardR.getPostCategories(), Matchers.containsInAnyOrder(boardDTO.getSettings().getPostCategories().stream().toArray(String[]::new)));
        Assert.assertEquals(boardDTO.getSettings().getDefaultPostVisibility(), boardR.getDefaultPostVisibility());
        
        DepartmentRepresentation departmentR = boardR.getDepartment();
        Assert.assertEquals(boardDTO.getDepartment().getName(), departmentR.getName());
        Assert.assertEquals(boardDTO.getDepartment().getHandle(), departmentR.getHandle());
        Assert.assertThat(departmentR.getMemberCategories(), Matchers.containsInAnyOrder(boardDTO.getDepartment().getMemberCategories().stream().toArray(String[]::new)));
    
        Board board = boardService.getBoard(boardR.getId());
        Department department = departmentService.getDepartment(departmentR.getId());
        Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());
        Assert.assertThat(boardR.getRoles(), Matchers.containsInAnyOrder(Role.ADMINISTRATOR));
        
        Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
        Assert.assertTrue(userRoleService.hasUserRole(board, user, Role.ADMINISTRATOR));
        
        Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));
        if (expectDepartmentAdministrator) {
            Assert.assertTrue(userRoleService.hasUserRole(department, user, Role.ADMINISTRATOR));
            Assert.assertThat(boardR.getDepartment().getRoles(), Matchers.containsInAnyOrder(Role.ADMINISTRATOR));
        }
    }
    
    private DepartmentRepresentation verifyGetDepartment(Long id) {
        DepartmentRepresentation departmentR = departmentBoardApi.getDepartment(id);
        DepartmentRepresentation departmentRByHandle = departmentBoardApi.getDepartmentByHandle(departmentR.getHandle());
        Assert.assertEquals(departmentR.getId(), departmentRByHandle.getId());
        return departmentRByHandle;
    }
    
    private BoardRepresentation verifyGetBoard(Long id) {
        BoardRepresentation boardR = departmentBoardApi.getBoard(id);
        BoardRepresentation boardRByHandle = departmentBoardApi.getBoardByHandle(Joiner.on("/").join(boardR.getDepartment().getHandle(), boardR.getHandle()));
        Assert.assertEquals(boardR.getId(), boardRByHandle.getId());
        return boardRByHandle;
    }
    
}
