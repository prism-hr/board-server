package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.ObjectUtils;
import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
public class BoardApiIT extends AbstractIT {

    private static LinkedHashMultimap<State, Action> DEPARTMENT_ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> BOARD_ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        DEPARTMENT_ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.ACCEPT, Action.REJECT));
        DEPARTMENT_ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.EXTEND, Action.REJECT));
        DEPARTMENT_ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.RESTORE));

        BOARD_ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT));
        BOARD_ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.EXTEND));
        BOARD_ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.AUDIT));

        PUBLIC_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EXTEND));
    }

    @Test
    public void shouldCreateAndListBoards() {
        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();

        User user11 = testUserService.authenticate();
        BoardDTO boardDTO11 = TestHelper.sampleBoard();
        boardDTO11.getDepartment().setName("department1");
        ((BoardDTO) boardDTO11.setName("board11")).setDocumentLogo(new DocumentDTO().setCloudinaryId("board1logo").setCloudinaryUrl("board1logo").setFileName("board1logo"));
        BoardRepresentation boardR11 = verifyPostBoard(boardDTO11, "board11");
        unprivilegedUsers.put("board11", makeUnprivilegedUsers(boardR11.getDepartment().getId(), boardR11.getId(), 110, 1100,
            TestHelper.samplePost()));

        User user12 = testUserService.authenticate();
        BoardDTO boardDTO12 = TestHelper.smallSampleBoard();
        boardDTO12.getDepartment().setName("department1");
        ((BoardDTO) boardDTO12.setName("board12")).setDocumentLogo(new DocumentDTO().setCloudinaryId("board2logo").setCloudinaryUrl("board2logo").setFileName("board2logo"));
        BoardRepresentation boardR12 = verifyPostBoard(boardDTO12, "board12");
        testUserService.setAuthentication(user11.getId());
        boardApi.executeAction(boardR12.getId(), "accept", new BoardPatchDTO());
        testUserService.setAuthentication(user12.getId());
        unprivilegedUsers.put("board12", makeUnprivilegedUsers(boardR12.getDepartment().getId(), boardR12.getId(), 120, 1200,
            TestHelper.smallSamplePost()
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));

        User user21 = testUserService.authenticate();
        BoardDTO boardDTO21 = TestHelper.smallSampleBoard();
        boardDTO21.getDepartment().setName("department2");
        boardDTO21.setName("board21");
        BoardRepresentation boardR21 = verifyPostBoard(boardDTO21, "board21");
        unprivilegedUsers.put("board21", makeUnprivilegedUsers(boardR21.getDepartment().getId(), boardR21.getId(), 210, 2100,
            TestHelper.smallSamplePost()));

        User user22 = testUserService.authenticate();
        BoardDTO boardDTO22 = TestHelper.sampleBoard();
        boardDTO22.getDepartment().setName("department2");
        boardDTO22.setName("board22");
        BoardRepresentation boardR22 = verifyPostBoard(boardDTO22, "board22");
        testUserService.setAuthentication(user21.getId());
        boardApi.executeAction(boardR22.getId(), "accept", new BoardPatchDTO());
        testUserService.setAuthentication(user22.getId());
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
                testUserService.setAuthentication(unprivilegedUserMap.get(scope).getId());
                if (scope == Scope.DEPARTMENT) {
                    verifyPrivilegedBoardUser(boardNames, Collections.singletonList(boardName + "0"), departmentBoardNames, DEPARTMENT_ADMIN_ACTIONS.get(State.ACCEPTED));
                } else if (scope == Scope.BOARD) {
                    verifyPrivilegedBoardUser(boardNames, Collections.singletonList(boardName + "00"), departmentBoardNames, BOARD_ADMIN_ACTIONS.get(State.ACCEPTED));
                } else {
                    verifyUnprivilegedBoardUser(boardNames, departmentBoardNames);
                }
            }
        }

        testUserService.setAuthentication(user11.getId());
        verifyPrivilegedBoardUser(boardNames, Arrays.asList("board11", "board1100", "board12", "board1200"), departmentBoardNames, DEPARTMENT_ADMIN_ACTIONS.get(State.ACCEPTED));

        testUserService.setAuthentication(user12.getId());
        verifyPrivilegedBoardUser(boardNames, Collections.singletonList("board12"), departmentBoardNames, BOARD_ADMIN_ACTIONS.get(State.ACCEPTED));

        testUserService.setAuthentication(user21.getId());
        verifyPrivilegedBoardUser(boardNames, Arrays.asList("board21", "board2100", "board22", "board2200"), departmentBoardNames, DEPARTMENT_ADMIN_ACTIONS.get(State.ACCEPTED));

        testUserService.setAuthentication(user22.getId());
        verifyPrivilegedBoardUser(boardNames, Collections.singletonList("board22"), departmentBoardNames, BOARD_ADMIN_ACTIONS.get(State.ACCEPTED));
    }

    @Test
    public void shouldNotCreateDuplicateBoard() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO));
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyDuplicateException(() -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, boardR.getId(), status);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        testUserService.authenticate();
        BoardDTO boardDTO = ((BoardDTO) new BoardDTO()
            .setName("new board with long name"))
            .setDepartment((DepartmentDTO) new DepartmentDTO()
                .setName("new department"));
        verifyPostBoard(boardDTO, "new-board-with-long");
        Long boardId = verifyPostBoard((BoardDTO) boardDTO.setName("new board with long name too"), "new-board-with-long-2").getId();

        transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setHandle(Optional.of("new-board-with-long-name")));
            Assert.assertEquals("new-board-with-long-name", boardR.getHandle());
            return null;
        });

        verifyPostBoard((BoardDTO) boardDTO.setName("new board with long name also"), "new-board-with-long-2");
    }

    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
            ExceptionUtils.verifyDuplicateException(() ->
                boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, boardRs.getValue().getId(), status);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setHandle(Optional.of(boardRs.getValue().getHandle()));
            ExceptionUtils.verifyException(BoardException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE,
                status);
            return null;
        });
    }

    @Test
    public void shouldUpdateBoardHandleWhenUpdatingDepartmentHandle() {
        testUserService.authenticate();
        Long departmentId = verifyPostBoard(
            ((BoardDTO) new BoardDTO()
                .setName("board 1"))
                .setDepartment((DepartmentDTO) new DepartmentDTO()
                    .setName("department")),
            "board-1")
            .getDepartment().getId();

        verifyPostBoard(
            ((BoardDTO) new BoardDTO()
                .setName("board 2"))
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
        BoardRepresentation boardR = verifyPostBoard(TestHelper.smallSampleBoard(), "board");
        DepartmentRepresentation departmentR = boardR.getDepartment();
        Long departmentId = departmentR.getId();

        // Create a board in the draft state
        testNotificationService.record();
        User boardUser = testUserService.authenticate();
        boardR = verifyPostBoard(
            ((BoardDTO) TestHelper.smallSampleBoard()
                .setName("board 1"))
                .setDepartment(
                    new DepartmentDTO()
                        .setId(departmentId)),
            "board-1");
        Long boardId = boardR.getId();
        testNotificationService.stop();

        String departmentUserGivenName = departmentUser.getGivenName();
        String departmentName = departmentR.getName();
        String boardUserGivenName = boardUser.getGivenName();
        String resourceRedirect = environment.getProperty("server.url") + "/redirect?resource=" + boardId;
        String homeRedirect = environment.getProperty("server.url") + "/redirect";

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.NEW_BOARD_PARENT, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", departmentUserGivenName).put("department", departmentName).put("resourceRedirect", resourceRedirect)
                    .put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_BOARD, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("resourceRedirect", resourceRedirect)
                    .put("modal", "Login").build()));

        // Create post
        User postUser = testUserService.authenticate();
        transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost()));

        // Create unprivileged users
        List<User> unprivilegedUsers = Lists.newArrayList(makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.smallSamplePost()).values());
        unprivilegedUsers.add(postUser);

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.AUDIT, () -> boardApi.getBoardOperations(boardId))
            .put(Action.EDIT, () -> boardApi.updateBoard(boardId, new BoardPatchDTO()))
            .put(Action.ACCEPT, () -> boardApi.executeAction(boardId, "accept", new BoardPatchDTO()))
            .put(Action.REJECT, () -> boardApi.executeAction(boardId, "reject", new BoardPatchDTO()))
            .put(Action.RESTORE, () -> boardApi.executeAction(boardId, "restore", new BoardPatchDTO()))
            .build();

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.DRAFT, operations);

        // Test that we do not audit viewing
        transactionTemplate.execute(status -> boardApi.getBoard(boardId));

        // Check that department user can reject the board
        testNotificationService.record();
        Long departmentUserId = departmentUser.getId();
        verifyExecuteBoard(boardId, departmentUserId, "reject", "we cannot accept this", State.REJECTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.REJECTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_BOARD, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("comment", "we cannot accept this")
                    .put("homeRedirect", homeRedirect).put("modal", "Login").build()));

        // Check that the department user can restore the board to draft
        verifyExecuteBoard(boardId, departmentUserId, "restore", "we made a mistake", State.DRAFT);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.DRAFT, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESTORE_BOARD, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("resourceRedirect", resourceRedirect)
                .put("modal", "Login").build()));

        // Check that the department user can accept the board
        verifyExecuteBoard(boardId, departmentUserId, "accept", null, State.ACCEPTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.ACCEPT_BOARD, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("resourceRedirect", resourceRedirect)
                .put("modal", "Login").build()));

        // Check that the department user can reject the board
        verifyExecuteBoard(boardId, departmentUserId, "reject", "we really cannot accept this", State.REJECTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.REJECTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_BOARD, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("comment", "we really cannot accept this")
                .put("homeRedirect", homeRedirect).put("modal", "Login").build()));

        // Check that the department user can restore the board to accepted
        verifyExecuteBoard(boardId, departmentUserId, "restore", "we made another mistake", State.ACCEPTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);
        testNotificationService.stop();

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.ACCEPT_BOARD, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("resourceRedirect", resourceRedirect)
                .put("modal", "Login").build()));

        // Check that we can make changes and leave nullable values null
        verifyPatchBoard(departmentUser, boardId,
            ((BoardPatchDTO) new BoardPatchDTO()
                .setName(Optional.of("board 2")))
                .setHandle(Optional.of("board-2"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("logo 1").setCloudinaryUrl("logo 1").setFileName("logo 1"))),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        // Check that we can make further changes and set default / nullable values
        verifyPatchBoard(boardUser, boardId,
            ((BoardPatchDTO) new BoardPatchDTO()
                .setName(Optional.of("board 3"))
                .setSummary(Optional.of("summary")))
                .setHandle(Optional.of("board-3"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("logo 2").setCloudinaryUrl("logo 2").setFileName("logo 2")))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PRIVATE))
                .setPostCategories(Optional.of(Arrays.asList("m1", "m2"))),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        // Check that we can make further changes and change default / nullable values
        verifyPatchBoard(departmentUser, boardId,
            ((BoardPatchDTO) new BoardPatchDTO()
                .setName(Optional.of("board 4"))
                .setSummary(Optional.of("summary 2")))
                .setHandle(Optional.of("board-4"))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PUBLIC))
                .setPostCategories(Optional.of(Arrays.asList("m2", "m1"))),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        // Check that we can clear nullable values
        verifyPatchBoard(boardUser, boardId,
            ((BoardPatchDTO) new BoardPatchDTO()
                .setSummary(Optional.empty()))
                .setPostCategories(Optional.empty()),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> boardApi.getBoardOperations(boardId));
        Assert.assertEquals(10, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, departmentUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.REJECT, departmentUser, "we cannot accept this");
        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.RESTORE, departmentUser, "we made a mistake");
        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.ACCEPT, departmentUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.REJECT, departmentUser, "we really cannot accept this");
        TestHelper.verifyResourceOperation(resourceOperationRs.get(5), Action.ACCEPT, departmentUser, "we made another mistake");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(6), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "board", "board 2")
                .put("handle", "board", "board-2")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "logo 1", "cloudinaryUrl", "logo 1", "fileName", "logo 1")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(7), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("name", "board 2", "board 3")
                .put("handle", "board-2", "board-3")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "logo 1", "cloudinaryUrl", "logo 1", "fileName", "logo 1"),
                    ObjectUtils.orderedMap("cloudinaryId", "logo 2", "cloudinaryUrl", "logo 2", "fileName", "logo 2"))
                .put("defaultPostVisibility", "PART_PRIVATE", "PRIVATE")
                .put("summary", null, "summary")
                .put("postCategories", new ArrayList<>(), Arrays.asList("m1", "m2")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(8), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("name", "board 3", "board 4")
                .put("handle", "board-3", "board-4")
                .put("defaultPostVisibility", "PRIVATE", "PUBLIC")
                .put("summary", "summary", "summary 2")
                .put("postCategories", Arrays.asList("m1", "m2"), Arrays.asList("m2", "m1")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(9), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("summary", "summary 2", null)
                .put("postCategories", Arrays.asList("m2", "m1"), null));
    }

    @Test
    public void shouldPostAndReplaceLogo() {
        // Create department and board
        User user = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.smallSampleBoard();
        DocumentDTO initialLogo = new DocumentDTO().setCloudinaryId("postingLogo").setCloudinaryUrl("postingLogo").setFileName("postingLogo");
        boardDTO.setDocumentLogo(initialLogo);
        boardDTO.getDepartment().setDocumentLogo(initialLogo);
        BoardRepresentation boardR = verifyPostBoard(boardDTO, "board");
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();

        // Check that we can make further changes and set default / nullable values
        DocumentDTO replacingLogo = new DocumentDTO().setCloudinaryId("replacingLogo").setCloudinaryUrl("replacingLogo").setFileName("replacingLogo");
        verifyPatchBoard(user, boardId,
            new BoardPatchDTO()
                .setDocumentLogo(Optional.of(replacingLogo)),
            State.ACCEPTED);

        DepartmentRepresentation department = departmentApi.getDepartment(departmentId);
        verifyDocument(initialLogo, department.getDocumentLogo());
    }

    private Pair<BoardRepresentation, BoardRepresentation> verifyPostTwoBoards() {
        testUserService.authenticate();
        BoardRepresentation boardR1 = verifyPostBoard(TestHelper.smallSampleBoard(), "board");
        BoardRepresentation boardR2 = verifyPostBoard(
            ((BoardDTO) new BoardDTO()
                .setName("board 2"))
                .setDepartment(new DepartmentDTO()
                    .setId(boardR1.getDepartment().getId())),
            "board-2");

        return new Pair<>(boardR1, boardR2);
    }

    private BoardRepresentation verifyPostBoard(BoardDTO boardDTO, String expectedHandle) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);

            Assert.assertEquals(boardDTO.getName(), boardR.getName());
            Assert.assertEquals(expectedHandle, boardR.getHandle());
            Assert.assertEquals(boardDTO.getSummary(), boardR.getSummary());
            verifyDocument(boardDTO.getDocumentLogo(), boardR.getDocumentLogo());
            Assert.assertEquals(Optional.ofNullable(boardDTO.getPostCategories()).orElse(new ArrayList<>()), boardR.getPostCategories());
            Assert.assertEquals(PostVisibility.PART_PRIVATE, boardR.getDefaultPostVisibility());

            Board board = boardService.getBoard(boardR.getId());
            Department department = departmentService.getDepartment(boardR.getDepartment().getId());
            Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());

            Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
            return boardR;
        });
    }

    private void verifyExecuteBoard(Long boardId, Long departmentUserId, String action, String comment, State expectedState) {
        BoardRepresentation boardR;
        testUserService.setAuthentication(departmentUserId);
        boardR = transactionTemplate.execute(status ->
            boardApi.executeAction(boardId, action, (BoardPatchDTO) new BoardPatchDTO().setComment(comment)));
        Assert.assertEquals(expectedState, boardR.getState());
    }

    private BoardRepresentation verifyPatchBoard(User user, Long boardId, BoardPatchDTO boardDTO, State expectedState) {
        testUserService.setAuthentication(user.getId());
        Board board = transactionTemplate.execute(status -> boardService.getBoard(boardId));
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.updateBoard(boardId, boardDTO));

        return transactionTemplate.execute(status -> {
            Optional<String> nameOptional = boardDTO.getName();
            Assert.assertEquals(nameOptional == null ? board.getName() : nameOptional.orElse(null), boardR.getName());

            Optional<DocumentDTO> documentLogoOptional = boardDTO.getDocumentLogo();
            verifyDocument(documentLogoOptional == null ? board.getDocumentLogo() : boardDTO.getDocumentLogo().orElse(null), boardR.getDocumentLogo());

            Optional<String> summaryOptional = boardDTO.getSummary();
            Assert.assertEquals(summaryOptional == null ? board.getSummary() : summaryOptional.orElse(null), boardR.getSummary());

            Optional<String> handleOptional = boardDTO.getHandle();
            Assert.assertEquals(handleOptional == null ? board.getHandle().split("/")[1] : handleOptional.orElse(null), boardR.getHandle());

            Optional<List<String>> postCategoriesOptional = boardDTO.getPostCategories();
            Assert.assertEquals(postCategoriesOptional == null ? resourceService.getCategories(board, CategoryType.POST) : postCategoriesOptional.orElse(new ArrayList<>()),
                boardR.getPostCategories());

            Optional<PostVisibility> defaultVisibilityOptional = boardDTO.getDefaultPostVisibility();
            Assert.assertEquals(defaultVisibilityOptional == null ? board.getDefaultPostVisibility() : defaultVisibilityOptional.orElse(null),
                boardR.getDefaultPostVisibility());

            Assert.assertEquals(expectedState, boardR.getState());
            return boardR;
        });
    }

    private void verifyBoardActions(User departmentAdmin, User boardAdmin, Collection<User> unprivilegedUsers, Long boardId, State state, Map<Action, Runnable> operations) {
        Collection<Action> publicActions = PUBLIC_ACTIONS.get(state);
        if (CollectionUtils.isEmpty(publicActions)) {
            verifyResourceActions(Scope.BOARD, boardId, operations);
            verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations);
        } else {
            verifyResourceActions(Scope.BOARD, boardId, operations, publicActions);
            verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations, publicActions);
        }

        verifyResourceActions(Scope.BOARD, boardId, operations, PUBLIC_ACTIONS.get(state));
        verifyResourceActions(unprivilegedUsers, Scope.BOARD, boardId, operations, PUBLIC_ACTIONS.get(state));
        verifyResourceActions(departmentAdmin, Scope.BOARD, boardId, operations, DEPARTMENT_ADMIN_ACTIONS.get(state));
        verifyResourceActions(boardAdmin, Scope.BOARD, boardId, operations, BOARD_ADMIN_ACTIONS.get(state));
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

    private void verifyPrivilegedBoardUser(List<String> boardNames, List<String> adminBoardNames, LinkedHashMultimap<Long, String> boardNamesByDepartment, Set<Action> adminActions) {
        List<Action> adminActionList = Lists.newArrayList(adminActions);
        List<Action> publicActionList = Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED));

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> boardApi.getBoards(null)),
            adminBoardNames,
            new TestHelper.ExpectedActions()
                .addAll(adminBoardNames, adminActionList));

        TestHelper.verifyResources(
            transactionTemplate.execute(status -> boardApi.getBoards(true)),
            boardNames,
            new TestHelper.ExpectedActions()
                .add(publicActionList)
                .addAll(adminBoardNames, adminActionList));

        for (Long departmentId : boardNamesByDepartment.keySet()) {
            List<String> departmentBoardNames = Lists.newArrayList(boardNamesByDepartment.get(departmentId));
            @SuppressWarnings("unchecked") List<String> adminDepartmentBoardNames = ListUtils.intersection(departmentBoardNames, adminBoardNames);
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> boardApi.getBoardsByDepartment(departmentId, null)),
                adminDepartmentBoardNames,
                new TestHelper.ExpectedActions()
                    .addAll(adminDepartmentBoardNames, adminActionList));

            TestHelper.verifyResources(
                transactionTemplate.execute(status -> boardApi.getBoardsByDepartment(departmentId, true)),
                departmentBoardNames,
                new TestHelper.ExpectedActions()
                    .add(publicActionList)
                    .addAll(adminDepartmentBoardNames, adminActionList));
        }
    }

}
