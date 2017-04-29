package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.*;
import hr.prism.board.service.BoardService;
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
import org.springframework.transaction.TransactionStatus;

import javax.inject.Inject;
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
    private PostApi postApi;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private TestUserService testUserService;
    
    @Test
    public void shouldCreateBoard() {
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardDTO fullBoardDTO = new BoardDTO()
            .setName("new board")
            .setDescription("description")
            .setPostCategories(ImmutableList.of("a", "b"))
            .setDepartment(new DepartmentDTO()
                .setName("new department")
                .setMemberCategories(ImmutableList.of("c", "d")));
    
        User user = testUserService.authenticate();
        verifyPostBoard(user, boardDTO, "board");
        verifyPostBoard(user, fullBoardDTO, "new-board");
    }
    
    @Test
    public void shouldNotCreateDuplicateBoard() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
    
        transactionTemplate.execute(status -> {
            boardApi.postBoard(boardDTO);
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("new board with long name")
            .setDepartment(new DepartmentDTO()
                .setName("new department"));
        verifyPostBoard(user, boardDTO, "new-board-with-long");
        Long boardId = verifyPostBoard(user, boardDTO.setName("new board with long name too"), "new-board-with-long-2").getId();
    
        transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setHandle(Optional.of("new-board-with-long-name")));
            Assert.assertEquals("new-board-with-long-name", boardR.getHandle());
            return null;
        });
    
        verifyPostBoard(user, boardDTO.setName("new board with long name also"), "new-board-with-long-2");
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        testUserService.authenticate();
        Pair<BoardRepresentation, BoardRepresentation> boardRs = postTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        testUserService.authenticate();
        Pair<BoardRepresentation, BoardRepresentation> boardRs = postTwoBoards();
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
                .setName("board 1")
                .setDepartment(new DepartmentDTO()
                    .setName("department")),
            "board-1")
            .getDepartment().getId();
        
        User secondUser = testUserService.authenticate();
        verifyPostBoard(secondUser,
            new BoardDTO()
                .setName("board 2")
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId)
                    .setName("department")),
            "board-2");
    
        // Verify second user does not also get department admin role
        Department department = departmentService.getDepartment(departmentId);
        Assert.assertFalse(userRoleService.hasUserRole(department, secondUser, Role.ADMINISTRATOR));
        
        transactionTemplate.execute(TransactionStatus -> {
            testUserService.setAuthentication(user.getStormpathId());
            List<BoardRepresentation> boardRs = boardApi.getBoards();
            List<DepartmentRepresentation> departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            boardRs.forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND)));
            
            testUserService.setAuthentication(secondUser.getStormpathId());
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
    
            boardRs.stream().filter(boardR -> boardR.getName().equals("board 1"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            boardRs.stream().filter(boardR -> boardR.getName().equals("board 2"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            
            testUserService.setAuthentication(null);
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            boardRs.forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            return null;
        });
    }
    
    @Test
    public void shouldReindexBoardHandleWhenDepartmentHandleUpdated() {
        User user = testUserService.authenticate();
        Long departmentId = verifyPostBoard(user,
            new BoardDTO()
                .setName("board 1")
                .setDepartment(new DepartmentDTO()
                    .setName("department")),
            "board-1")
            .getDepartment().getId();
        
        verifyPostBoard(user, new BoardDTO()
                .setName("board 2")
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId)),
            "board-2");
        
        transactionTemplate.execute(status -> {
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(departmentId);
            Assert.assertEquals(2, boardRs.size());
            
            List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("board 1", "board 2"));
    
            departmentApi.updateDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-updated")));
            return null;
        });
        
        transactionTemplate.execute(status -> {
            Department department = departmentService.getDepartment(departmentId);
            Assert.assertEquals("new-department-updated", department.getHandle());
            
            int index = 1;
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(department.getId());
            Assert.assertEquals(2, boardRs.size());
            for (BoardRepresentation boardR : boardRs) {
                Assert.assertEquals("new-department-updated/board-" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                index++;
            }
            
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
                        .setName("board " + i + " " + j)
                        .setDepartment(new DepartmentDTO()
                            .setName("department " + i)),
                    "board-" + i + "-" + j);
            }
        }
        
        transactionTemplate.execute(transactionStatus -> {
            List<DepartmentRepresentation> departmentRepresentations = departmentApi.getDepartments();
            Assert.assertEquals(Arrays.asList("department 1", "department 2", "department 3"),
                departmentRepresentations.stream().map(DepartmentRepresentation::getName).collect(Collectors.toList()));
            
            List<BoardRepresentation> boardRepresentations = boardApi.getBoards();
            Assert.assertEquals(Arrays.asList("board 1 1", "board 1 2", "board 1 3", "board 2 1", "board 2 2", "board 2 3", "board 3 1", "board 3 2", "board 3 3"),
                boardRepresentations.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
            return null;
        });
    }
    
    @Test
    public void shouldNotBeAbleToCorruptBoardByPatching() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute((TransactionStatus status) -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    boardApi.updateBoard(boardId, new BoardPatchDTO().setName(Optional.empty())),
                ExceptionCode.MISSING_BOARD_NAME, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    boardApi.updateBoard(boardId, new BoardPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
                ExceptionCode.MISSING_BOARD_HANDLE, null);
            status.setRollbackOnly();
            return null;
        });
    }
    
    @Test
    public void shouldAuditBoardAndMakeChangesPrivatelyVisible() {
        User boardUser = testUserService.authenticate();
        Long boardId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("new board")
                .setDepartment(new DepartmentDTO()
                    .setName("new department"));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            return boardR.getId();
        });
        
        User postUser = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> postApi.postPost(boardId, TestHelper.samplePost()));
        
        // Test that we do not audit viewing
        transactionTemplate.execute(status -> {
            boardApi.getBoard(boardId);
            return null;
        });
    
        // Check that we can make changes and leave nullable values null
        testUserService.setAuthentication(boardUser.getStormpathId());
        transactionTemplate.execute(status -> {
            boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setName(Optional.of("new board 2"))
                    .setHandle(Optional.of("new-board-2")));
            return null;
        });
    
        // Check that we can make further changes and set default / nullable values
        transactionTemplate.execute(status -> {
            boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setName(Optional.of("new board 3"))
                    .setHandle(Optional.of("new-board-3"))
                    .setDefaultPostVisibility(Optional.of(PostVisibility.PRIVATE))
                    .setDescription(Optional.of("description"))
                    .setPostCategories(Optional.of(Arrays.asList("a", "b"))));
            return null;
        });
    
        // Check that we can make further changes and change default / nullable values
        transactionTemplate.execute(status -> {
            boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setName(Optional.of("new board 4"))
                    .setHandle(Optional.of("new-board-4"))
                    .setDefaultPostVisibility(Optional.of(PostVisibility.PUBLIC))
                    .setDescription(Optional.of("description 2"))
                    .setPostCategories(Optional.of(Arrays.asList("b2", "a2"))));
            return null;
        });
    
        // Check that we can clear nullable values
        transactionTemplate.execute(status -> {
            boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setDescription(Optional.empty())
                    .setPostCategories(Optional.empty()));
            return null;
        });
        
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.getBoard(boardId));
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> boardApi.getBoardOperations(boardId));
        Assert.assertEquals(5, resourceOperationRs.size());
    
        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        ResourceOperationRepresentation resourceOperationR0 = resourceOperationRs.get(0);
        ResourceOperationRepresentation resourceOperationR4 = resourceOperationRs.get(4);
    
        TestHelper.verifyResourceOperation(resourceOperationR0, Action.EXTEND, boardUser, null);
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "new board", "new board 2")
                .put("handle", "new-board", "new-board-2"));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "new board 2", "new board 3")
                .put("handle", "new-board-2", "new-board-3")
                .put("defaultPostVisibility", "PART_PRIVATE", "PRIVATE")
                .put("description", null, "description")
                .put("postCategories", null, Arrays.asList("a", "b")));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "new board 3", "new board 4")
                .put("handle", "new-board-3", "new-board-4")
                .put("defaultPostVisibility", "PRIVATE", "PUBLIC")
                .put("description", "description", "description 2")
                .put("postCategories", Arrays.asList("a", "b"), Arrays.asList("b2", "a2")));
    
        TestHelper.verifyResourceOperation(resourceOperationR4, Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("description", "description 2", null)
                .put("postCategories", Arrays.asList("b2", "a2"), null));
    
        Assert.assertEquals(resourceOperationR0.getCreatedTimestamp(), boardR.getCreatedTimestamp());
        Assert.assertEquals(resourceOperationR4.getCreatedTimestamp(), boardR.getUpdatedTimestamp());
        
        // Test that post administrator cannot view audit trail
        testUserService.setAuthentication(postUser.getStormpathId());
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> boardApi.getBoardOperations(boardId), ExceptionCode.FORBIDDEN_ACTION, status);
            return null;
        });
        
        // Test that a member of the public cannot view audit trail
        testUserService.unauthenticate();
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> boardApi.getBoardOperations(boardId), ExceptionCode.UNAUTHENTICATED_USER, status);
            return null;
        });
    }
    
    private Pair<BoardRepresentation, BoardRepresentation> postTwoBoards() {
        BoardRepresentation boardR1 = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("board")
                .setDepartment(new DepartmentDTO()
                    .setName("department"));
            
            return boardApi.postBoard(boardDTO);
        });
        
        BoardRepresentation boardR2 = transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("board")
                .setDepartment(new DepartmentDTO()
                    .setName("department 2"));
            
            return boardApi.postBoard(boardDTO);
        });
        
        return new Pair<>(boardR1, boardR2);
    }
    
    private BoardRepresentation verifyPostBoard(User user, BoardDTO boardDTO, String expectedHandle) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
    
            Assert.assertEquals(boardDTO.getName(), boardR.getName());
            Assert.assertEquals(expectedHandle, boardR.getHandle());
            Assert.assertEquals(boardDTO.getDescription(), boardR.getDescription());
            Assert.assertEquals(boardDTO.getPostCategories(), boardR.getPostCategories());
            Assert.assertEquals(PostVisibility.PART_PRIVATE, boardR.getDefaultPostVisibility());
    
            Board board = boardService.getBoard(boardR.getId());
            Department department = departmentService.getDepartment(boardR.getDepartment().getId());
            Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());
            Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND));
    
            Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
            Assert.assertTrue(userRoleService.hasUserRole(board, user, Role.ADMINISTRATOR));
            return boardR;
        });
    }
    
}
