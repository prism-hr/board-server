package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.enums.Activity;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.TestWebSocketService;
import hr.prism.board.util.ObjectUtils;
import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
public class BoardApiIT extends AbstractIT {

    private static LinkedHashMultimap<State, Action> DEPARTMENT_ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> BOARD_ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        DEPARTMENT_ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT));
        DEPARTMENT_ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.EXTEND, Action.REJECT));
        DEPARTMENT_ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.RESTORE));

        BOARD_ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT));
        BOARD_ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.EXTEND));
        BOARD_ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT));

        PUBLIC_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EXTEND));
    }

    @Test
    public void shouldCreateAndListBoards() {
        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());

        User user11 = testUserService.authenticate();
        Long departmentId1 = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department1").setSummary("department summary"))).getId();

        BoardDTO boardDTO11 = TestHelper.sampleBoard();
        boardDTO11.setName("board11").setDocumentLogo(new DocumentDTO().setCloudinaryId("board1logo").setCloudinaryUrl("board1logo").setFileName("board1logo"));
        BoardRepresentation boardR11 = verifyPostBoard(departmentId1, boardDTO11, "board11");
        unprivilegedUsers.put("board11", makeUnprivilegedUsers(boardR11.getDepartment().getId(), boardR11.getId(), 110, 1100,
            TestHelper.samplePost()));

        User user12 = testUserService.authenticate();
        BoardDTO boardDTO12 = TestHelper.smallSampleBoard();
        boardDTO12.setName("board12").setDocumentLogo(new DocumentDTO().setCloudinaryId("board2logo").setCloudinaryUrl("board2logo").setFileName("board2logo"));
        BoardRepresentation boardR12 = verifyPostBoard(departmentId1, boardDTO12, "board12");
        testUserService.setAuthentication(user11.getId());
        boardApi.executeAction(boardR12.getId(), "accept", new BoardPatchDTO());
        testUserService.setAuthentication(user12.getId());
        unprivilegedUsers.put("board12", makeUnprivilegedUsers(boardR12.getDepartment().getId(), boardR12.getId(), 120, 1200,
            TestHelper.smallSamplePost()
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));

        User user21 = testUserService.authenticate();
        Long departmentId2 = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department2").setSummary("department summary"))).getId();

        BoardDTO boardDTO21 = TestHelper.smallSampleBoard();
        boardDTO21.setName("board21");
        BoardRepresentation boardR21 = verifyPostBoard(departmentId2, boardDTO21, "board21");
        unprivilegedUsers.put("board21", makeUnprivilegedUsers(boardR21.getDepartment().getId(), boardR21.getId(), 210, 2100,
            TestHelper.smallSamplePost()
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));

        User user22 = testUserService.authenticate();
        BoardDTO boardDTO22 = TestHelper.sampleBoard();
        boardDTO22.setName("board22");
        BoardRepresentation boardR22 = verifyPostBoard(departmentId2, boardDTO22, "board22");
        testUserService.setAuthentication(user21.getId());
        boardApi.executeAction(boardR22.getId(), "accept", new BoardPatchDTO());
        testUserService.setAuthentication(user22.getId());
        unprivilegedUsers.put("board22", makeUnprivilegedUsers(boardR22.getDepartment().getId(), boardR22.getId(), 220, 2200,
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));

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
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department1").setSummary("department summary"))).getId();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(departmentId, boardDTO));
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyDuplicateException(() -> boardApi.postBoard(departmentId, boardDTO), ExceptionCode.DUPLICATE_BOARD, boardR.getId(), status);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        testUserService.authenticate();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary"))).getId();

        BoardDTO boardDTO = new BoardDTO().setName("new board with long name");
        verifyPostBoard(departmentId, boardDTO, "new-board-with-long-name");
        Long boardId = verifyPostBoard(departmentId, boardDTO.setName("new board with long name too"), "new-board-with-long-name-2").getId();

        transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.patchBoard(boardId,
                new BoardPatchDTO()
                    .setHandle(Optional.of("new-board-with-longer-name")));
            Assert.assertEquals("new-board-with-longer-name", boardR.getHandle());
            return null;
        });

        verifyPostBoard(departmentId, boardDTO.setName("new board with long name also"), "new-board-with-long-name-2");
    }

    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
            ExceptionUtils.verifyDuplicateException(() ->
                boardApi.patchBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, boardRs.getValue().getId(), status);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setHandle(Optional.of(boardRs.getValue().getHandle()));
            ExceptionUtils.verifyException(BoardException.class, () -> boardApi.patchBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE,
                status);
            return null;
        });
    }

    @Test
    public void shouldUpdateBoardHandleWhenUpdatingDepartmentHandle() {
        testUserService.authenticate();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary"))).getId();

        verifyPostBoard(departmentId, new BoardDTO().setName("board 1"), "board-1");
        verifyPostBoard(departmentId, new BoardDTO().setName("board 2"), "board-2");

        transactionTemplate.execute(status -> {
            List<BoardRepresentation> boardRs = boardApi.getBoards(departmentId, true, null, null, null);
            Assert.assertEquals(4, boardRs.size());

            List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("board 1", "board 2", "Career Opportunities", "Research Opportunities"));

            departmentApi.patchDepartment(departmentId,
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-updated")));
            return null;
        });

        transactionTemplate.execute(status -> {
            Department department = departmentService.getDepartment(departmentId);
            Assert.assertEquals("ucl/new-department-updated", department.getHandle());

            int index = 1;
            List<BoardRepresentation> boardRs = boardApi.getBoards(department.getId(), true, null, null, null);
            Assert.assertEquals(4, boardRs.size());
            for (BoardRepresentation boardR : boardRs) {
                String boardName = boardR.getName();
                switch (boardName) {
                    case "Career Opportunities":
                        Assert.assertEquals("new-department-updated/career-opportunities", boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                        break;
                    case "Research Opportunities":
                        Assert.assertEquals("new-department-updated/research-opportunities", boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                        break;
                    default:
                        Assert.assertEquals("new-department-updated/board-" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                        index++;
                        break;
                }
            }

            return null;
        });
    }

    @Test
    public void shouldSupportBoardLifeCycleAndPermissions() {
        // Create department and board
        User departmentUser = testUserService.authenticate();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary"))).getId();
        verifyPostBoard(departmentId, TestHelper.smallSampleBoard(), "board");

        // Create a board in the draft state
        testWebSocketService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForNewActivities(departmentUserId);

        User boardUser = testUserService.authenticate();
        Long boardUserId = boardUser.getId();
        listenForNewActivities(boardUserId);

        BoardRepresentation boardR = verifyPostBoard(departmentId, TestHelper.smallSampleBoard().setName("board 1"), "board-1");
        Long boardId = boardR.getId();
        testWebSocketService.stop();
        testNotificationService.stop();

        String departmentUserGivenName = departmentUser.getGivenName();
        String departmentName = "department";
        String boardUserGivenName = boardUser.getGivenName();
        String resourceRedirect = serverUrl + "/redirect?resource=" + boardId;
        String homeRedirect = serverUrl + "/redirect";

        testWebSocketService.verify(departmentUserId, new TestWebSocketService.ActivityInstance(boardId, Activity.NEW_BOARD_PARENT_ACTIVITY));

        Resource board = resourceService.findOne(boardId);
        Long activityId = activityService.findByResourceAndActivity(board, Activity.NEW_BOARD_PARENT_ACTIVITY).getId();
        testUserService.setAuthentication(departmentUserId);
        transactionTemplate.execute(status -> {
            userApi.dismissActivity(activityId);
            return null;
        });

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.NEW_BOARD_PARENT_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", departmentUserGivenName).put("department", departmentName).put("resourceRedirect", resourceRedirect)
                    .put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_BOARD_NOTIFICATION, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("resourceRedirect", resourceRedirect)
                    .put("modal", "Login").build()));

        // Create unprivileged users
        List<User> unprivilegedUsers = Lists.newArrayList(makeUnprivilegedUsers(departmentId, 2, 2).values());

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.EDIT, () -> boardApi.patchBoard(boardId, new BoardPatchDTO()))
            .put(Action.ACCEPT, () -> boardApi.executeAction(boardId, "accept", new BoardPatchDTO()))
            .put(Action.REJECT, () -> boardApi.executeAction(boardId, "reject", new BoardPatchDTO().setComment("comment")))
            .put(Action.RESTORE, () -> boardApi.executeAction(boardId, "restore", new BoardPatchDTO()))
            .build();

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.DRAFT, operations);

        // Test that we do not audit viewing
        transactionTemplate.execute(status -> boardApi.getBoard(boardId));

        // Check that department user can reject the board
        testWebSocketService.record();
        testNotificationService.record();

        listenForNewActivities(departmentUserId);
        listenForNewActivities(boardUserId);
        testUserService.setAuthentication(departmentUserId);

        verifyExecuteBoard(boardId, departmentUserId, "reject", "we cannot accept this", State.REJECTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.REJECTED, operations);

        testWebSocketService.verify(boardUserId, new TestWebSocketService.ActivityInstance(boardId, Activity.REJECT_BOARD_ACTIVITY));
        testWebSocketService.verify(departmentUserId);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_BOARD_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("comment", "we cannot accept this")
                .put("homeRedirect", homeRedirect).put("modal", "Login").build()));

        // Check that the department user can restore the board to draft
        verifyExecuteBoard(boardId, departmentUserId, "restore", "we made a mistake", State.DRAFT);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.DRAFT, operations);

        testWebSocketService.verify(boardUserId, new TestWebSocketService.ActivityInstance(boardId, Activity.RESTORE_BOARD_ACTIVITY));
        testWebSocketService.verify(departmentUserId);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESTORE_BOARD_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("resourceRedirect", resourceRedirect)
                .put("modal", "Login").build()));

        // Check that the department user can accept the board
        verifyExecuteBoard(boardId, departmentUserId, "accept", null, State.ACCEPTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        testWebSocketService.verify(boardUserId, new TestWebSocketService.ActivityInstance(boardId, Activity.ACCEPT_BOARD_ACTIVITY));
        testWebSocketService.verify(departmentUserId);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.ACCEPT_BOARD_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("resourceRedirect", resourceRedirect)
                .put("modal", "Login").build()));

        // Check that the department user can reject the board
        verifyExecuteBoard(boardId, departmentUserId, "reject", "we really cannot accept this", State.REJECTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.REJECTED, operations);

        testWebSocketService.verify(boardUserId, new TestWebSocketService.ActivityInstance(boardId, Activity.REJECT_BOARD_ACTIVITY));
        testWebSocketService.verify(departmentUserId);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_BOARD_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("comment", "we really cannot accept this")
                .put("homeRedirect", homeRedirect).put("modal", "Login").build()));

        // Check that the department user can restore the board to accepted
        verifyExecuteBoard(boardId, departmentUserId, "restore", "we made another mistake", State.ACCEPTED);
        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);
        testWebSocketService.stop();
        testNotificationService.stop();

        testWebSocketService.verify(boardUserId, new TestWebSocketService.ActivityInstance(boardId, Activity.RESTORE_BOARD_ACTIVITY));
        testWebSocketService.verify(departmentUserId);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESTORE_BOARD_NOTIFICATION, boardUser,
            ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", "board 1").put("resourceRedirect", resourceRedirect)
                .put("modal", "Login").build()));

        // Create post
        User postUser = testUserService.authenticate();
        transactionTemplate.execute(status -> postApi.postPost(boardId,
            TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));
        unprivilegedUsers.add(postUser);

        // Check that we can make changes and leave nullable values null
        verifyPatchBoard(departmentUser, boardId,
            new BoardPatchDTO()
                .setName(Optional.of("board 2"))
                .setHandle(Optional.of("board-2"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("logo 1").setCloudinaryUrl("logo 1").setFileName("logo 1"))),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        // Check that we can make further changes and set default / nullable values
        verifyPatchBoard(boardUser, boardId,
            new BoardPatchDTO()
                .setName(Optional.of("board 3"))
                .setSummary(Optional.of("summary"))
                .setHandle(Optional.of("board-3"))
                .setDocumentLogo(Optional.of(new DocumentDTO().setCloudinaryId("logo 2").setCloudinaryUrl("logo 2").setFileName("logo 2")))
                .setPostCategories(Optional.of(Arrays.asList("m1", "m2"))),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        // Check that we can make further changes and change default / nullable values
        verifyPatchBoard(departmentUser, boardId,
            new BoardPatchDTO()
                .setName(Optional.of("board 4"))
                .setSummary(Optional.of("summary 2"))
                .setHandle(Optional.of("board-4"))
                .setPostCategories(Optional.of(Arrays.asList("m2", "m1"))),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);

        // Check that we can clear nullable values
        verifyPatchBoard(boardUser, boardId,
            new BoardPatchDTO()
                .setSummary(Optional.empty())
                .setPostCategories(Optional.empty()),
            State.ACCEPTED);

        verifyBoardActions(departmentUser, boardUser, unprivilegedUsers, boardId, State.ACCEPTED, operations);
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> boardApi.getBoardOperations(boardId));
        Assert.assertEquals(10, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, boardUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.REJECT, departmentUser, "we cannot accept this");
        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.RESTORE, departmentUser, "we made a mistake");
        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.ACCEPT, departmentUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.REJECT, departmentUser, "we really cannot accept this");
        TestHelper.verifyResourceOperation(resourceOperationRs.get(5), Action.RESTORE, departmentUser, "we made another mistake");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(6), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("name", "board 1", "board 2")
                .put("handle", "board-1", "board-2")
                .put("documentLogo", null, ObjectUtils.orderedMap("cloudinaryId", "logo 1", "cloudinaryUrl", "logo 1", "fileName", "logo 1")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(7), Action.EDIT, boardUser,
            new ChangeListRepresentation()
                .put("name", "board 2", "board 3")
                .put("handle", "board-2", "board-3")
                .put("documentLogo",
                    ObjectUtils.orderedMap("cloudinaryId", "logo 1", "cloudinaryUrl", "logo 1", "fileName", "logo 1"),
                    ObjectUtils.orderedMap("cloudinaryId", "logo 2", "cloudinaryUrl", "logo 2", "fileName", "logo 2"))
                .put("summary", null, "summary")
                .put("postCategories", new ArrayList<>(), Arrays.asList("m1", "m2")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(8), Action.EDIT, departmentUser,
            new ChangeListRepresentation()
                .put("name", "board 3", "board 4")
                .put("handle", "board-3", "board-4")
                .put("summary", "summary", "summary 2")
                .put("postCategories", Arrays.asList("m1", "m2"), Arrays.asList("m2", "m1")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(9), Action.EDIT, boardUser,
            new ChangeListRepresentation()
                .put("summary", "summary 2", null)
                .put("postCategories", Arrays.asList("m2", "m1"), null));
    }

    @Test
    public void shouldPostAndReplaceLogo() {
        // Create department and board
        User user = testUserService.authenticate();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        DocumentDTO initialLogo = new DocumentDTO().setCloudinaryId("postingLogo").setCloudinaryUrl("postingLogo").setFileName("postingLogo");
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary").setDocumentLogo(initialLogo))).getId();

        BoardDTO boardDTO = TestHelper.smallSampleBoard();
        boardDTO.setDocumentLogo(initialLogo);
        BoardRepresentation boardR = verifyPostBoard(departmentId, boardDTO, "board");
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

    @Test
    public void shouldCountPostsAndAuthors() {
        Long boardUserId = testUserService.authenticate().getId();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary"))).getId();

        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(departmentId, TestHelper.smallSampleBoard())).getId();
        transactionTemplate.execute(status -> boardApi.postBoard(departmentId, TestHelper.smallSampleBoard().setName("other")));
        transactionTemplate.execute(status -> departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.empty())));

        testUserService.authenticate();
        Long post1id = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost())).getId();

        Long postUserId = testUserService.authenticate().getId();
        Long post2id = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost())).getId();

        testUserService.authenticate();
        Long post3id = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost())).getId();
        verifyPostAndAuthorCount(boardId, 0L, 0L);

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> postApi.executeAction(post1id, "accept", new PostPatchDTO()));
        transactionTemplate.execute(status -> postApi.executeAction(post2id, "accept", new PostPatchDTO()));
        transactionTemplate.execute(status -> postApi.executeAction(post3id, "reject", new PostPatchDTO().setComment("comment")));

        postService.publishAndRetirePosts();
        verifyPostAndAuthorCount(boardId, 2L, 2L);

        transactionTemplate.execute(status -> postApi.executeAction(post2id, "reject", new PostPatchDTO().setComment("comment")));
        verifyPostAndAuthorCount(boardId, 1L, 2L);

        transactionTemplate.execute(status -> {
            resourceApi.deleteResourceUser(Scope.BOARD, boardId, postUserId);
            return null;
        });

        verifyPostAndAuthorCount(boardId, 1L, 1L);

        transactionTemplate.execute(status -> postApi.patchPost(post1id, new PostPatchDTO().setDeadTimestamp(Optional.of(LocalDateTime.now().minusSeconds(1)))));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        verifyPostAndAuthorCount(boardId, 0L, 1L);

        Long post4id = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost().setLiveTimestamp(LocalDateTime.now().plusMinutes(1)))).getId();
        verifyPostAndAuthorCount(boardId, 0L, 1L);

        transactionTemplate.execute(status -> postApi.patchPost(post4id, new PostPatchDTO().setLiveTimestamp(Optional.empty())));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        verifyPostAndAuthorCount(boardId, 1L, 1L);
    }

    @Test
    public void shouldArchiveBoards() throws InterruptedException {
        testUserService.authenticate();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary"))).getId();

        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(departmentId, new BoardDTO().setName("board1")));
        Long boardId1 = boardR.getId();

        testUserService.authenticate();
        Long boardId2 = transactionTemplate.execute(status -> boardApi.postBoard(departmentId, new BoardDTO().setName("board2"))).getId();

        TimeUnit.SECONDS.sleep(resourceArchiveDurationSeconds + 1);

        transactionTemplate.execute(status -> {
            resourceService.archiveResources();
            return null;
        });

        Assert.assertEquals(State.ACCEPTED, transactionTemplate.execute(status -> resourceRepository.findOne(boardId1)).getState());
        Assert.assertEquals(State.ARCHIVED, transactionTemplate.execute(status -> resourceRepository.findOne(boardId2)).getState());
    }

    private Pair<BoardRepresentation, BoardRepresentation> verifyPostTwoBoards() {
        testUserService.authenticate();
        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long departmentId = transactionTemplate.execute(status ->
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary"))).getId();
        BoardRepresentation boardR1 = verifyPostBoard(departmentId, TestHelper.smallSampleBoard(), "board");
        BoardRepresentation boardR2 = verifyPostBoard(departmentId, new BoardDTO().setName("board 2"), "board-2");
        return new Pair<>(boardR1, boardR2);
    }

    private BoardRepresentation verifyPostBoard(Long departmentId, BoardDTO boardDTO, String expectedHandle) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.postBoard(departmentId, boardDTO);

            Assert.assertEquals(boardDTO.getName(), boardR.getName());
            Assert.assertEquals(expectedHandle, boardR.getHandle());
            Assert.assertEquals(boardDTO.getSummary(), boardR.getSummary());
            verifyDocument(boardDTO.getDocumentLogo(), boardR.getDocumentLogo());
            Assert.assertEquals(Optional.ofNullable(boardDTO.getPostCategories()).orElse(new ArrayList<>()), boardR.getPostCategories());

            Board board = boardService.getBoard(boardR.getId());
            DepartmentRepresentation departmentR = boardR.getDepartment();
            Department department = departmentService.getDepartment(departmentR.getId());
            University university = universityService.getUniversity(departmentR.getUniversity().getId());
            Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());

            Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(board, department, university));
            return boardR;
        });
    }

    private void verifyExecuteBoard(Long boardId, Long departmentUserId, String action, String comment, State expectedState) {
        BoardRepresentation boardR;
        testUserService.setAuthentication(departmentUserId);
        boardR = transactionTemplate.execute(status ->
            boardApi.executeAction(boardId, action, new BoardPatchDTO().setComment(comment)));
        Assert.assertEquals(expectedState, boardR.getState());
    }

    private BoardRepresentation verifyPatchBoard(User user, Long boardId, BoardPatchDTO boardDTO, State expectedState) {
        testUserService.setAuthentication(user.getId());
        Board board = transactionTemplate.execute(status -> boardService.getBoard(boardId));
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.patchBoard(boardId, boardDTO));

        return transactionTemplate.execute(status -> {
            Optional<String> nameOptional = boardDTO.getName();
            Assert.assertEquals(nameOptional == null ? board.getName() : nameOptional.orElse(null), boardR.getName());

            Optional<DocumentDTO> documentLogoOptional = boardDTO.getDocumentLogo();
            verifyDocument(documentLogoOptional == null ? board.getDocumentLogo() : boardDTO.getDocumentLogo().orElse(null), boardR.getDocumentLogo());

            Optional<String> summaryOptional = boardDTO.getSummary();
            Assert.assertEquals(summaryOptional == null ? board.getSummary() : summaryOptional.orElse(null), boardR.getSummary());

            Optional<String> handleOptional = boardDTO.getHandle();
            Assert.assertEquals(handleOptional == null ? board.getHandle().split("/")[2] : handleOptional.orElse(null), boardR.getHandle());

            Optional<List<String>> postCategoriesOptional = boardDTO.getPostCategories();
            Assert.assertEquals(postCategoriesOptional == null ? resourceService.getCategories(board, CategoryType.POST) : postCategoriesOptional.orElse(new ArrayList<>()),
                boardR.getPostCategories());

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
            transactionTemplate.execute(status ->
                boardApi.getBoards(null, null, null, null, null)
                    .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                    .collect(Collectors.toList())),
            Collections.emptyList(),
            null);

        TestHelper.ExpectedActions expectedActions = new TestHelper.ExpectedActions()
            .add(publicActions);
        TestHelper.verifyResources(
            transactionTemplate.execute(status ->
                boardApi.getBoards(null, true, null, null, null)
                    .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                    .collect(Collectors.toList())),
            boardNames,
            expectedActions);

        for (Long departmentId : boardNamesByDepartment.keySet()) {
            TestHelper.verifyResources(
                transactionTemplate.execute(status ->
                    boardApi.getBoards(departmentId, null, null, null, null)
                        .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                        .collect(Collectors.toList())),
                Collections.emptyList(),
                null);

            TestHelper.verifyResources(
                transactionTemplate.execute(status ->
                    boardApi.getBoards(departmentId, true, null, null, null)
                        .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                        .collect(Collectors.toList())),
                Lists.newArrayList(boardNamesByDepartment.get(departmentId)),
                expectedActions);
        }
    }

    private void verifyPrivilegedBoardUser(List<String> boardNames, List<String> adminBoardNames, LinkedHashMultimap<Long, String> boardNamesByDepartment, Set<Action> adminActions) {
        List<Action> adminActionList = Lists.newArrayList(adminActions);
        List<Action> publicActionList = Lists.newArrayList(PUBLIC_ACTIONS.get(State.ACCEPTED));

        TestHelper.verifyResources(
            transactionTemplate.execute(status ->
                boardApi.getBoards(null,null, null, null, null)
                    .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                    .collect(Collectors.toList())),
            adminBoardNames,
            new TestHelper.ExpectedActions()
                .addAll(adminBoardNames, adminActionList));

        TestHelper.verifyResources(
            transactionTemplate.execute(status ->
                boardApi.getBoards(null, true, null, null, null)
                    .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                    .collect(Collectors.toList())),
            boardNames,
            new TestHelper.ExpectedActions()
                .add(publicActionList)
                .addAll(adminBoardNames, adminActionList));

        for (Long departmentId : boardNamesByDepartment.keySet()) {
            List<String> departmentBoardNames = Lists.newArrayList(boardNamesByDepartment.get(departmentId));
            @SuppressWarnings("unchecked") List<String> adminDepartmentBoardNames = ListUtils.intersection(departmentBoardNames, adminBoardNames);
            TestHelper.verifyResources(
                transactionTemplate.execute(status ->
                    boardApi.getBoards(departmentId, null, null, null, null)
                        .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                        .collect(Collectors.toList())),
                adminDepartmentBoardNames,
                new TestHelper.ExpectedActions()
                    .addAll(adminDepartmentBoardNames, adminActionList));

            TestHelper.verifyResources(
                transactionTemplate.execute(status ->
                    boardApi.getBoards(departmentId, true, null, null, null)
                        .stream().filter(board -> board.getType().equals(BoardType.CUSTOM))
                        .collect(Collectors.toList())),
                departmentBoardNames,
                new TestHelper.ExpectedActions()
                    .add(publicActionList)
                    .addAll(adminDepartmentBoardNames, adminActionList));
        }
    }

    private void verifyPostAndAuthorCount(Long boardId, Long postCount, Long authorCount) {
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.getBoard(boardId));
        TestHelper.verifyNullableCount(postCount, boardR.getPostCount());
        TestHelper.verifyNullableCount(authorCount, boardR.getAuthorCount());

        List<BoardRepresentation> boardRs = transactionTemplate.execute(status -> boardApi.getBoards(null, true, null, null, null));
        TestHelper.verifyNullableCount(postCount, boardRs.get(0).getPostCount());
        TestHelper.verifyNullableCount(authorCount, boardRs.get(0).getAuthorCount());

        TestHelper.verifyNullableCount(0L, boardRs.get(1).getPostCount());
        TestHelper.verifyNullableCount(0L, boardRs.get(1).getAuthorCount());
    }

}
