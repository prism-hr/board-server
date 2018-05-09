//package hr.prism.board.api;
//
//import com.google.common.base.Joiner;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.LinkedHashMultimap;
//import com.google.common.collect.Lists;
//import hr.prism.board.ApiTestContext;
//import hr.prism.board.TestHelper;
//import hr.prism.board.domain.*;
//import hr.prism.board.dto.BoardDTO;
//import hr.prism.board.dto.BoardPatchDTO;
//import hr.prism.board.dto.DepartmentDTO;
//import hr.prism.board.dto.DepartmentPatchDTO;
//import hr.prism.board.enums.*;
//import hr.prism.board.exception.BoardDuplicateException;
//import hr.prism.board.exception.ExceptionCode;
//import hr.prism.board.exception.ExceptionUtils;
//import hr.prism.board.representation.BoardRepresentation;
//import hr.prism.board.representation.ChangeListRepresentation;
//import hr.prism.board.representation.DepartmentRepresentation;
//import hr.prism.board.representation.ResourceOperationRepresentation;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.collections.ListUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.assertj.core.api.Assertions;
//import org.hamcrest.Matchers;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static hr.prism.board.enums.State.ACCEPTED;
//import static java.util.Collections.singletonList;
//
//@ApiTestContext
//@RunWith(SpringRunner.class)
//public class BoardApiIT extends AbstractIT {
//
//    private static final List<String> DEFAULT_BOARD_NAMES = ImmutableList.of("Career Opportunities", "Research Opportunities");
//    private static final LinkedHashMultimap<State, Action> DEPARTMENT_ADMIN_ACTIONS = LinkedHashMultimap.create();
//    private static final LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();
//
//    static {
//        DEPARTMENT_ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT));
//        DEPARTMENT_ADMIN_ACTIONS.putAll(ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.EXTEND, Action.REJECT));
//        DEPARTMENT_ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.RESTORE));
//        PUBLIC_ACTIONS.putAll(ACCEPTED, Arrays.asList(Action.VIEW, Action.EXTEND));
//    }
//
//    @Test
//    public void shouldCreateAndListBoards() {
//        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//
//        User user11 = testUserService.authenticate();
//        Long departmentId1 =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department1").setSummary("department summary")).getId();
//
//        BoardDTO boardDTO11 = TestHelper.sampleBoard();
//        boardDTO11.setName("board11");
//        BoardRepresentation boardR11 = verifyPostBoard(departmentId1, boardDTO11, "board11");
//        unprivilegedUsers.put("board11", makeUnprivilegedUsers(boardR11.getId(), 110, TestHelper.samplePost()));
//
//        User user21 = testUserService.authenticate();
//        Long departmentId2 =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department2").setSummary("department summary")).getId();
//
//        BoardDTO boardDTO21 = TestHelper.smallSampleBoard();
//        boardDTO21.setName("board21");
//        BoardRepresentation boardR21 = verifyPostBoard(departmentId2, boardDTO21, "board21");
//        unprivilegedUsers.put("board21", makeUnprivilegedUsers(boardR21.getId(), 210,
//            TestHelper.smallSamplePost()
//                .setMemberCategories(singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));
//
//        List<String> boardNames = Arrays.asList("board11", "board110", "board21", "board210");
//        LinkedHashMultimap<Long, String> departmentBoardNames = LinkedHashMultimap.create();
//        departmentBoardNames.putAll(boardR11.getDepartment().getId(), singletonList("board11"));
//        departmentBoardNames.putAll(boardR21.getDepartment().getId(), singletonList("board21"));
//
//        testUserService.unauthenticate();
//        verifyUnprivilegedBoardUser(boardNames, departmentBoardNames);
//
//        for (String boardName : unprivilegedUsers.keySet()) {
//            Map<Scope, User> unprivilegedUserMap = unprivilegedUsers.get(boardName);
//            for (Scope scope : unprivilegedUserMap.keySet()) {
//                testUserService.setAuthentication(unprivilegedUserMap.get(scope));
//                if (scope == Scope.DEPARTMENT) {
//                    verifyPrivilegedBoardUser(boardNames, singletonList(boardName + "0"), departmentBoardNames, DEPARTMENT_ADMIN_ACTIONS.get(ACCEPTED));
//                } else if (scope == Scope.BOARD) {
//                    Assert.fail();
//                } else {
//                    verifyUnprivilegedBoardUser(boardNames, departmentBoardNames);
//                }
//            }
//        }
//
//        testUserService.setAuthentication(user11);
//        verifyPrivilegedBoardUser(boardNames, singletonList("board11"), departmentBoardNames, DEPARTMENT_ADMIN_ACTIONS.get(ACCEPTED));
//
//        testUserService.setAuthentication(user21);
//        verifyPrivilegedBoardUser(boardNames, singletonList("board21"), departmentBoardNames, DEPARTMENT_ADMIN_ACTIONS.get(ACCEPTED));
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateBoard() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department1").setSummary("department summary")).getId();
//        BoardDTO boardDTO = TestHelper.sampleBoard();
//
//        BoardRepresentation boardR = boardApi.createBoard(departmentId, boardDTO);
//        ExceptionUtils.verifyDuplicateException(() -> boardApi.createBoard(departmentId, boardDTO), ExceptionCode.DUPLICATE_BOARD, boardR.getId());
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateBoardHandle() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
//
//        BoardDTO boardDTO = new BoardDTO().setName("new board with long name");
//        verifyPostBoard(departmentId, boardDTO, "new-board-with-long-name");
//
//        Long boardId = verifyPostBoard(departmentId, boardDTO.setName("new board with long name too"), "new-board-with-long-name-2").getId();
//        BoardRepresentation boardR = boardApi.updateBoard(boardId,
//            new BoardPatchDTO()
//                .setHandle(Optional.of("new-board-with-longer-name")));
//        Assert.assertEquals("new-board-with-longer-name", boardR.getHandle());
//        verifyPostBoard(departmentId, boardDTO.setName("new board with long name also"), "new-board-with-long-name-2");
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateBoardByUpdating() {
//        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
//        BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
//        boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
//        ExceptionUtils.verifyDuplicateException(() ->
//            boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, boardRs.getValue().getId());
//    }
//
//    @Test
//    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
//        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
//        BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
//        boardPatchDTO.setHandle(Optional.of(boardRs.getValue().getHandle()));
//        ExceptionUtils.verifyException(BoardDuplicateException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE);
//    }
//
//    @Test
//    public void shouldUpdateBoardHandleWhenUpdatingDepartmentHandle() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
//
//        verifyPostBoard(departmentId, new BoardDTO().setName("board 1"), "board-1");
//        verifyPostBoard(departmentId, new BoardDTO().setName("board 2"), "board-2");
//
//        List<BoardRepresentation> boardRs = boardApi.getBoards(departmentId, true, null, null, null);
//        Assert.assertEquals(4, boardRs.size());
//
//        List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
//        Assert.assertThat(boardNames, Matchers.containsInAnyOrder("board 1", "board 2", "Career Opportunities", "Research Opportunities"));
//
//        departmentApi.updateDepartment(departmentId,
//            new DepartmentPatchDTO()
//                .setHandle(Optional.of("new-department-updated")));
//
//        Department department = departmentService.getByHandle(departmentId);
//        Assert.assertEquals("ucl/new-department-updated", department.getHandle());
//
//        int index = 1;
//        boardRs = boardApi.getBoards(department.getId(), true, null, null, null);
//        Assert.assertEquals(4, boardRs.size());
//        for (BoardRepresentation boardR : boardRs) {
//            String boardName = boardR.getName();
//            switch (boardName) {
//                case "Career Opportunities":
//                    Assert.assertEquals("new-department-updated/career-opportunities", boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
//                    break;
//                case "Research Opportunities":
//                    Assert.assertEquals("new-department-updated/research-opportunities", boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
//                    break;
//                default:
//                    Assert.assertEquals("new-department-updated/board-" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
//                    index++;
//                    break;
//            }
//        }
//    }
//
//    @Test
//    public void shouldSupportBoardActionsAndPermissions() {
//        // Create department and board
//        User departmentUser = testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
//        verifyPostBoard(departmentId, TestHelper.smallSampleBoard(), "board");
//
//        // Create a board in the draft state
//        listenForActivities(departmentUser);
//        Long boardId = boardApi.getBoards(departmentId, null, null, null, null).get(0).getId();
//
//        // Create unprivileged users
//        List<User> unprivilegedUsers = Lists.newArrayList(makeUnprivilegedUsers(2).values());
//        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
//            .put(Action.EDIT, () -> boardApi.updateBoard(boardId, new BoardPatchDTO()))
//            .put(Action.REJECT, () -> boardApi.performActionOnBoard(boardId, "reject", new BoardPatchDTO().setComment("comment")))
//            .put(Action.RESTORE, () -> boardApi.performActionOnBoard(boardId, "restore", new BoardPatchDTO()))
//            .build();
//
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, ACCEPTED, operations);
//
//        // Test that we do not audit viewing
//        boardApi.getBoard(boardId);
//
//        // Check that department user can reject the board
//        testUserService.setAuthentication(departmentUser);
//
//        // Check that the department user can reject the board
//        verifyExecuteBoard(boardId, departmentUser, "reject", "rejecting", State.REJECTED);
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, State.REJECTED, operations);
//        Assertions.assertThat(userActivityApi.getActivities()).isEmpty();
//
//        // Check that the department user can restore the board to accepted
//        verifyExecuteBoard(boardId, departmentUser, "restore", "restoring", ACCEPTED);
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, ACCEPTED, operations);
//        Assertions.assertThat(userActivityApi.getActivities()).isEmpty();
//
//        // Create post
//        User postUser = testUserService.authenticate();
//        postApi.postPost(boardId,
//            TestHelper.smallSamplePost().setMemberCategories(singletonList(MemberCategory.UNDERGRADUATE_STUDENT)));
//        unprivilegedUsers.add(postUser);
//
//        // Check that we can make changes and leave nullable values null
//        verifyPatchBoard(departmentUser, boardId,
//            new BoardPatchDTO()
//                .setName(Optional.of("board 2"))
//                .setHandle(Optional.of("board-2")));
//
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, ACCEPTED, operations);
//
//        // Check that we can make further changes and set default / nullable values
//        verifyPatchBoard(departmentUser, boardId,
//            new BoardPatchDTO()
//                .setName(Optional.of("board 3"))
//                .setHandle(Optional.of("board-3"))
//                .setPostCategories(Optional.of(Arrays.asList("m1", "m2"))));
//
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, ACCEPTED, operations);
//
//        // Check that we can make further changes and change default / nullable values
//        verifyPatchBoard(departmentUser, boardId,
//            new BoardPatchDTO()
//                .setName(Optional.of("board 4"))
//                .setHandle(Optional.of("board-4"))
//                .setPostCategories(Optional.of(Arrays.asList("m2", "m1"))));
//
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, ACCEPTED, operations);
//
//        // Check that we can clear nullable values
//        verifyPatchBoard(departmentUser, boardId,
//            new BoardPatchDTO()
//                .setPostCategories(Optional.empty()));
//
//        verifyBoardActions(departmentUser, unprivilegedUsers, boardId, ACCEPTED, operations);
//        List<ResourceOperationRepresentation> resourceOperationRs = boardApi.getBoardOperations(boardId);
//        Assert.assertEquals(7, resourceOperationRs.size());
//
//        // Operations are returned most recent first - reverse the order to make it easier to test
//        resourceOperationRs = Lists.reverse(resourceOperationRs);
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, departmentUser);
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.REJECT, departmentUser, "rejecting");
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.RESTORE, departmentUser, "restoring");
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("name", "board", "board 2")
//                .put("handle", "board", "board-2"));
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("name", "board 2", "board 3")
//                .put("handle", "board-2", "board-3")
//                .put("postCategories", new ArrayList<>(), Arrays.asList("m1", "m2")));
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(5), Action.EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("name", "board 3", "board 4")
//                .put("handle", "board-3", "board-4")
//                .put("postCategories", Arrays.asList("m1", "m2"), Arrays.asList("m2", "m1")));
//
//        TestHelper.verifyResourceOperation(resourceOperationRs.get(6), Action.EDIT, departmentUser,
//            new ChangeListRepresentation()
//                .put("postCategories", Arrays.asList("m2", "m1"), null));
//    }
//
//    private Pair<BoardRepresentation, BoardRepresentation> verifyPostTwoBoards() {
//        testUserService.authenticate();
//        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
//        Long departmentId =
//            departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
//        BoardRepresentation boardR1 = verifyPostBoard(departmentId, TestHelper.smallSampleBoard(), "board");
//        BoardRepresentation boardR2 = verifyPostBoard(departmentId, new BoardDTO().setName("board 2"), "board-2");
//        return Pair.of(boardR1, boardR2);
//    }
//
//    private BoardRepresentation verifyPostBoard(Long departmentId, BoardDTO boardDTO, String expectedHandle) {
//        BoardRepresentation boardR = boardApi.createBoard(departmentId, boardDTO);
//
//        Assert.assertEquals(boardDTO.getName(), boardR.getName());
//        Assert.assertEquals(expectedHandle, boardR.getHandle());
//        Assert.assertEquals(Optional.ofNullable(boardDTO.getPostCategories()).orElse(new ArrayList<>()), boardR.getPostCategories());
//
//        Board board = boardService.getByHandle(boardR.getId());
//        DepartmentRepresentation departmentR = boardR.getDepartment();
//        Department department = departmentService.getByHandle(departmentR.getId());
//        University university = universityService.getByHandle(departmentR.getUniversity().getId());
//        Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());
//
//        List<ResourceRelation> parents = resourceRelationRepository.findByResource2(board);
//        Assert.assertThat(parents.stream().map(ResourceRelation::getResource1).collect(Collectors.toList()),
//            Matchers.containsInAnyOrder(board, department, university));
//        return boardR;
//    }
//
//    private void verifyExecuteBoard(Long boardId, User departmentUser, String action, String comment, State expectedState) {
//        testUserService.setAuthentication(departmentUser);
//        BoardRepresentation boardR = boardApi.performActionOnBoard(boardId, action, new BoardPatchDTO().setComment(comment));
//        Assert.assertEquals(expectedState, boardR.getState());
//    }
//
//    private void verifyPatchBoard(User user, Long boardId, BoardPatchDTO boardDTO) {
//        testUserService.setAuthentication(user);
//        Board board = boardService.getByHandle(boardId);
//        BoardRepresentation boardR = boardApi.updateBoard(boardId, boardDTO);
//
//        Optional<String> nameOptional = boardDTO.getName();
//        Assert.assertEquals(nameOptional == null ? board.getName() : nameOptional.orElse(null), boardR.getName());
//
//        Optional<String> handleOptional = boardDTO.getHandle();
//        Assert.assertEquals(handleOptional == null ? board.getHandle().split("/")[2] : handleOptional.orElse(null), boardR.getHandle());
//
//        Optional<List<String>> postCategoriesOptional = boardDTO.getPostCategories();
//        Assert.assertEquals(postCategoriesOptional == null ? resourceService.getCategories(board, CategoryType.POST) : postCategoriesOptional.orElse(new ArrayList<>()),
//            boardR.getPostCategories());
//
//        Assert.assertEquals(ACCEPTED, boardR.getState());
//    }
//
//    private void verifyBoardActions(User departmentAdmin, Collection<User> unprivilegedUsers, Long boardId, State state, Map<Action, Runnable> operations) {
//        Collection<Action> publicActions = PUBLIC_ACTIONS.get(state);
//        if (CollectionUtils.isEmpty(publicActions)) {
//            verifyResourceActions(Scope.BOARD, boardId, operations);
//            verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations);
//        } else {
//            verifyResourceActions(Scope.BOARD, boardId, operations, publicActions);
//            verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations, publicActions);
//        }
//
//        verifyResourceActions(Scope.BOARD, boardId, operations, PUBLIC_ACTIONS.get(state));
//        verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations, PUBLIC_ACTIONS.get(state));
//        verifyResourceActions(departmentAdmin, Scope.BOARD, boardId, operations, DEPARTMENT_ADMIN_ACTIONS.get(state));
//    }
//
//    private void verifyUnprivilegedBoardUser(List<String> boardNames, LinkedHashMultimap<Long, String> boardNamesByDepartment) {
//        List<Action> publicActions = Lists.newArrayList(PUBLIC_ACTIONS.get(ACCEPTED));
//
//        TestHelper.verifyResources(
//            boardApi.getBoards(null, null, null, null, null)
//                .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                .collect(Collectors.toList()),
//            Collections.emptyList(),
//            null);
//
//        TestHelper.ExpectedActions expectedActions = new TestHelper.ExpectedActions()
//            .add(publicActions);
//        TestHelper.verifyResources(
//            boardApi.getBoards(null, true, null, null, null)
//                .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                .collect(Collectors.toList()),
//            boardNames,
//            expectedActions);
//
//        for (Long departmentId : boardNamesByDepartment.keySet()) {
//            TestHelper.verifyResources(
//                boardApi.getBoards(departmentId, null, null, null, null)
//                    .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                    .collect(Collectors.toList()),
//                Collections.emptyList(),
//                null);
//
//            TestHelper.verifyResources(
//                boardApi.getBoards(departmentId, true, null, null, null)
//                    .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                    .collect(Collectors.toList()),
//                Lists.newArrayList(boardNamesByDepartment.get(departmentId)),
//                expectedActions);
//        }
//    }
//
//    private void verifyPrivilegedBoardUser(List<String> boardNames, List<String> adminBoardNames, LinkedHashMultimap<Long, String> boardNamesByDepartment, Set<Action> adminActions) {
//        List<Action> adminActionList = Lists.newArrayList(adminActions);
//        List<Action> publicActionList = Lists.newArrayList(PUBLIC_ACTIONS.get(ACCEPTED));
//
//        TestHelper.verifyResources(
//            boardApi.getBoards(null, null, null, null, null)
//                .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                .collect(Collectors.toList()),
//            adminBoardNames,
//            new TestHelper.ExpectedActions()
//                .addAll(adminBoardNames, adminActionList));
//
//        TestHelper.verifyResources(
//            boardApi.getBoards(null, true, null, null, null)
//                .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                .collect(Collectors.toList()),
//            boardNames,
//            new TestHelper.ExpectedActions()
//                .add(publicActionList)
//                .addAll(adminBoardNames, adminActionList));
//
//        for (Long departmentId : boardNamesByDepartment.keySet()) {
//            List<String> departmentBoardNames = Lists.newArrayList(boardNamesByDepartment.get(departmentId));
//            @SuppressWarnings("unchecked") List<String> adminDepartmentBoardNames = ListUtils.intersection(departmentBoardNames, adminBoardNames);
//            TestHelper.verifyResources(
//                boardApi.getBoards(departmentId, null, null, null, null)
//                    .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                    .collect(Collectors.toList()),
//                adminDepartmentBoardNames,
//                new TestHelper.ExpectedActions()
//                    .addAll(adminDepartmentBoardNames, adminActionList));
//
//            TestHelper.verifyResources(
//                boardApi.getBoards(departmentId, true, null, null, null)
//                    .stream().filter(board -> !DEFAULT_BOARD_NAMES.contains(board.getName()))
//                    .collect(Collectors.toList()),
//                departmentBoardNames,
//                new TestHelper.ExpectedActions()
//                    .add(publicActionList)
//                    .addAll(adminDepartmentBoardNames, adminActionList));
//        }
//    }
//
//}
