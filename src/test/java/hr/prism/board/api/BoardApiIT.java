package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
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
import org.apache.commons.collections.ListUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
public class BoardApiIT extends AbstractIT {
    
    private static LinkedHashMultimap<State, Action> ADMIN_ACTIONS = LinkedHashMultimap.create();
    
    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();
    
    static {
        ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.EXTEND));
        PUBLIC_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EXTEND));
    }
    
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
        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();
        
        User user11 = testUserService.authenticate();
        BoardDTO boardDTO11 = TestHelper.sampleBoard();
        boardDTO11.getDepartment().setName("department1");
        boardDTO11.setName("board11");
        BoardRepresentation boardR11 = verifyPostBoard(user11, boardDTO11, "board11");
        unprivilegedUsers.put("board11", makeUnprivilegedUsers(boardR11.getDepartment().getId(), boardR11.getId(), 110, 1100,
            TestHelper.samplePost()));
        
        User user12 = testUserService.authenticate();
        BoardDTO boardDTO12 = TestHelper.smallSampleBoard();
        boardDTO12.getDepartment().setName("department1");
        boardDTO12.setName("board12");
        BoardRepresentation boardR12 = verifyPostBoard(user11, boardDTO12, "board12");
        unprivilegedUsers.put("board12", makeUnprivilegedUsers(boardR12.getDepartment().getId(), boardR12.getId(), 120, 1200,
            TestHelper.smallSamplePost()
                .setMemberCategories(Collections.singletonList("m1"))));
        
        User user21 = testUserService.authenticate();
        BoardDTO boardDTO21 = TestHelper.smallSampleBoard();
        boardDTO21.getDepartment().setName("department2");
        boardDTO21.setName("board21");
        BoardRepresentation boardR21 = verifyPostBoard(user21, boardDTO21, "board21");
        unprivilegedUsers.put("board21", makeUnprivilegedUsers(boardR21.getDepartment().getId(), boardR21.getId(), 210, 2100,
            TestHelper.smallSamplePost()));
        
        User user22 = testUserService.authenticate();
        BoardDTO boardDTO22 = TestHelper.sampleBoard();
        boardDTO22.getDepartment().setName("department2");
        boardDTO22.setName("board22");
        BoardRepresentation boardR22 = verifyPostBoard(user22, boardDTO22, "board22");
        unprivilegedUsers.put("board22", makeUnprivilegedUsers(boardR22.getDepartment().getId(), boardR22.getId(), 220, 2200,
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("p1"))));
    
        List<String> boardNames = Arrays.asList(
            "board11", "board110", "board1100", "board12", "board120", "board1200", "board21", "board210", "board2100", "board22", "board220", "board2200");
        LinkedHashMultimap<Long, String> departmentBoardNames = LinkedHashMultimap.create();
        departmentBoardNames.putAll(boardR11.getDepartment().getId(), Arrays.asList("board11", "board1100", "board12", "board1200"));
        departmentBoardNames.putAll(boardR21.getDepartment().getId(), Arrays.asList("board21", "board2100", "board22", "board2200"));
        
        testUserService.unauthenticate();
        verifyUnprivilegedBoardUser(boardNames, departmentBoardNames);
        
        for (String boardName : unprivilegedUsers.keySet()) {
            Map<Scope, User> unprivilegedUserMap = unprivilegedUsers.get(boardName);
            for (Scope scope : unprivilegedUserMap.keySet()) {
                testUserService.setAuthentication(unprivilegedUserMap.get(scope).getStormpathId());
                if (scope == Scope.DEPARTMENT) {
                    verifyPrivilegedBoardUser(
                        boardNames, Collections.singletonList(boardName + "0"), departmentBoardNames);
                } else if (scope == Scope.BOARD) {
                    verifyPrivilegedBoardUser(
                        boardNames, Collections.singletonList(boardName + "00"), departmentBoardNames);
                } else {
                    verifyUnprivilegedBoardUser(boardNames, departmentBoardNames);
                }
            }
        }
    
        testUserService.setAuthentication(user11.getStormpathId());
        verifyPrivilegedBoardUser(boardNames, Arrays.asList("board11", "board1100", "board12", "board1200"), departmentBoardNames);
        
        testUserService.setAuthentication(user12.getStormpathId());
        verifyPrivilegedBoardUser(boardNames, Collections.singletonList("board12"), departmentBoardNames);
        
        testUserService.setAuthentication(user21.getStormpathId());
        verifyPrivilegedBoardUser(boardNames, Arrays.asList("board21", "board2100", "board22", "board2200"), departmentBoardNames);
        
        testUserService.setAuthentication(user22.getStormpathId());
        verifyPrivilegedBoardUser(boardNames, Collections.singletonList("board22"), departmentBoardNames);
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
        List<User> unprivilegedUsers = Lists.newArrayList(makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.smallSamplePost()).values());
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
    
    private void verifyBoardActions(List<User> adminUsers, Collection<User> unprivilegedUsers, Long boardId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.BOARD, boardId, operations, PUBLIC_ACTIONS.get(State.ACCEPTED));
        verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations, PUBLIC_ACTIONS.get(State.ACCEPTED));
        verifyResourceActions(adminUsers, Scope.BOARD, boardId, operations, ADMIN_ACTIONS.get(State.ACCEPTED));
    }
    
    private void verifyUnprivilegedBoardUser(List<String> boardNames, LinkedHashMultimap<Long, String> boardNamesByDepartment) {
        List<Action> publicActions = Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED));
        
        TestHelper.verifyResources(
            transactionTemplate.execute(status -> boardApi.getBoards(null)),
            Collections.emptyList(),
            null);
        
        TestHelper.ExpectedActions expectedActions = new TestHelper.ExpectedActions()
            .add(publicActions);
        TestHelper.verifyResources(
            transactionTemplate.execute(status -> boardApi.getBoards(true)),
            boardNames,
            expectedActions);
        
        for (Long departmentId : boardNamesByDepartment.keySet()) {
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> boardApi.getBoardsByDepartment(departmentId, null)),
                Collections.emptyList(),
                null);
            
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> boardApi.getBoardsByDepartment(departmentId, true)),
                Lists.newArrayList(boardNamesByDepartment.get(departmentId)),
                expectedActions);
        }
    }
    
    private void verifyPrivilegedBoardUser(List<String> boardNames, List<String> adminBoardNames, LinkedHashMultimap<Long, String> boardNamesByDepartment) {
        List<Action> adminActions = Lists.newArrayList(ADMIN_ACTIONS.get(State.ACCEPTED));
        List<Action> publicActions = Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED));
        
        TestHelper.verifyResources(
            transactionTemplate.execute(status -> boardApi.getBoards(null)),
            adminBoardNames,
            new TestHelper.ExpectedActions()
                .addAll(adminBoardNames, adminActions));
        
        TestHelper.verifyResources(
            transactionTemplate.execute(status -> boardApi.getBoards(true)),
            boardNames,
            new TestHelper.ExpectedActions()
                .add(publicActions)
                .addAll(adminBoardNames, adminActions));
        
        for (Long departmentId : boardNamesByDepartment.keySet()) {
            List<String> departmentBoardNames = Lists.newArrayList(boardNamesByDepartment.get(departmentId));
            @SuppressWarnings("unchecked") List<String> adminDepartmentBoardNames = ListUtils.intersection(departmentBoardNames, adminBoardNames);
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> boardApi.getBoardsByDepartment(departmentId, null)),
                adminDepartmentBoardNames,
                new TestHelper.ExpectedActions()
                    .addAll(adminDepartmentBoardNames, adminActions));
            
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> boardApi.getBoardsByDepartment(departmentId, true)),
                departmentBoardNames,
                new TestHelper.ExpectedActions()
                    .add(publicActions)
                    .addAll(adminDepartmentBoardNames, adminActions));
        }
    }
    
}
