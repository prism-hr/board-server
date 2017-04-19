package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.DepartmentService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class BoardApiIT extends AbstractIT {
    
    @Inject
    private DepartmentApi departmentApi;
    
    @Inject
    private BoardApi boardApi;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private TestUserService testUserService;
    
    @Inject
    private DepartmentBoardHelper departmentBoardHelper;
    
    @Test
    public void shouldCreateBoard() {
        User user = testUserService.authenticate();
        verifyPostBoard(user,
            new BoardDTO()
                .setName("New Board")
                .setPurpose("Purpose")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setMemberCategories(ImmutableList.of("category1", "category2"))),
            "new-board", "new-department");
    }
    
    @Test
    public void shouldNotCreateDuplicateBoard() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("New Board")
            .setPurpose("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        verifyPostBoard(user, boardDTO, "new-board", "new-department");
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("New Board With Long Name")
            .setPurpose("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        verifyPostBoard(user, boardDTO, "new-board-with-long", "new-department");
        
        boardDTO.setName("New Board With Long Name Two");
        verifyPostBoard(user, boardDTO, "new-board-with-long-2", "new-department");
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setHandle(Optional.of(boardRs.getValue().getHandle()));
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE, status);
            return null;
        });
    }
    
    @Test
    public void shouldCreateMultipleBoardsAndReturnCorrectResourceListsForUsers() {
        User user = testUserService.authenticate();
        Long departmentId = verifyPostBoard(user,
            new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-1", "department-1")
            .getDepartment().getId();
        
        User secondUser = testUserService.authenticate();
        verifyPostBoard(secondUser,
            new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId)
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-2", "department-1", false);
        
        transactionTemplate.execute(TransactionStatus -> {
            testUserService.setAuthentication(user.getStormpathId());
            List<BoardRepresentation> boardRs = boardApi.getBoards();
            List<DepartmentRepresentation> departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
    
            boardRs.forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND)));
            
            testUserService.setAuthentication(secondUser.getStormpathId());
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            boardRs.stream().filter(boardR -> boardR.getName().equals("Board 1"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            boardRs.stream().filter(boardR -> boardR.getName().equals("Board 2"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            
            testUserService.setAuthentication(null);
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            Assert.assertThat(boardRs, Matchers.everyItem(Matchers.hasProperty("actions", Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND))));
            Assert.assertThat(departmentRs, Matchers.contains(Matchers.hasProperty("actions", Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND))));
            return null;
        });
    }
    
    @Test
    public void shouldCreateAndUpdateTwoBoardsWithinOneDepartment() {
        User user = testUserService.authenticate();
        Long departmentId = verifyPostBoard(user,
            new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose 1")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-1", "department-1")
            .getDepartment().getId();
        
        verifyPostBoard(user, new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose 2")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId)
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-2", "department-1");
        
        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = departmentBoardHelper.verifyGetDepartment(departmentId);
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(departmentR.getId());
            Assert.assertEquals(2, boardRs.size());
            
            List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("Board 1", "Board 2"));
            
            departmentApi.updateDepartment(departmentR.getId(),
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-updated")));
            return null;
        });
        
        transactionTemplate.execute(status -> {
            Department department = departmentService.getDepartment(departmentId);
            Assert.assertEquals("new-department-updated", department.getHandle());
            
            int index = 1;
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(department.getId());
            for (BoardRepresentation boardR : boardRs) {
                Assert.assertEquals("new-department-updated/board-" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                index++;
            }
            
            return null;
        });
    }
    
    @Test
    public void shouldUpdateBoard() {
        User user = testUserService.authenticate();
        Long boardId = verifyPostBoard(user,
            new BoardDTO()
                .setName("New Board")
                .setPurpose("Purpose")
                .setPostCategories(ImmutableList.of("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setMemberCategories(new ArrayList<>())),
            "new-board", "new-department")
            .getId();
        
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO()
                .setName(Optional.of("New Board Updated"))
                .setPurpose(Optional.of("Purpose Updated"))
                .setHandle(Optional.of("new-board-updated"))
                .setPostCategories(Optional.of(ImmutableList.of("c")))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PUBLIC));
            boardApi.updateBoard(boardId, boardPatchDTO);
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = departmentBoardHelper.verifyGetBoard(boardId);
            Assert.assertEquals("New Board Updated", boardR.getName());
            Assert.assertEquals("Purpose Updated", boardR.getPurpose());
            Assert.assertEquals("new-board-updated", boardR.getHandle());
            Assert.assertThat(boardR.getPostCategories(), Matchers.contains("c"));
            Assert.assertEquals(PostVisibility.PUBLIC, boardR.getDefaultPostVisibility());
            return null;
        });
    }
    
    @Test
    public void shouldCreateAndListBoards() {
        User user = testUserService.authenticate();
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                verifyPostBoard(user,
                    new BoardDTO()
                        .setName("Board " + i + " " + j)
                        .setPurpose("Purpose")
                        .setPostCategories(ImmutableList.of("category3", "category4"))
                        .setDepartment(new DepartmentDTO()
                            .setName("Department " + i)
                            .setMemberCategories(ImmutableList.of("category1", "category2"))),
                    "board-" + i + "-" + j, "department-" + i);
            }
        }
        
        transactionTemplate.execute(transactionStatus -> {
            List<DepartmentRepresentation> departmentRepresentations = departmentApi.getDepartments();
            Assert.assertEquals(Arrays.asList("Department 1", "Department 2", "Department 3"),
                departmentRepresentations.stream().map(DepartmentRepresentation::getName).collect(Collectors.toList()));
            
            List<BoardRepresentation> boardRepresentations = boardApi.getBoards();
            Assert.assertEquals(Arrays.asList("Board 1 1", "Board 1 2", "Board 1 3", "Board 2 1", "Board 2 2", "Board 2 3", "Board 3 1", "Board 3 2", "Board 3 3"),
                boardRepresentations.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
            return null;
        });
    }
    
    private Pair<BoardRepresentation, BoardRepresentation> verifyPostTwoBoards() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("New Board")
            .setPurpose("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        BoardRepresentation boardR = verifyPostBoard(user, boardDTO, "new-board", "new-department");
        
        BoardDTO otherBoardDTO = new BoardDTO()
            .setName("Other New Board")
            .setPurpose("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        BoardRepresentation otherBoardR = verifyPostBoard(user, otherBoardDTO, "other-new-board", "new-department");
        return new Pair<>(boardR, otherBoardR);
    }
    
    private BoardRepresentation verifyPostBoard(User user, BoardDTO boardDTO, String expectedHandle, String expectedDepartmentHandle) {
        return verifyPostBoard(user, boardDTO, expectedHandle, expectedDepartmentHandle, true);
    }
    
    private BoardRepresentation verifyPostBoard(User user, BoardDTO boardDTO, String expectedHandle, String expectedDepartmentHandle, boolean expectDepartmentAdministrator) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation postedBoardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, postedBoardR, expectDepartmentAdministrator);
            Assert.assertEquals(expectedHandle, postedBoardR.getHandle());
            Assert.assertEquals(expectedDepartmentHandle, postedBoardR.getDepartment().getHandle());
            return postedBoardR;
        });
    }
    
}
