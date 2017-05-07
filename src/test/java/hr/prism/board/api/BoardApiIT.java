package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
public class BoardApiIT extends AbstractIT {
    
    @Inject
    private DepartmentApi departmentApi;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Test
    public void shouldCreateAndListBoards() {
        User user1 = testUserService.authenticate();
        verifyPostBoard(user1, TestHelper.sampleBoard().setName("board 1"), "board-1");
        verifyPostBoard(user1, TestHelper.smallSampleBoard().setName("board 2"), "board-2");
    
        User user2 = testUserService.authenticate();
        verifyPostBoard(user1, TestHelper.sampleBoard().setName("board 3"), "board-3");
        verifyPostBoard(user1, TestHelper.smallSampleBoard().setName("board 4"), "board-4");
    
        testUserService.unauthenticate();
        Assert.assertEquals(0, transactionTemplate.execute(status -> departmentApi.getDepartments(null)).size());
        Assert.assertEquals(4, transactionTemplate.execute(status -> departmentApi.getDepartments(true)).size());
    
        testUserService.setAuthentication(user1.getStormpathId());
        List<BoardRepresentation> boardRs = transactionTemplate.execute(status -> boardApi.getBoards(null));
        Assert.assertEquals(2, boardRs.size());
        Assert.assertEquals(Arrays.asList("board 1", "board 2"), boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
        Assert.assertEquals(4, transactionTemplate.execute(status -> departmentApi.getDepartments(true)).size());
    
        testUserService.setAuthentication(user2.getStormpathId());
        boardRs = transactionTemplate.execute(status -> boardApi.getBoards(null));
        Assert.assertEquals(2, boardRs.size());
        Assert.assertEquals(Arrays.asList("board 3", "board 4"), boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
        Assert.assertEquals(4, transactionTemplate.execute(status -> departmentApi.getDepartments(true)).size());
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
            ExceptionUtils.verifyApiException(ApiException.class, () -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, status);
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
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
            ExceptionUtils.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setHandle(Optional.of(boardRs.getValue().getHandle()));
            ExceptionUtils.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE,
                status);
            return null;
        });
    }
    
    @Test
    public void shouldUpdateBoardHandleWhenUpdatingDepartmentHandle() {
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
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(departmentId, true);
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
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(department.getId(), true);
            Assert.assertEquals(2, boardRs.size());
            for (BoardRepresentation boardR : boardRs) {
                Assert.assertEquals("new-department-updated/board-" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                index++;
            }
            
            return null;
        });
    }
    
    @Test
    public void shouldSupportBoardLifeCycleAndPermissions() {
        // Create department and board
        User departmentUser = testUserService.authenticate();
        BoardRepresentation boardR = verifyPostBoard(departmentUser, TestHelper.smallSampleBoard(), "board");
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();
    
        User boardUser = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            userRoleService.createUserRole(boardId, boardUser.getId(), Role.ADMINISTRATOR);
            return null;
        });
    
        List<User> adminUsers = Arrays.asList(departmentUser, boardUser);
    
        // Create post
        User pUser = testUserService.authenticate();
        transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost()));
        
        // Create unprivileged users
        List<User> unprivilegedUsers = makeUnprivilegedUsers(departmentId, boardId, 1, TestHelper.smallSamplePost());
        unprivilegedUsers.add(pUser);
    
        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.AUDIT, () -> boardApi.getBoardOperations(boardId))
            .put(Action.EDIT, () -> boardApi.updateBoard(boardId, new BoardPatchDTO()))
            .build();
    
        verifyBoardActions(adminUsers, unprivilegedUsers, boardId, operations);
        
        // Test that we do not audit viewing
        transactionTemplate.execute(status -> boardApi.getBoard(boardId));
        
        // Check that we can make changes and leave nullable values null
        verifyPatchBoard(departmentUser, boardId,
            new BoardPatchDTO()
                .setName(Optional.of("board 2"))
                .setHandle(Optional.of("board-2")),
            State.ACCEPTED);
    
        verifyBoardActions(adminUsers, unprivilegedUsers, boardId, operations);
        
        // Check that we can make further changes and set default / nullable values
        verifyPatchBoard(boardUser, boardId,
            new BoardPatchDTO()
                .setName(Optional.of("board 3"))
                .setHandle(Optional.of("board-3"))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PRIVATE))
                .setDescription(Optional.of("description"))
                .setPostCategories(Optional.of(Arrays.asList("m1", "m2"))),
            State.ACCEPTED);
    
        verifyBoardActions(adminUsers, unprivilegedUsers, boardId, operations);
        
        // Check that we can make further changes and change default / nullable values
        verifyPatchBoard(departmentUser, boardId,
            new BoardPatchDTO()
                .setName(Optional.of("board 4"))
                .setHandle(Optional.of("board-4"))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PUBLIC))
                .setDescription(Optional.of("description 2"))
                .setPostCategories(Optional.of(Arrays.asList("m2", "m1"))),
            State.ACCEPTED);
    
        verifyBoardActions(adminUsers, unprivilegedUsers, boardId, operations);
        
        // Check that we can clear nullable values
        verifyPatchBoard(boardUser, boardId,
            new BoardPatchDTO()
                .setDescription(Optional.empty())
                .setPostCategories(Optional.empty()),
            State.ACCEPTED);
    
        verifyBoardActions(adminUsers, unprivilegedUsers, boardId, operations);
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> boardApi.getBoardOperations(boardId));
        Assert.assertEquals(5, resourceOperationRs.size());
    
        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, departmentUser);
        
        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "board", "board 2")
                .put("handle", "board", "board-2"));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "board 2", "board 3")
                .put("handle", "board-2", "board-3")
                .put("defaultPostVisibility", "PART_PRIVATE", "PRIVATE")
                .put("description", null, "description")
                .put("postCategories", null, Arrays.asList("m1", "m2")));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "board 3", "board 4")
                .put("handle", "board-3", "board-4")
                .put("defaultPostVisibility", "PRIVATE", "PUBLIC")
                .put("description", "description", "description 2")
                .put("postCategories", Arrays.asList("m1", "m2"), Arrays.asList("m2", "m1")));
    
        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("description", "description 2", null)
                .put("postCategories", Arrays.asList("m2", "m1"), null));
    }
    
    private Pair<BoardRepresentation, BoardRepresentation> verifyPostTwoBoards() {
        User user = testUserService.authenticate();
        BoardRepresentation boardR1 = verifyPostBoard(user, TestHelper.smallSampleBoard(), "board");
        BoardRepresentation boardR2 = verifyPostBoard(user,
            new BoardDTO()
                .setName("board 2")
                .setDepartment(new DepartmentDTO()
                    .setId(boardR1.getDepartment().getId())),
            "board-2");
        
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
    
            Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
            Assert.assertTrue(userRoleService.hasUserRole(board, user, Role.ADMINISTRATOR));
            return boardR;
        });
    }
    
    private BoardRepresentation verifyPatchBoard(User user, Long boardId, BoardPatchDTO boardDTO, State expectedState) {
        testUserService.setAuthentication(user.getStormpathId());
        return transactionTemplate.execute(status -> {
            Board board = boardService.getBoard(boardId);
            BoardRepresentation boardR = boardApi.updateBoard(boardId, boardDTO);
            
            Optional<String> nameOptional = boardDTO.getName();
            Assert.assertEquals(nameOptional == null ? board.getName() : nameOptional.orElse(null), boardR.getName());
            
            Optional<String> descriptionOptional = boardDTO.getDescription();
            Assert.assertEquals(descriptionOptional == null ? board.getDescription() : descriptionOptional.orElse(null), boardR.getDescription());
            
            Optional<String> handleOptional = boardDTO.getHandle();
            Assert.assertEquals(handleOptional == null ? board.getHandle().split("/")[1] : handleOptional.orElse(null), boardR.getHandle());
            
            Optional<List<String>> postCategoriesOptional = boardDTO.getPostCategories();
            Assert.assertEquals(postCategoriesOptional == null ? resourceService.getCategories(board, CategoryType.POST) : postCategoriesOptional.orElse(null),
                boardR.getPostCategories());
            
            Optional<PostVisibility> defaultVisibilityOptional = boardDTO.getDefaultPostVisibility();
            Assert.assertEquals(defaultVisibilityOptional == null ? board.getDefaultPostVisibility() : defaultVisibilityOptional.orElse(null),
                boardR.getDefaultPostVisibility());
            
            Assert.assertEquals(expectedState, boardR.getState());
            return boardR;
        });
    }
    
    private void verifyBoardActions(List<User> adminUsers, List<User> unprivilegedUsers, Long boardId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.BOARD, boardId, operations, Action.VIEW, Action.EXTEND);
        verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations, Action.VIEW, Action.EXTEND);
        verifyResourceActions(adminUsers, Scope.BOARD, boardId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND);
    }
    
}
