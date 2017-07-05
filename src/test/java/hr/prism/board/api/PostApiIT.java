package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.definition.LocationDefinition;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.service.PostService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.util.ObjectUtils;
import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@TestContext
@RunWith(SpringRunner.class)
public class PostApiIT extends AbstractIT {

    private static LinkedHashMultimap<State, Action> ADMIN_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> AUTHOR_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.SUSPENDED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.ACCEPT, Action.REJECT));
        ADMIN_ACTIONS.putAll(State.PENDING, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.EXPIRED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE));
        ADMIN_ACTIONS.putAll(State.WITHDRAWN, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT));

        AUTHOR_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.SUSPENDED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.WITHDRAW, Action.CORRECT));
        AUTHOR_ACTIONS.putAll(State.PENDING, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.EXPIRED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.WITHDRAWN, Arrays.asList(Action.VIEW, Action.AUDIT, Action.EDIT, Action.RESTORE));

        PUBLIC_ACTIONS.put(State.ACCEPTED, Action.VIEW);
    }

    @Inject
    private PostService postService;

    @Test
    public void shouldCreateAndListPosts() {
        Map<Long, Map<Scope, User>> unprivilegedUsers = new HashMap<>();
        Map<Long, String> unprivilegedUserPosts = new LinkedHashMap<>();

        User user11 = testUserService.authenticate();
        BoardDTO boardDTO11 = TestHelper.sampleBoard();
        boardDTO11.getDepartment().setName("department1");
        boardDTO11.setName("board11");
        BoardRepresentation boardR11 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO11));

        Long board11Id = boardR11.getId();
        String board11PostName = boardR11.getName() + " " + State.DRAFT.name().toLowerCase() + " " + 0;
        unprivilegedUsers.put(board11Id, makeUnprivilegedUsers(boardR11.getDepartment().getId(), boardR11.getId(), 110, 1100,
            (PostDTO) TestHelper.samplePost()
                .setName(board11PostName)));
        unprivilegedUserPosts.put(board11Id, board11PostName);


        User user12 = testUserService.authenticate();
        BoardDTO boardDTO12 = TestHelper.smallSampleBoard();
        boardDTO12.getDepartment().setName("department1");
        boardDTO12.setName("board12");
        BoardRepresentation boardR12 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO12));
        testUserService.setAuthentication(user11.getId());
        boardApi.executeAction(boardR12.getId(), "accept", new BoardPatchDTO());
        testUserService.setAuthentication(user12.getId());

        Long board12Id = boardR12.getId();
        String board12PostName = boardR12.getName() + " " + State.DRAFT.name().toLowerCase() + " " + 0;
        unprivilegedUsers.put(board12Id, makeUnprivilegedUsers(boardR12.getDepartment().getId(), boardR12.getId(), 120, 1200,
            ((PostDTO) TestHelper.smallSamplePost()
                .setName(board12PostName))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));
        unprivilegedUserPosts.put(board12Id, board12PostName);

        User user21 = testUserService.authenticate();
        BoardDTO boardDTO21 = TestHelper.smallSampleBoard();
        boardDTO21.getDepartment().setName("department2");
        boardDTO21.setName("board21");
        BoardRepresentation boardR21 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO21));

        Long board21Id = boardR21.getId();
        String board21PostName = boardR21.getName() + " " + State.DRAFT.name().toLowerCase() + " " + 0;
        unprivilegedUsers.put(board21Id, makeUnprivilegedUsers(boardR21.getDepartment().getId(), boardR21.getId(), 210, 2100,
            (PostDTO) TestHelper.smallSamplePost()
                .setName(board21PostName)));
        unprivilegedUserPosts.put(board21Id, board21PostName);

        User user22 = testUserService.authenticate();
        BoardDTO boardDTO22 = TestHelper.sampleBoard();
        boardDTO22.getDepartment().setName("department2");
        boardDTO22.setName("board22");
        BoardRepresentation boardR22 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO22));
        testUserService.setAuthentication(user21.getId());
        boardApi.executeAction(boardR22.getId(), "accept", new BoardPatchDTO());
        testUserService.setAuthentication(user22.getId());

        Long board22Id = boardR22.getId();
        String board22PostName = boardR22.getName() + " " + State.DRAFT.name().toLowerCase() + " " + 0;
        unprivilegedUsers.put(board22Id, makeUnprivilegedUsers(boardR22.getDepartment().getId(), boardR22.getId(), 220, 2200,
            ((PostDTO) TestHelper.smallSamplePost()
                .setName(board22PostName))
                .setPostCategories(Collections.singletonList("p1"))));
        unprivilegedUserPosts.put(board22Id, board22PostName);

        LinkedHashMultimap<State, String> boardPostNames11 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames12 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames21 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames22 = LinkedHashMultimap.create();

        int postCount = 1;
        LocalDateTime baseline = LocalDateTime.now();
        LinkedHashMap<User, LinkedHashMap<Long, LinkedHashMultimap<State, String>>> posts = new LinkedHashMap<>();
        for (State state : Arrays.stream(State.values()).filter(state -> state != State.PREVIOUS).collect(Collectors.toList())) {
            if (state == State.DRAFT) {
                reschedulePost(board11PostName, baseline, postCount);
                boardPostNames11.put(state, board11PostName);
                postCount++;
            }

            User postUser1 = testUserService.authenticate();
            for (int i = 1; i < 3; i++) {
                String name = boardR11.getName() + " " + state.name().toLowerCase() + " " + i;
                verifyPostPostAndSetState(postUser1, board11Id,
                    (PostDTO) TestHelper.samplePost()
                        .setName(name),
                    state, posts, baseline, postCount);
                boardPostNames11.put(state, name);
                postCount++;
            }

            if (state == State.DRAFT) {
                reschedulePost(board12PostName, baseline, postCount);
                boardPostNames12.put(state, board12PostName);
                postCount++;
            }

            for (int i = 1; i < 3; i++) {
                String name = boardR12.getName() + " " + state.name().toLowerCase() + " " + i;
                verifyPostPostAndSetState(postUser1, board12Id,
                    ((PostDTO) TestHelper.smallSamplePost()
                        .setName(name))
                        .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)),
                    state, posts, baseline, postCount);
                boardPostNames12.put(state, name);
                postCount++;
            }

            if (state == State.DRAFT) {
                reschedulePost(board21PostName, baseline, postCount);
                boardPostNames21.put(state, board21PostName);
                postCount++;
            }

            User postUser2 = testUserService.authenticate();
            for (int i = 1; i < 3; i++) {
                String name = boardR21.getName() + " " + state.name().toLowerCase() + " " + i;
                verifyPostPostAndSetState(postUser2, board21Id,
                    (PostDTO) TestHelper.smallSamplePost()
                        .setName(name),
                    state, posts, baseline, postCount);
                boardPostNames21.put(state, name);
                postCount++;
            }

            if (state == State.DRAFT) {
                reschedulePost(board22PostName, baseline, postCount);
                boardPostNames22.put(state, board22PostName);
                postCount++;
            }

            for (int i = 1; i < 3; i++) {
                String name = boardR22.getName() + " " + state.name().toLowerCase() + " " + i;
                verifyPostPostAndSetState(postUser2, board22Id,
                    ((PostDTO) TestHelper.smallSamplePost()
                        .setName(name))
                        .setPostCategories(Collections.singletonList("p1")),
                    state, posts, baseline, postCount);
                boardPostNames22.put(state, name);
                postCount++;
            }
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> boardPostNames = new LinkedHashMap<>();
        boardPostNames.put(board11Id, boardPostNames11);
        boardPostNames.put(board12Id, boardPostNames12);
        boardPostNames.put(board21Id, boardPostNames21);
        boardPostNames.put(board22Id, boardPostNames22);

        LinkedHashMap<Long, LinkedHashMultimap<State, String>> publicPostNames = new LinkedHashMap<>();
        LinkedHashMultimap<State, String> publicPostNames11 = LinkedHashMultimap.create();
        publicPostNames11.putAll(State.ACCEPTED, Arrays.asList("board11 accepted 1", "board11 accepted 2"));
        publicPostNames.put(board11Id, publicPostNames11);

        LinkedHashMultimap<State, String> publicPostNames12 = LinkedHashMultimap.create();
        publicPostNames12.putAll(State.ACCEPTED, Arrays.asList("board12 accepted 1", "board12 accepted 2"));
        publicPostNames.put(board12Id, publicPostNames12);

        LinkedHashMultimap<State, String> publicPostNames21 = LinkedHashMultimap.create();
        publicPostNames21.putAll(State.ACCEPTED, Arrays.asList("board21 accepted 1", "board21 accepted 2"));
        publicPostNames.put(board21Id, publicPostNames21);

        LinkedHashMultimap<State, String> publicPostNames22 = LinkedHashMultimap.create();
        publicPostNames22.putAll(State.ACCEPTED, Arrays.asList("board22 accepted 1", "board22 accepted 2"));
        publicPostNames.put(board22Id, publicPostNames22);

        testUserService.unauthenticate();
        verifyUnprivilegedPostUser(publicPostNames);

        for (Long boardId : unprivilegedUsers.keySet()) {
            Map<Scope, User> unprivilegedUserMap = unprivilegedUsers.get(boardId);
            for (Scope scope : unprivilegedUserMap.keySet()) {
                testUserService.setAuthentication(unprivilegedUserMap.get(scope).getId());
                if (scope == Scope.DEPARTMENT || scope == Scope.BOARD) {
                    verifyPrivilegedPostUser(publicPostNames, new LinkedHashMap<>(), PostAdminContext.ADMIN);
                } else if (scope == Scope.POST) {
                    LinkedHashMap<Long, LinkedHashMultimap<State, String>> authorPostNames = new LinkedHashMap<>();
                    LinkedHashMultimap<State, String> authorStatePostNames = LinkedHashMultimap.create();
                    authorStatePostNames.put(State.DRAFT, unprivilegedUserPosts.get(boardId));
                    authorPostNames.put(boardId, authorStatePostNames);
                    verifyPrivilegedPostUser(publicPostNames, authorPostNames, PostAdminContext.AUTHOR);
                }
            }
        }

        testUserService.setAuthentication(user11.getId());
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> user11BoardPostNames = new LinkedHashMap<>();
        user11BoardPostNames.put(board11Id, boardPostNames11);
        user11BoardPostNames.put(board12Id, boardPostNames12);
        verifyPrivilegedPostUser(publicPostNames, user11BoardPostNames, PostAdminContext.ADMIN);

        testUserService.setAuthentication(user12.getId());
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> user12BoardPostNames = new LinkedHashMap<>();
        user12BoardPostNames.put(board12Id, boardPostNames12);
        verifyPrivilegedPostUser(publicPostNames, user12BoardPostNames, PostAdminContext.ADMIN);

        testUserService.setAuthentication(user21.getId());
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> user21BoardPostNames = new LinkedHashMap<>();
        user11BoardPostNames.put(board21Id, boardPostNames21);
        user11BoardPostNames.put(board22Id, boardPostNames22);
        verifyPrivilegedPostUser(publicPostNames, user21BoardPostNames, PostAdminContext.ADMIN);

        testUserService.setAuthentication(user22.getId());
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> user22BoardPostNames = new LinkedHashMap<>();
        user12BoardPostNames.put(board22Id, boardPostNames22);
        verifyPrivilegedPostUser(publicPostNames, user22BoardPostNames, PostAdminContext.ADMIN);

        for (User postUser : posts.keySet()) {
            testUserService.setAuthentication(postUser.getId());
            verifyPrivilegedPostUser(publicPostNames, posts.get(postUser), PostAdminContext.AUTHOR);
        }
    }

    @Test
    public void shouldNotAcceptPostWithMissingApply() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        transactionTemplate.execute(status -> {
            PostDTO postDTO = ((PostDTO) new PostDTO()
                .setName("post")
                .setSummary("summary"))
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_APPLY, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithCorruptedApply() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        transactionTemplate.execute(status -> {
            PostDTO postDTO = ((PostDTO) new PostDTO()
                .setName("post")
                .setSummary("summary"))
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setApplyWebsite("http://www.google.com")
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_APPLY, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithMissingRelationForUserWithoutAuthorRole() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        testUserService.authenticate();
        transactionTemplate.execute(status -> {
            PostDTO postDTO = ((PostDTO) new PostDTO()
                .setName("post")
                .setSummary("summary"))
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithCategoriesForBoardWithoutCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithoutCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithInvalidCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.samplePost().setPostCategories(Collections.singletonList("p4"));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.INVALID_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.samplePost().setMemberCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT));
            ExceptionUtils.verifyApiException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.INVALID_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotCorruptPostByPatching() {
        testUserService.authenticate();
        BoardRepresentation boardRepresentation = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()));
        Long departmentId = boardRepresentation.getDepartment().getId();
        Long boardId = boardRepresentation.getId();
        Long postId = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.samplePost()).getId());

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(BoardException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.empty())),
                ExceptionCode.MISSING_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(BoardException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.empty())),
                ExceptionCode.MISSING_POST_MEMBER_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(BoardException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.of(Collections.singletonList("p4")))),
                ExceptionCode.INVALID_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(BoardException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.of(Collections.singletonList(MemberCategory.RESEARCH_STUDENT)))),
                ExceptionCode.INVALID_POST_MEMBER_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.empty())));
        transactionTemplate.execute(status -> boardApi.updateBoard(boardId, new BoardPatchDTO().setPostCategories(Optional.empty())));

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(BoardException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.of(Collections.singletonList("p1")))),
                ExceptionCode.CORRUPTED_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(BoardException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.of(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)))),
                ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldSupportPostLifecycleAndPermissions() {
        // Create department and board
        User departmentUser = testUserService.authenticate();
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()));
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();

        // Allow department to have research students
        departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(
            Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT, MemberCategory.RESEARCH_STUDENT))));

        User boardUser = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            Board board = boardService.getBoard(boardId);
            userRoleService.createUserRole(board, boardUser, Role.ADMINISTRATOR);
            return null;
        });

        List<User> adminUsers = Arrays.asList(departmentUser, boardUser);

        // Create post
        testNotificationService.record();
        User postUser = testUserService.authenticate();
        PostRepresentation postR = verifyPostPost(boardId, TestHelper.samplePost());
        Long postId = postR.getId();

        String departmentName = boardR.getDepartment().getName();
        String boardName = boardR.getName();
        String postName = postR.getName();
        String departmentUserGivenName = departmentUser.getGivenName();
        String boardUserGivenName = boardUser.getGivenName();
        String postUserGivenName = postUser.getGivenName();
        String resourceRedirect = environment.getProperty("server.url") + "/redirect?resource=" + postId;

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_PARENT, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", departmentUserGivenName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_PARENT, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_POST, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Create unprivileged users
        testNotificationService.clear();
        Collection<User> unprivilegedUsers = makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.samplePost()).values();
        testNotificationService.record();

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.VIEW, () -> postApi.getPost(postId))
            .put(Action.AUDIT, () -> postApi.getPostOperations(postId))
            .put(Action.EDIT, () -> postApi.updatePost(postId, new PostPatchDTO()))
            .put(Action.ACCEPT, () -> postApi.executeAction(postId, "accept", new PostPatchDTO()))
            .put(Action.SUSPEND, () -> postApi.executeAction(postId, "suspend", (PostPatchDTO) new PostPatchDTO().setComment("comment")))
            .put(Action.CORRECT, () -> postApi.executeAction(postId, "correct", new PostPatchDTO()))
            .put(Action.REJECT, () -> postApi.executeAction(postId, "reject", (PostPatchDTO) new PostPatchDTO().setComment("comment")))
            .put(Action.RESTORE, () -> postApi.executeAction(postId, "restore", (PostPatchDTO) new PostPatchDTO()))
            .put(Action.WITHDRAW, () -> postApi.executeAction(postId, "withdraw", new PostPatchDTO()))
            .build();

        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.DRAFT, operations);

        // Check that we do not audit viewing
        transactionTemplate.execute(status -> postApi.getPost(postId));

        LocalDateTime liveTimestamp = postR.getLiveTimestamp();
        LocalDateTime deadTimestamp = postR.getDeadTimestamp();

        LocalDateTime liveTimestampDelayed = LocalDateTime.now().plusWeeks(4L).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime deadTimestampDelayed = LocalDateTime.now().plusWeeks(8L).truncatedTo(ChronoUnit.SECONDS);

        // Check that the author can update the post
        PostPatchDTO updateDTO = ((PostPatchDTO) new PostPatchDTO()
            .setName(Optional.of("post 2"))
            .setSummary(Optional.of("summary 2")))
            .setDescription(Optional.of("description"))
            .setOrganizationName(Optional.of("organization name 2"))
            .setLocation(Optional.of(
                new LocationDTO()
                    .setName("london")
                    .setDomicile("GB")
                    .setGoogleId("ttt")
                    .setLatitude(BigDecimal.TEN)
                    .setLongitude(BigDecimal.TEN)))
            .setApplyWebsite(Optional.of("http://www.facebook.com"))
            .setPostCategories(Optional.of(Arrays.asList("p2", "p1")))
            .setMemberCategories(Optional.of(Arrays.asList(MemberCategory.MASTER_STUDENT, MemberCategory.UNDERGRADUATE_STUDENT)))
            .setExistingRelation(Optional.of(ExistingRelation.STAFF))
            .setExistingRelationExplanation(Optional.of(ObjectUtils.orderedMap("jobTitle", "professor")))
            .setLiveTimestamp(Optional.of(liveTimestampDelayed))
            .setDeadTimestamp(Optional.of(deadTimestampDelayed));

        postR = verifyPatchPost(postUser, postId, updateDTO, () -> postApi.updatePost(postId, updateDTO), State.DRAFT);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.DRAFT, operations);
        postName = postR.getName();

        // Check that the administrator can make changes and suspend the post
        PostPatchDTO suspendDTO = (PostPatchDTO) new PostPatchDTO()
            .setLiveTimestamp(Optional.of(liveTimestamp))
            .setDeadTimestamp(Optional.of(deadTimestamp))
            .setComment("could you please explain what you will pay the successful applicant");

        verifyPatchPost(departmentUser, postId, suspendDTO, () -> postApi.executeAction(postId, "suspend", suspendDTO), State.SUSPENDED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.SUSPENDED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_POST, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("comment", "could you please explain what you will pay the successful applicant").put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Check that the author can make changes and correct the post
        PostPatchDTO correctDTO = (PostPatchDTO) new PostPatchDTO()
            .setOrganizationName(Optional.of("organization name"))
            .setDescription(Optional.of("description 2"))
            .setLocation(Optional.of(
                new LocationDTO()
                    .setName("birmingham")
                    .setDomicile("GB")
                    .setGoogleId("uuu")
                    .setLatitude(BigDecimal.ZERO)
                    .setLongitude(BigDecimal.ZERO)))
            .setApplyDocument(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
            .setMemberCategories(Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT)))
            .setComment("i uploaded a document this time which explains that");

        verifyPatchPost(postUser, postId, correctDTO, () -> postApi.executeAction(postId, "correct", correctDTO), State.DRAFT);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.DRAFT, operations);

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CORRECT_POST, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", departmentUserGivenName).put("post", postName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.CORRECT_POST, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("post", postName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Check that the administrator can accept post in the suspended state
        PostPatchDTO acceptDTO = (PostPatchDTO) new PostPatchDTO()
            .setLiveTimestamp(Optional.empty())
            .setDeadTimestamp(Optional.empty())
            .setComment("accepting without time constraints");

        verifyPatchPost(boardUser, postId, acceptDTO, () -> postApi.executeAction(postId, "accept", acceptDTO), State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.JOIN_BOARD, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", environment.getProperty("server.url") + "/redirect?resource=" + boardId).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.ACCEPT_POST, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("publicationSchedule", "immediately").put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Suspend the post so that it can be accepted again
        verifyPatchPost(boardUser, postId, new PostPatchDTO(),
            () -> postApi.executeAction(postId, "suspend", (PostPatchDTO) new PostPatchDTO().setComment("comment")), State.SUSPENDED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.SUSPENDED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_POST, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("comment", "comment").put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Check that the administrator can make further changes and accept the post again
        PostPatchDTO acceptPendingDTO = (PostPatchDTO) new PostPatchDTO()
            .setApplyWebsite(Optional.of("http://www.twitter.com"))
            .setPostCategories(Optional.of(Arrays.asList("p1", "p2")))
            .setLiveTimestamp(Optional.of(liveTimestampDelayed))
            .setDeadTimestamp(Optional.of(deadTimestampDelayed))
            .setComment("this looks good now - i replaced the document with the complete website for the opportunity");

        verifyPatchPost(boardUser, postId, acceptPendingDTO, () -> postApi.executeAction(postId, "accept", acceptPendingDTO), State.PENDING);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.PENDING, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.ACCEPT_POST, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("publicationSchedule",
                    "on or around " + postR.getLiveTimestamp().format(BoardUtils.DATETIME_FORMATTER) + ". We will send you a follow-up message when your post has gone live")
                .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Check that the post stays in pending state when the update job runs
        verifyPublishAndRetirePost(postId, State.PENDING);

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setLiveTimestamp(liveTimestamp);
            post.setDeadTimestamp(deadTimestamp);
            return null;
        });

        // Should be notified
        testUserService.setAuthentication(departmentUser.getId());
        Long departmentMember1Id = resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(
                new UserDTO()
                    .setGivenName("student1")
                    .setSurname("student1")
                    .setEmail("student1@student1.com"))
                .setRoles(Collections.singleton(
                    new UserRoleDTO()
                        .setRole(Role.MEMBER)
                        .setExpiryDate(LocalDate.now().plusDays(1))
                        .setCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))))).getUser().getId();

        // Should be notified
        Long departmentMember2Id = resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(
                new UserDTO()
                    .setGivenName("student2")
                    .setSurname("student2")
                    .setEmail("student2@student2.com"))
                .setRoles(Collections.singleton(
                    new UserRoleDTO()
                        .setRole(Role.MEMBER)
                        .setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT))))).getUser().getId();

        // Should not be notified
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(
                new UserDTO()
                    .setGivenName("student3")
                    .setSurname("student3")
                    .setEmail("student3@student3.com"))
                .setRoles(Collections.singleton(
                    new UserRoleDTO()
                        .setRole(Role.MEMBER)
                        .setExpiryDate(LocalDate.now().plusDays(1))
                        .setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT)))));

        // Should not be notified
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(
                new UserDTO()
                    .setGivenName("student4")
                    .setSurname("student4")
                    .setEmail("student4@student4.com"))
                .setRoles(Collections.singleton(
                    new UserRoleDTO()
                        .setRole(Role.MEMBER)
                        .setExpiryDate(LocalDate.now().minusDays(1))
                        .setCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)))));

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        String parentRedirect = environment.getProperty("server.url") + "/redirect?resource=" + boardId;
        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER, userCacheService.findOne(departmentMember1Id),
                ImmutableMap.<String, String>builder().put("recipient", "student1").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "Register")
                    .put("parentRedirect", parentRedirect).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER, userCacheService.findOne(departmentMember2Id),
                ImmutableMap.<String, String>builder().put("recipient", "student2").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "Register")
                    .put("parentRedirect", parentRedirect).build()));

        // Check that the administrator can reject the post
        PostPatchDTO rejectDTO = (PostPatchDTO) new PostPatchDTO()
            .setComment("we have received a complaint, we're closing down the post");

        verifyPatchPost(departmentUser, postId, rejectDTO, () -> postApi.executeAction(postId, "reject", rejectDTO), State.REJECTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.REJECTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_POST, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("comment", "we have received a complaint, we're closing down the post").put("homeRedirect", environment.getProperty("server.url") + "/redirect")
                .put("modal", "Login").build()));

        // Check that the administrator can restore the post
        PostPatchDTO restoreFromRejectedDTO = (PostPatchDTO) new PostPatchDTO()
            .setComment("sorry we made a mistake, we're restoring the post");

        verifyPatchPost(boardUser, postId, restoreFromRejectedDTO, () -> postApi.executeAction(postId, "restore", restoreFromRejectedDTO), State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESTORE_POST, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setDeadTimestamp(liveTimestamp.minusSeconds(1));
            return null;
        });

        // Check that the post now moves to the expired state when the update job runs
        verifyPublishAndRetirePost(postId, State.EXPIRED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.EXPIRED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RETIRE_POST, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()));

        // Check that the author can withdraw the post
        PostPatchDTO withdrawDTO = new PostPatchDTO();
        verifyPatchPost(postUser, postId, withdrawDTO, () -> postApi.executeAction(postId, "withdraw", withdrawDTO), State.WITHDRAWN);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.WITHDRAWN, operations);

        // Check that the author can restore the post
        PostPatchDTO restoreFromWithdrawnDTO = new PostPatchDTO();

        verifyPatchPost(postUser, postId, restoreFromWithdrawnDTO, () -> postApi.executeAction(postId, "restore", restoreFromWithdrawnDTO), State.EXPIRED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.EXPIRED, operations);

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setDeadTimestamp(null);
            return null;
        });

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER, userCacheService.findOne(departmentMember1Id),
                ImmutableMap.<String, String>builder().put("recipient", "student1").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "Register")
                    .put("parentRedirect", parentRedirect).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER, userCacheService.findOne(departmentMember2Id),
                ImmutableMap.<String, String>builder().put("recipient", "student2").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "Register")
                    .put("parentRedirect", parentRedirect).build()));
        testNotificationService.clear();

        testUserService.setAuthentication(postUser.getId());
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> postApi.getPostOperations(postId));
        Assert.assertEquals(18, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, postUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, postUser,
            new ResourceChangeListRepresentation()
                .put("name", "post", "post 2")
                .put("summary", "summary", "summary 2")
                .put("description", null, "description")
                .put("organizationName", "organization name", "organization name 2")
                .put("location",
                    ObjectUtils.orderedMap("name", "krakow", "domicile", "PL", "googleId", "sss",
                        "latitude", BigDecimal.ONE.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.ONE.stripTrailingZeros().toPlainString()),
                    ObjectUtils.orderedMap("name", "london", "domicile", "GB", "googleId", "ttt",
                        "latitude", BigDecimal.TEN.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.TEN.stripTrailingZeros().toPlainString()))
                .put("applyWebsite", "http://www.google.co.uk", "http://www.facebook.com")
                .put("postCategories", Arrays.asList("p1", "p2"), Arrays.asList("p2", "p1"))
                .put("memberCategories", Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT"), Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT"))
                .put("existingRelation", "STUDENT", "STAFF")
                .put("existingRelationExplanation",
                    ObjectUtils.orderedMap("studyLevel", "MASTER"),
                    ObjectUtils.orderedMap("jobTitle", "professor"))
                .put("liveTimestamp", TestHelper.toString(liveTimestamp), TestHelper.toString(liveTimestampDelayed))
                .put("deadTimestamp", TestHelper.toString(deadTimestamp), TestHelper.toString(deadTimestampDelayed)));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("liveTimestamp", TestHelper.toString(liveTimestampDelayed), TestHelper.toString(liveTimestamp))
                .put("deadTimestamp", TestHelper.toString(deadTimestampDelayed), TestHelper.toString(deadTimestamp)));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.SUSPEND, departmentUser,
            "could you please explain what you will pay the successful applicant");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, postUser,
            new ResourceChangeListRepresentation()
                .put("description", "description", "description 2")
                .put("organizationName", "organization name 2", "organization name")
                .put("location",
                    ObjectUtils.orderedMap("name", "london", "domicile", "GB", "googleId", "ttt",
                        "latitude", BigDecimal.TEN.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.TEN.stripTrailingZeros().toPlainString()),
                    ObjectUtils.orderedMap("name", "birmingham", "domicile", "GB", "googleId", "uuu",
                        "latitude", BigDecimal.ZERO.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.ZERO.stripTrailingZeros().toPlainString()))
                .put("applyWebsite", "http://www.facebook.com", null)
                .put("applyDocument", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT"), Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(5), Action.CORRECT, postUser,
            "i uploaded a document this time which explains that");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(6), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("liveTimestamp", liveTimestamp.toString(), null)
                .put("deadTimestamp", deadTimestamp.toString(), null));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(7), Action.ACCEPT, boardUser, "accepting without time constraints");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(8), Action.SUSPEND, boardUser, "comment");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(9), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("applyWebsite", null, "http://www.twitter.com")
                .put("applyDocument", ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"), null)
                .put("postCategories", Arrays.asList("p2", "p1"), Arrays.asList("p1", "p2"))
                .put("liveTimestamp", null, TestHelper.toString(liveTimestampDelayed))
                .put("deadTimestamp", null, TestHelper.toString(deadTimestampDelayed)));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(10), Action.ACCEPT, boardUser,
            "this looks good now - i replaced the document with the complete website for the opportunity");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(11), Action.PUBLISH);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(12), Action.REJECT, departmentUser,
            "we have received a complaint, we're closing down the post");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(13), Action.RESTORE, boardUser,
            "sorry we made a mistake, we're restoring the post");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(14), Action.RETIRE);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(15), Action.WITHDRAW, postUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(16), Action.RESTORE, postUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(17), Action.PUBLISH);
    }

    private PostRepresentation verifyPostPost(Long boardId, PostDTO postDTO) {
        return transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.postPost(boardId, postDTO);

            assertEquals(postDTO.getName(), postR.getName());
            assertEquals(postDTO.getSummary(), postR.getSummary());
            assertEquals(postDTO.getDescription(), postR.getDescription());
            assertEquals(postDTO.getOrganizationName(), postR.getOrganizationName());
            verifyLocation(postDTO.getLocation(), postR);

            assertEquals(Optional.ofNullable(postDTO.getPostCategories()).orElse(new ArrayList<>()), postR.getPostCategories());
            assertEquals(Optional.ofNullable(postDTO.getMemberCategories()).orElse(new ArrayList<>()), postR.getMemberCategories());
            assertEquals(postDTO.getExistingRelation(), postR.getExistingRelation());
            assertEquals(postDTO.getExistingRelationExplanation(), postR.getExistingRelationExplanation());
            assertEquals(postDTO.getApplyWebsite(), postR.getApplyWebsite());

            DocumentDTO applyDocumentDTO = postDTO.getApplyDocument();
            verifyDocument(applyDocumentDTO, postR.getApplyDocument());

            assertEquals(postDTO.getApplyEmail(), postR.getApplyEmail());
            assertEquals(postDTO.getLiveTimestamp().truncatedTo(ChronoUnit.SECONDS), postR.getLiveTimestamp().truncatedTo(ChronoUnit.SECONDS));
            assertEquals(postDTO.getDeadTimestamp().truncatedTo(ChronoUnit.SECONDS), postR.getDeadTimestamp().truncatedTo(ChronoUnit.SECONDS));

            Post post = postService.getPost(postR.getId());

            Board board = boardService.getBoard(postR.getBoard().getId());
            Department department = departmentService.getDepartment(postR.getBoard().getDepartment().getId());
            assertThat(post.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(post, board, department));
            return postR;
        });
    }

    private PostRepresentation verifyPatchPost(User user, Long postId, PostPatchDTO postDTO, PostOperation operation, State expectedState) {
        testUserService.setAuthentication(user.getId());
        return transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            PostRepresentation postR = operation.execute();

            Optional<String> nameOptional = postDTO.getName();
            assertEquals(nameOptional == null ? post.getName() : nameOptional.orElse(null), postR.getName());

            Optional<String> summaryOptional = postDTO.getSummary();
            assertEquals(summaryOptional == null ? post.getSummary() : summaryOptional.orElse(null), postR.getSummary());

            Optional<String> descriptionOptional = postDTO.getDescription();
            assertEquals(descriptionOptional == null ? post.getDescription() : descriptionOptional.orElse(null), postR.getDescription());

            Optional<String> organizationNameOptional = postDTO.getOrganizationName();
            assertEquals(organizationNameOptional == null ? post.getOrganizationName() : organizationNameOptional.orElse(null), postR.getOrganizationName());

            Optional<LocationDTO> locationOptional = postDTO.getLocation();
            verifyLocation(locationOptional == null ? post.getLocation() : locationOptional.orElse(null), postR);

            Optional<List<String>> postCategoriesOptional = postDTO.getPostCategories();
            assertEquals(postCategoriesOptional == null ? resourceService.getCategories(post, CategoryType.POST) : postCategoriesOptional.orElse(null), postR.getPostCategories());

            Optional<List<MemberCategory>> memberCategoriesOptional = postDTO.getMemberCategories();
            assertEquals(memberCategoriesOptional == null ? MemberCategory.fromStrings(resourceService.getCategories(post, CategoryType.MEMBER)) :
                memberCategoriesOptional.orElse(null), postR.getMemberCategories());

            Optional<ExistingRelation> existingRelationOptional = postDTO.getExistingRelation();
            assertEquals(existingRelationOptional == null ? post.getExistingRelation() : existingRelationOptional.orElse(null), postR.getExistingRelation());

            Optional<LinkedHashMap<String, Object>> existingRelationExplanationOptional = postDTO.getExistingRelationExplanation();
            assertEquals(existingRelationExplanationOptional == null ? postService.mapExistingRelationExplanation(post.getExistingRelationExplanation()) :
                existingRelationExplanationOptional.orElse(null), postR.getExistingRelationExplanation());

            Optional<String> applyWebsiteOptional = postDTO.getApplyWebsite();
            assertEquals(applyWebsiteOptional == null ? post.getApplyWebsite() : applyWebsiteOptional.orElse(null), postR.getApplyWebsite());

            Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
            verifyDocument(applyDocumentOptional == null ? post.getApplyDocument() : applyDocumentOptional.orElse(null), postR.getApplyDocument());

            Optional<String> applyEmail = postDTO.getApplyEmail();
            assertEquals(applyEmail == null ? post.getApplyEmail() : applyEmail.orElse(null), postR.getApplyEmail());

            Optional<LocalDateTime> liveTimestampOptional = postDTO.getLiveTimestamp();
            assertEquals(liveTimestampOptional == null ? post.getLiveTimestamp() : liveTimestampOptional.orElse(null), postR.getLiveTimestamp());

            Optional<LocalDateTime> deadTimestampOptional = postDTO.getDeadTimestamp();
            assertEquals(deadTimestampOptional == null ? post.getDeadTimestamp() : deadTimestampOptional.orElse(null), postR.getDeadTimestamp());

            Assert.assertEquals(expectedState, postR.getState());
            return postR;
        });
    }

    private void verifyPublishAndRetirePost(Long postId, State expectedState) {
        PostRepresentation postR;

        // Check that the scheduler does not create duplicate operations
        for (int i = 0; i < 2; i++) {
            transactionTemplate.execute(status -> {
                postService.publishAndRetirePosts();
                return null;
            });
        }

        postR = transactionTemplate.execute(status -> postApi.getPost(postId));
        Assert.assertEquals(expectedState, postR.getState());
    }

    private void verifyLocation(LocationDefinition locationDefinition, PostRepresentation postR) {
        if (locationDefinition == null) {
            fail("Post location can never be set to null");
        }

        LocationRepresentation locationR = postR.getLocation();
        assertEquals(locationDefinition.getName(), locationR.getName());
        assertEquals(locationDefinition.getDomicile(), locationR.getDomicile());
        assertEquals(locationDefinition.getGoogleId(), locationR.getGoogleId());
        assertTrue(locationDefinition.getLatitude().compareTo(locationR.getLatitude()) == 0);
        assertTrue(locationDefinition.getLongitude().compareTo(locationR.getLongitude()) == 0);
    }

    private void verifyPostActions(List<User> adminUsers, User postUser, Collection<User> unprivilegedUsers, Long postId, State state, Map<Action, Runnable> operations) {
        Collection<Action> publicActions = PUBLIC_ACTIONS.get(state);
        if (CollectionUtils.isEmpty(publicActions)) {
            verifyResourceActions(Scope.POST, postId, operations);
            verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations);
        } else {
            verifyResourceActions(Scope.POST, postId, operations, publicActions);
            verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations, publicActions);
        }

        verifyResourceActions(adminUsers, Scope.POST, postId, operations, ADMIN_ACTIONS.get(state));
        verifyResourceActions(postUser, Scope.POST, postId, operations, AUTHOR_ACTIONS.get(state));
    }

    private void verifyPostPostAndSetState(User user, Long boardId, PostDTO postDTO, State state,
                                           LinkedHashMap<User, LinkedHashMap<Long, LinkedHashMultimap<State, String>>> posts, LocalDateTime baseline, int seconds) {
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> userPosts = posts.computeIfAbsent(user, k -> new LinkedHashMap<>());
        LinkedHashMultimap<State, String> userStatePosts = userPosts.computeIfAbsent(boardId, k -> LinkedHashMultimap.create());
        PostRepresentation postR = verifyPostPost(boardId, postDTO);
        transactionTemplate.execute(status -> postService.getPost(postR.getId()).setState(state).setUpdatedTimestamp(baseline.minusSeconds(seconds)));
        userStatePosts.put(state, postDTO.getName());
    }

    private void reschedulePost(String postName, LocalDateTime baseline, int seconds) {
        transactionTemplate.execute(status -> {
            Post post = postService.getByName(postName).get(0);
            post.setUpdatedTimestamp(baseline.minusSeconds(seconds));
            return null;
        });
    }

    private void verifyUnprivilegedPostUser(LinkedHashMap<Long, LinkedHashMultimap<State, String>> postNames) {
        TestHelper.verifyResources(
            transactionTemplate.execute(status -> postApi.getPosts(null)),
            Collections.emptyList(),
            null);

        LinkedHashMultimap<State, String> statePostNames = getPostNamesByState(postNames);
        LinkedHashMultimap<State, PostRepresentation> statePosts = getPostsByState(transactionTemplate.execute(status -> postApi.getPosts(true)));
        statePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(statePosts.get(state)),
                Lists.newArrayList(statePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(Lists.newArrayList(PUBLIC_ACTIONS.get(state)))));

        for (Long boardId : postNames.keySet()) {
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, null)),
                Collections.emptyList(),
                null);

            LinkedHashMultimap<State, String> boardStatePostNames = postNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> boardStatePosts = getPostsByState(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, true)));
            boardStatePostNames.keySet().forEach(state ->
                TestHelper.verifyResources(
                    Lists.newArrayList(boardStatePosts.get(state)),
                    Lists.newArrayList(boardStatePostNames.get(state)),
                    new TestHelper.ExpectedActions()
                        .add(Lists.newArrayList(PUBLIC_ACTIONS.get(state)))));
        }
    }

    private void verifyPrivilegedPostUser(LinkedHashMap<Long, LinkedHashMultimap<State, String>> publicPostNames,
                                          LinkedHashMap<Long, LinkedHashMultimap<State, String>> postNames, PostAdminContext adminContext) {
        LinkedHashMultimap<State, String> statePostNames = getPostNamesByState(postNames);
        LinkedHashMultimap<State, PostRepresentation> statePosts = getPostsByState(transactionTemplate.execute(status -> postApi.getPosts(null)));
        statePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(statePosts.get(state)),
                Lists.newArrayList(statePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(getAdminActions(state, adminContext))));

        LinkedHashMultimap<State, String> publicStatePostNames = getPostNamesByState(publicPostNames);
        LinkedHashMultimap<State, PostRepresentation> mergedStatePosts = getPostsByState(transactionTemplate.execute(status -> postApi.getPosts(true)));
        LinkedHashMultimap<State, String> mergedStatePostNames = mergePostNamesPreservingOrder(statePostNames, publicStatePostNames);
        mergedStatePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(mergedStatePosts.get(state)),
                Lists.newArrayList(mergedStatePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(Lists.newArrayList(PUBLIC_ACTIONS.get(state)))
                    .addAll(Lists.newArrayList(statePostNames.get(state)), getAdminActions(state, adminContext))));

        for (Long boardId : postNames.keySet()) {
            LinkedHashMultimap<State, String> boardStatePostNames = postNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> boardStatePosts = getPostsByState(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, null)));
            boardStatePostNames.keySet().forEach(state ->
                TestHelper.verifyResources(
                    Lists.newArrayList(boardStatePosts.get(state)),
                    Lists.newArrayList(boardStatePostNames.get(state)),
                    new TestHelper.ExpectedActions()
                        .add(getAdminActions(state, adminContext))));

            LinkedHashMultimap<State, String> publicBoardStatePostNames = publicPostNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> mergedBoardStatePosts = getPostsByState(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, null)));
            LinkedHashMultimap<State, String> mergedBoardStatePostNames = mergePostNamesPreservingOrder(boardStatePostNames, publicBoardStatePostNames);
            mergedBoardStatePostNames.keySet().forEach(state ->
                TestHelper.verifyResources(
                    Lists.newArrayList(mergedBoardStatePosts.get(state)),
                    Lists.newArrayList(mergedBoardStatePostNames.get(state)),
                    new TestHelper.ExpectedActions()
                        .add(Lists.newArrayList(PUBLIC_ACTIONS.get(state)))
                        .addAll(Lists.newArrayList(boardStatePostNames.get(state)), getAdminActions(state, adminContext))));
        }
    }

    private List<Action> getAdminActions(State state, PostAdminContext adminContext) {
        return Lists.newArrayList(adminContext == PostAdminContext.ADMIN ? ADMIN_ACTIONS.get(state) : AUTHOR_ACTIONS.get(state));
    }

    private LinkedHashMultimap<State, PostRepresentation> getPostsByState(List<PostRepresentation> postRs) {
        LinkedHashMultimap<State, PostRepresentation> postsByState = LinkedHashMultimap.create();
        postRs.forEach(postR -> postsByState.put(postR.getState(), postR));
        return postsByState;
    }

    private LinkedHashMultimap<State, String> getPostNamesByState(LinkedHashMap<Long, LinkedHashMultimap<State, String>> boardPostNameMap) {
        LinkedHashMultimap<State, String> postNamesByState = LinkedHashMultimap.create();
        boardPostNameMap.entrySet().forEach(entry -> postNamesByState.putAll(entry.getValue()));
        return postNamesByState;
    }

    private LinkedHashMultimap<State, String> mergePostNamesPreservingOrder(LinkedHashMultimap<State, String> postNames, LinkedHashMultimap<State, String> publicPostNames) {
        LinkedHashMultimap<State, String> mergedPostNames = LinkedHashMultimap.create();
        postNames.keySet().forEach(state -> {
            List<String> merge = Lists.newArrayList(postNames.get(state));
            merge.addAll(publicPostNames.get(state));
            merge.sort(Comparator.naturalOrder());
            mergedPostNames.putAll(state, merge);
        });

        return mergedPostNames;
    }

    private interface PostOperation {
        PostRepresentation execute();
    }

    private enum PostAdminContext {
        ADMIN, AUTHOR
    }

}
