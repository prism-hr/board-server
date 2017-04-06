package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoardApiIT extends AbstractIT {
    
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
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
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
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            boardDTO.getSettings().setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
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
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
    
            ExceptionUtil.verifyApiException(() -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, transactionStatus);
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
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            ExceptionUtil.verifyApiException(() -> boardApi.postBoard(boardDTO.setName("shouldNotCreateDuplicateBoardHandle Board 2")),
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
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);
            
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
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);
            
            boardDTO1.setName(boardDTO2.getName());
            boardDTO1.getSettings().setHandle(boardDTO2.getSettings().getHandle());
            ExceptionUtil.verifyApiException(() -> boardApi.updateBoard(boardR1.getId(), boardDTO1), ExceptionCode.DUPLICATE_BOARD, transactionStatus);
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
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);
            
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
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);
    
            ExceptionUtil.verifyApiException(() -> boardApi.updateBoardSettings(boardR1.getId(), boardDTO1.getSettings().setHandle(boardDTO2.getSettings().getHandle())),
                ExceptionCode.DUPLICATE_BOARD_HANDLE, transactionStatus);
            return null;
        });
    }
    
    @Test
    public void shouldCreateMultipleBoardsAndReturnCorrectResourceListsForUsers() {
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
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            
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
    
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());
    
            departmentBoardHelper.verifyBoard(otherUser, boardDTO2, boardR2, false);
            return null;
        });
        
        transactionTemplate.execute(TransactionStatus -> {
            userTestService.setAuthentication(user.getStormpathId());
            List<BoardRepresentation> boards = boardApi.getBoards();
            List<DepartmentRepresentation> departments = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boards.size());
            Assert.assertEquals(1, departments.size());
            
            userTestService.setAuthentication(otherUser.getStormpathId());
            boards = boardApi.getBoards();
            departments = departmentApi.getDepartments();
            
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
    
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            
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
    
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());
    
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);
            return boardR.getDepartment().getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = departmentBoardHelper.verifyGetDepartment(createdDepartmentId);
            Assert.assertEquals(2, departmentR.getBoards().size());
            
            List<String> boardNames = departmentR.getBoards().stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("Board 1", "Board 2"));
    
            departmentApi.updateDepartment(departmentR.getId(),
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
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
    
            boardApi.updateBoardSettings(boardR.getId(), new BoardSettingsDTO()
                .setHandle("subs2")
                .setPostCategories(ImmutableList.of("c"))
                .setDefaultPostVisibility(PostVisibility.PUBLIC));
            return boardR.getId();
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = departmentBoardHelper.verifyGetBoard(boardId);
            Assert.assertThat(boardR.getPostCategories(), Matchers.contains("c"));
            Assert.assertEquals(PostVisibility.PUBLIC, boardR.getDefaultPostVisibility());
            return null;
        });
    }
    
    @Test
    public void shouldCreateBoardAndUpdateCategoriesForBoardAndDepartment() {
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
            BoardRepresentation board = boardApi.postBoard(boardDTO1);
            boardApi.updateBoardSettings(board.getId(), settingsDTO.setPostCategories(ImmutableList.of("b2", "b3")));
            departmentApi.updateDepartment(board.getDepartment().getId(), departmentDTO.setMemberCategories(ImmutableList.of("d1", "d3")));
            return board;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation updatedBoard = boardApi.getBoard(savedBoard.getId());
            Assert.assertThat(updatedBoard.getPostCategories(), Matchers.containsInAnyOrder("b2", "b3"));
            Assert.assertThat(updatedBoard.getDepartment().getMemberCategories(), Matchers.containsInAnyOrder("d1", "d3"));
            return null;
        });
    }
    
    @Test
    public void shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            for (int i = 1; i < 4; i++) {
                String departmentName = "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser " + i;
                String departmentHandle = "sgdab" + i;
                for (int j = 1; j < 4; j++) {
                    String boardSuffix = i + " " + j;
                    BoardDTO boardDTO = new BoardDTO()
                        .setName("shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser " + boardSuffix)
                        .setPurpose("Purpose")
                        .setDepartment(new DepartmentDTO()
                            .setName(departmentName)
                            .setHandle(departmentHandle)
                            .setMemberCategories(ImmutableList.of("category1", "category2")))
                        .setSettings(new BoardSettingsDTO()
                            .setHandle("sgdab" + boardSuffix)
                            .setPostCategories(ImmutableList.of("category3", "category4")));
                    BoardRepresentation boardR = boardApi.postBoard(boardDTO);
                    departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
                }
            }
    
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            List<DepartmentRepresentation> departmentRepresentations = departmentApi.getDepartments();
            Assert.assertEquals(Arrays.asList(
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3"),
                departmentRepresentations.stream().map(DepartmentRepresentation::getName).collect(Collectors.toList()));
    
            List<BoardRepresentation> boardRepresentations = boardApi.getBoards();
            Assert.assertEquals(Arrays.asList(
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1 1", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1 2",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1 3", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2 1",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2 2", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2 3",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3 1", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3 2",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3 3"),
                boardRepresentations.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
            return null;
        });
    }
    
}
