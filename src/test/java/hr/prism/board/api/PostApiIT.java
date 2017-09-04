package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.sendgrid.Attachments;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.definition.LocationDefinition;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.ResourceEvent;
import hr.prism.board.exception.*;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.representation.*;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.TestUserActivityService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.util.ObjectUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
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

    private static LinkedHashMultimap<State, Action> MEMBER_ACTIONS = LinkedHashMultimap.create();

    private static LinkedHashMultimap<State, Action> PUBLIC_ACTIONS = LinkedHashMultimap.create();

    static {
        ADMIN_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.SUSPENDED, Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT));
        ADMIN_ACTIONS.putAll(State.PENDING, Arrays.asList(Action.VIEW, Action.EDIT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.PURSUE, Action.EDIT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.EXPIRED, Arrays.asList(Action.VIEW, Action.EDIT, Action.REJECT, Action.SUSPEND));
        ADMIN_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE));
        ADMIN_ACTIONS.putAll(State.WITHDRAWN, Arrays.asList(Action.VIEW, Action.EDIT));

        AUTHOR_ACTIONS.putAll(State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.SUSPENDED, Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW, Action.CORRECT));
        AUTHOR_ACTIONS.putAll(State.PENDING, Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.PURSUE, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.EXPIRED, Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW));
        AUTHOR_ACTIONS.putAll(State.WITHDRAWN, Arrays.asList(Action.VIEW, Action.EDIT, Action.RESTORE));

        MEMBER_ACTIONS.putAll(State.ACCEPTED, Arrays.asList(Action.VIEW, Action.PURSUE));

        PUBLIC_ACTIONS.put(State.ACCEPTED, Action.VIEW);
    }

    @Inject
    private PostRepository postRepository;

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
            TestHelper.samplePost()
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
            TestHelper.smallSamplePost()
                .setName(board12PostName)
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
            TestHelper.smallSamplePost()
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
            TestHelper.smallSamplePost()
                .setName(board22PostName)
                .setPostCategories(Collections.singletonList("p1"))));
        unprivilegedUserPosts.put(board22Id, board22PostName);

        LinkedHashMultimap<State, String> boardPostNames11 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames12 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames21 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames22 = LinkedHashMultimap.create();

        int postCount = 1;
        LocalDateTime baseline = LocalDateTime.now();
        LinkedHashMap<User, LinkedHashMap<Long, LinkedHashMultimap<State, String>>> posts = new LinkedHashMap<>();
        for (State state : Arrays.stream(State.values()).filter(state -> !Arrays.asList(State.ARCHIVED, State.PREVIOUS).contains(state)).collect(Collectors.toList())) {
            if (state == State.DRAFT) {
                reschedulePost(board11PostName, baseline, postCount);
                boardPostNames11.put(state, board11PostName);
                postCount++;
            }

            User postUser1 = testUserService.authenticate();
            for (int i = 1; i < 3; i++) {
                String name = boardR11.getName() + " " + state.name().toLowerCase() + " " + i;
                verifyPostPostAndSetState(postUser1, board11Id,
                    TestHelper.samplePost()
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
                    TestHelper.smallSamplePost()
                        .setName(name)
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
                    TestHelper.smallSamplePost()
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
                    TestHelper.smallSamplePost()
                        .setName(name)
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
            PostDTO postDTO = new PostDTO()
                .setName("post")
                .setSummary("summary")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_APPLY, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithCorruptedApply() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post")
                .setSummary("summary")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setApplyWebsite("http://www.google.com")
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_APPLY, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithMissingRelationForUserWithoutAuthorRole() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        testUserService.authenticate();
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post")
                .setSummary("summary")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithCategoriesForBoardWithoutCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithoutCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithInvalidCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.samplePost().setPostCategories(Collections.singletonList("p4"));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.INVALID_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.samplePost().setMemberCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT));
            ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.INVALID_POST_MEMBER_CATEGORIES, status);
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
            ExceptionUtils.verifyException(BoardException.class, () ->
                    postApi.patchPost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.empty())),
                ExceptionCode.MISSING_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class, () ->
                    postApi.patchPost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.empty())),
                ExceptionCode.MISSING_POST_MEMBER_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class, () ->
                    postApi.patchPost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.of(Collections.singletonList("p4")))),
                ExceptionCode.INVALID_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class, () ->
                    postApi.patchPost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.of(Collections.singletonList(MemberCategory.RESEARCH_STUDENT)))),
                ExceptionCode.INVALID_POST_MEMBER_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.empty())));
        transactionTemplate.execute(status -> boardApi.patchBoard(boardId, new BoardPatchDTO().setPostCategories(Optional.empty())));

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class, () ->
                    postApi.patchPost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.of(Collections.singletonList("p1")))),
                ExceptionCode.CORRUPTED_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class, () ->
                    postApi.patchPost(postId, new PostPatchDTO()
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
        departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(
            Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT, MemberCategory.RESEARCH_STUDENT))));

        User boardUser = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            Board board = boardService.getBoard(boardId);
            userRoleService.createOrUpdateUserRole(board, boardUser, Role.ADMINISTRATOR);
            return null;
        });

        List<User> adminUsers = Arrays.asList(departmentUser, boardUser);

        // Create post
        testUserActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForNewActivities(departmentUserId);

        Long boardUserId = boardUser.getId();
        listenForNewActivities(boardUserId);

        User postUser = testUserService.authenticate();
        PostRepresentation postR = verifyPostPost(boardId, TestHelper.samplePost());
        Long postId = postR.getId();

        String departmentName = boardR.getDepartment().getName();
        String boardName = boardR.getName();
        String postName = postR.getName();
        String departmentUserGivenName = departmentUser.getGivenName();
        String boardUserGivenName = boardUser.getGivenName();
        String postUserGivenName = postUser.getGivenName();
        String resourceRedirect = serverUrl + "/redirect?resource=" + postId;

        testUserActivityService.verify(departmentUserId, new TestUserActivityService.ActivityInstance(postId, Activity.NEW_POST_PARENT_ACTIVITY));
        testUserActivityService.verify(boardUserId, new TestUserActivityService.ActivityInstance(postId, Activity.NEW_POST_PARENT_ACTIVITY));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_PARENT_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", departmentUserGivenName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_PARENT_NOTIFICATION, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        testUserActivityService.stop();
        testNotificationService.stop();

        // Create unprivileged users
        Collection<User> unprivilegedUsers = makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.samplePost()).values();
        testUserActivityService.record();
        testNotificationService.record();

        // Clear activity streams for the admin users
        for (Long userId : new Long[]{departmentUserId, boardUserId}) {
            testUserService.setAuthentication(userId);
            List<Long> activityIds = userApi.getActivities().stream().map(ActivityRepresentation::getId).collect(Collectors.toList());
            Assert.assertEquals(2, activityIds.size());
            for (Long activityId : activityIds) {
                userApi.dismissActivity(activityId);
            }

            listenForNewActivities(userId);
        }

        Long postUserId = postUser.getId();
        listenForNewActivities(postUserId);

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.VIEW, () -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("address")))
            .put(Action.EDIT, () -> postApi.patchPost(postId, new PostPatchDTO()))
            .put(Action.ACCEPT, () -> postApi.executeAction(postId, "accept", new PostPatchDTO()))
            .put(Action.SUSPEND, () -> postApi.executeAction(postId, "suspend", new PostPatchDTO().setComment("comment")))
            .put(Action.CORRECT, () -> postApi.executeAction(postId, "correct", new PostPatchDTO()))
            .put(Action.REJECT, () -> postApi.executeAction(postId, "reject", new PostPatchDTO().setComment("comment")))
            .put(Action.RESTORE, () -> postApi.executeAction(postId, "restore", new PostPatchDTO()))
            .put(Action.WITHDRAW, () -> postApi.executeAction(postId, "withdraw", new PostPatchDTO()))
            .build();

        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.DRAFT, operations);

        // Check that we do not audit viewing
        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("address")));

        LocalDateTime liveTimestamp = postR.getLiveTimestamp();
        LocalDateTime deadTimestamp = postR.getDeadTimestamp();

        LocalDateTime liveTimestampDelayed = LocalDateTime.now().plusWeeks(4L).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime deadTimestampDelayed = LocalDateTime.now().plusWeeks(8L).truncatedTo(ChronoUnit.SECONDS);

        // Check that the author can update the post
        PostPatchDTO updateDTO = new PostPatchDTO()
            .setName(Optional.of("post 2"))
            .setSummary(Optional.of("summary 2"))
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

        postR = verifyPatchPost(postUser, postId, updateDTO, () -> postApi.patchPost(postId, updateDTO), State.DRAFT);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.DRAFT, operations);
        postName = postR.getName();

        // Check that the administrator can make changes and suspend the post
        PostPatchDTO suspendDTO = new PostPatchDTO()
            .setLiveTimestamp(Optional.of(liveTimestamp))
            .setDeadTimestamp(Optional.of(deadTimestamp))
            .setComment("could you please explain what you will pay the successful applicant");

        verifyPatchPost(departmentUser, postId, suspendDTO, () -> postApi.executeAction(postId, "suspend", suspendDTO), State.SUSPENDED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.SUSPENDED, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.SUSPEND_POST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("comment", "could you please explain what you will pay the successful applicant").put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        // Check that the author can make changes and correct the post
        PostPatchDTO correctDTO = new PostPatchDTO()
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

        testUserActivityService.verify(departmentUserId, new TestUserActivityService.ActivityInstance(postId, Activity.CORRECT_POST_ACTIVITY));
        testUserActivityService.verify(boardUserId, new TestUserActivityService.ActivityInstance(postId, Activity.CORRECT_POST_ACTIVITY));
        testUserActivityService.verify(postUserId);

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CORRECT_POST_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder().put("recipient", departmentUserGivenName).put("post", postName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.CORRECT_POST_NOTIFICATION, boardUser,
                ImmutableMap.<String, String>builder().put("recipient", boardUserGivenName).put("post", postName).put("department", departmentName).put("board", boardName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        // Check that the administrator can accept post in the suspended state
        PostPatchDTO acceptDTO = new PostPatchDTO()
            .setLiveTimestamp(Optional.empty())
            .setDeadTimestamp(Optional.empty())
            .setComment("accepting without time constraints");

        verifyPatchPost(boardUser, postId, acceptDTO, () -> postApi.executeAction(postId, "accept", acceptDTO), State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.ACCEPT_POST_ACTIVITY));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.ACCEPT_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("publicationSchedule", "immediately").put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        // Suspend the post so that it can be accepted again
        verifyPatchPost(boardUser, postId, new PostPatchDTO(),
            () -> postApi.executeAction(postId, "suspend", new PostPatchDTO().setComment("comment")), State.SUSPENDED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.SUSPENDED, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.SUSPEND_POST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("comment", "comment").put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        // Check that the administrator can make further changes and accept the post again
        PostPatchDTO acceptPendingDTO = new PostPatchDTO()
            .setApplyWebsite(Optional.of("http://www.twitter.com"))
            .setPostCategories(Optional.of(Arrays.asList("p1", "p2")))
            .setLiveTimestamp(Optional.of(liveTimestampDelayed))
            .setDeadTimestamp(Optional.of(deadTimestampDelayed))
            .setComment("this looks good now - i replaced the document with the complete website for the opportunity");

        verifyPatchPost(boardUser, postId, acceptPendingDTO, () -> postApi.executeAction(postId, "accept", acceptPendingDTO), State.PENDING);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.PENDING, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.ACCEPT_POST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.ACCEPT_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("publicationSchedule",
                    "on or around " + postR.getLiveTimestamp().format(BoardUtils.DATETIME_FORMATTER) + ". We will send you a follow-up message when your post has gone live")
                .put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        // Check that the post stays in pending state when the update job runs
        verifyPublishAndRetirePost(postId, State.PENDING);

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setLiveTimestamp(liveTimestamp);
            post.setDeadTimestamp(deadTimestamp);
            return null;
        });

        // Should be notified
        testUserService.setAuthentication(departmentUserId);
        Long departmentMember1Id = transactionTemplate.execute(status ->
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new ResourceUserDTO().setUser(
                    new UserDTO()
                        .setGivenName("student1")
                        .setSurname("student1")
                        .setEmail("student1@student1.com"))
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.MEMBER)
                            .setExpiryDate(LocalDate.now().plusDays(1))
                            .setCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))))).getUser().getId());

        // Should be notified
        Long departmentMember2Id = transactionTemplate.execute(status ->
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new ResourceUserDTO().setUser(
                    new UserDTO()
                        .setGivenName("student2")
                        .setSurname("student2")
                        .setEmail("student2@student2.com"))
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.MEMBER)
                            .setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT))))).getUser().getId());

        // Should not be notified - suppressed
        Long departmentMember3Id = transactionTemplate.execute(status ->
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new ResourceUserDTO().setUser(
                    new UserDTO()
                        .setGivenName("student3")
                        .setSurname("student3")
                        .setEmail("student3@student3.com"))
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.MEMBER)
                            .setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT))))).getUser().getId());

        testUserService.setAuthentication(departmentMember3Id);
        userApi.postSuppressions();

        // Should not be notified
        testUserService.setAuthentication(departmentUserId);
        Long departmentMember4Id = transactionTemplate.execute(status ->
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new ResourceUserDTO().setUser(
                    new UserDTO()
                        .setGivenName("student4")
                        .setSurname("student4")
                        .setEmail("student4@student4.com"))
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.MEMBER)
                            .setExpiryDate(LocalDate.now().plusDays(1))
                            .setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT))))).getUser().getId());

        // Should not be notified
        Long departmentMember5Id = transactionTemplate.execute(status ->
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new ResourceUserDTO().setUser(
                    new UserDTO()
                        .setGivenName("student5")
                        .setSurname("student5")
                        .setEmail("student5@student5.com"))
                    .setRoles(Collections.singleton(
                        new UserRoleDTO()
                            .setRole(Role.MEMBER)
                            .setExpiryDate(LocalDate.now().minusDays(1))
                            .setCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))))).getUser().getId());

        listenForNewActivities(departmentMember1Id);
        listenForNewActivities(departmentMember2Id);
        listenForNewActivities(departmentMember3Id);
        listenForNewActivities(departmentMember4Id);
        listenForNewActivities(departmentMember5Id);

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        User departmentMember1 = userCacheService.findOne(departmentMember1Id);
        User departmentMember2 = userCacheService.findOne(departmentMember2Id);

        String departmentMember1Uuid = departmentMember1.getUuid();
        String departmentMember2Uuid = departmentMember2.getUuid();
        String parentRedirect = serverUrl + "/redirect?resource=" + boardId;

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_ACTIVITY));
        testUserActivityService.verify(departmentMember1Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testUserActivityService.verify(departmentMember2Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testUserActivityService.verify(departmentMember3Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testUserActivityService.verify(departmentMember4Id);
        testUserActivityService.verify(departmentMember5Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember1,
                ImmutableMap.<String, String>builder().put("recipient", "student1").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "register")
                    .put("parentRedirect", parentRedirect).put("recipientUuid", departmentMember1Uuid).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember2,
                ImmutableMap.<String, String>builder().put("recipient", "student2").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "register")
                    .put("parentRedirect", parentRedirect).put("recipientUuid", departmentMember2Uuid).build()));

        // Check that the administrator can reject the post
        PostPatchDTO rejectDTO = new PostPatchDTO()
            .setComment("we have received a complaint, we're closing down the post");

        verifyPatchPost(departmentUser, postId, rejectDTO, () -> postApi.executeAction(postId, "reject", rejectDTO), State.REJECTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.REJECTED, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.REJECT_POST_ACTIVITY));
        testUserActivityService.verify(departmentMember1Id);
        testUserActivityService.verify(departmentMember2Id);
        testUserActivityService.verify(departmentMember3Id);
        testUserActivityService.verify(departmentMember4Id);
        testUserActivityService.verify(departmentMember5Id);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("comment", "we have received a complaint, we're closing down the post").put("homeRedirect", serverUrl + "/redirect")
                .put("modal", "login").build()));

        // Check that the administrator can restore the post
        PostPatchDTO restoreFromRejectedDTO = new PostPatchDTO()
            .setComment("sorry we made a mistake, we're restoring the post");

        verifyPatchPost(boardUser, postId, restoreFromRejectedDTO, () -> postApi.executeAction(postId, "restore", restoreFromRejectedDTO), State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.RESTORE_POST_ACTIVITY));
        testUserActivityService.verify(departmentMember1Id);
        testUserActivityService.verify(departmentMember2Id);
        testUserActivityService.verify(departmentMember3Id);
        testUserActivityService.verify(departmentMember4Id);
        testUserActivityService.verify(departmentMember5Id);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESTORE_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setDeadTimestamp(liveTimestamp.minusSeconds(1));
            return null;
        });

        // Check that the post now moves to the expired state when the update job runs
        verifyPublishAndRetirePost(postId, State.EXPIRED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.EXPIRED, operations);

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.RETIRE_POST_ACTIVITY));
        testUserActivityService.verify(departmentMember1Id);
        testUserActivityService.verify(departmentMember2Id);
        testUserActivityService.verify(departmentMember3Id);
        testUserActivityService.verify(departmentMember4Id);
        testUserActivityService.verify(departmentMember5Id);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RETIRE_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                .put("resourceRedirect", resourceRedirect).put("modal", "login").build()));

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

        testUserActivityService.verify(departmentUserId);
        testUserActivityService.verify(boardUserId);
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_ACTIVITY));
        testUserActivityService.verify(departmentMember1Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testUserActivityService.verify(departmentMember2Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testUserActivityService.verify(departmentMember3Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testUserActivityService.verify(departmentMember4Id);
        testUserActivityService.verify(departmentMember5Id, new TestUserActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("resourceRedirect", resourceRedirect).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember1,
                ImmutableMap.<String, String>builder().put("recipient", "student1").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "register")
                    .put("parentRedirect", parentRedirect).put("recipientUuid", departmentMember1Uuid).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember2,
                ImmutableMap.<String, String>builder().put("recipient", "student2").put("department", departmentName).put("board", boardName).put("post", postName)
                    .put("organization", "organization name").put("summary", "summary 2").put("resourceRedirect", resourceRedirect).put("modal", "register")
                    .put("parentRedirect", parentRedirect).put("recipientUuid", departmentMember2Uuid).build()));
        testUserActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(postUser.getId());
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> postApi.getPostOperations(postId));
        Assert.assertEquals(18, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, postUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, postUser,
            new ChangeListRepresentation()
                .put("name", "post", "post 2")
                .put("summary", "summary", "summary 2")
                .put("description", null, "description")
                .put("organizationName", "organization name", "organization name 2")
                .put("location",
                    ObjectUtils.orderedMap("name", "krakow", "domicile", "PL", "googleId", "sss",
                        "latitude", 1, "longitude", 1),
                    ObjectUtils.orderedMap("name", "london", "domicile", "GB", "googleId", "ttt",
                        "latitude", 10, "longitude", 10))
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
            new ChangeListRepresentation()
                .put("liveTimestamp", TestHelper.toString(liveTimestampDelayed), TestHelper.toString(liveTimestamp))
                .put("deadTimestamp", TestHelper.toString(deadTimestampDelayed), TestHelper.toString(deadTimestamp)));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.SUSPEND, departmentUser,
            "could you please explain what you will pay the successful applicant");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, postUser,
            new ChangeListRepresentation()
                .put("description", "description", "description 2")
                .put("organizationName", "organization name 2", "organization name")
                .put("location",
                    ObjectUtils.orderedMap("name", "london", "domicile", "GB", "googleId", "ttt",
                        "latitude", 10, "longitude", 10),
                    ObjectUtils.orderedMap("name", "birmingham", "domicile", "GB", "googleId", "uuu",
                        "latitude", 0, "longitude", 0))
                .put("applyWebsite", "http://www.facebook.com", null)
                .put("applyDocument", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", Arrays.asList("MASTER_STUDENT", "UNDERGRADUATE_STUDENT"), Arrays.asList("UNDERGRADUATE_STUDENT", "MASTER_STUDENT")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(5), Action.CORRECT, postUser,
            "i uploaded a document this time which explains that");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(6), Action.EDIT, boardUser,
            new ChangeListRepresentation()
                .put("liveTimestamp", TestHelper.toString(liveTimestamp), null)
                .put("deadTimestamp", TestHelper.toString(deadTimestamp), null));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(7), Action.ACCEPT, boardUser, "accepting without time constraints");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(8), Action.SUSPEND, boardUser, "comment");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(9), Action.EDIT, boardUser,
            new ChangeListRepresentation()
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


    @Test
    @Sql("classpath:data/organization_autosuggest_setup.sql")
    public void shouldSuggestOrganizations() {
        List<String> organizations = postApi.lookupOrganizations("Computer");
        Assert.assertEquals(3, organizations.size());

        Assert.assertEquals("Computer Science Department", organizations.get(0));
        Assert.assertEquals("Department of Computer Science", organizations.get(1));
        Assert.assertEquals("Laboratory for the Foundations of Computer Science", organizations.get(2));

        organizations = postApi.lookupOrganizations("Computer Science Laboratory");
        Assert.assertEquals(3, organizations.size());

        Assert.assertEquals("Laboratory for the Foundations of Computer Science", organizations.get(0));
        Assert.assertEquals("Computer Science Department", organizations.get(1));
        Assert.assertEquals("Department of Computer Science", organizations.get(2));

        organizations = postApi.lookupOrganizations("School of Informatics");
        Assert.assertEquals(1, organizations.size());

        Assert.assertEquals("School of Informatics", organizations.get(0));

        organizations = postApi.lookupOrganizations("Physics");
        Assert.assertEquals(1, organizations.size());

        organizations = postApi.lookupOrganizations("Mathematics");
        Assert.assertEquals(0, organizations.size());
    }

    @Test
    public void shouldCountPostViewsReferralsAndResponses() throws IOException {
        Long boardUserId = testUserService.authenticate().getId();
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()));
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();

        transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost()));
        Long postId = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.smallSamplePost())).getId();

        Long memberUser1 = testUserService.authenticate().getId();
        Long memberUser2 = testUserService.authenticate().getId();

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(new UserDTO().setId(memberUser1)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));
        transactionTemplate.execute(status -> resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(new UserDTO().setId(memberUser2)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));

        testUserService.setAuthentication(memberUser1);
        PostRepresentation viewPostMemberUser1 = transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser1")));
        verifyViewReferralAndResponseCounts(postId, 1L, 0L, 0L);
        String referral1 = viewPostMemberUser1.getReferral();
        Assert.assertNotNull(referral1);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser1")));
        verifyViewReferralAndResponseCounts(postId, 1L, 0L, 0L);

        testUserService.setAuthentication(memberUser2);
        PostRepresentation viewPostMemberUser2 = transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser2")));
        verifyViewReferralAndResponseCounts(postId, 2L, 0L, 0L);
        String referral2 = viewPostMemberUser2.getReferral();
        Assert.assertNotNull(referral2);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser2")));
        verifyViewReferralAndResponseCounts(postId, 2L, 0L, 0L);

        testUserService.unauthenticate();
        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown1")));
        verifyViewReferralAndResponseCounts(postId, 3L, 0L, 0L);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown1")));
        verifyViewReferralAndResponseCounts(postId, 3L, 0L, 0L);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown2")));
        verifyViewReferralAndResponseCounts(postId, 4L, 0L, 0L);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown2", "proxy2")));
        verifyViewReferralAndResponseCounts(postId, 4L, 0L, 0L);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown3", "proxy3")));
        verifyViewReferralAndResponseCounts(postId, 5L, 0L, 0L);

        transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown3", "proxy3")));
        verifyViewReferralAndResponseCounts(postId, 5L, 0L, 0L);

        PostRepresentation viewPostUnknown1 = transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown1")));
        Assert.assertNull(viewPostUnknown1.getReferral());

        TestHelper.MockHttpServletResponse response = TestHelper.mockHttpServletResponse();
        verifyPostReferral(referral1, response, "http://www.google.co.uk");
        verifyViewReferralAndResponseCounts(postId, 5L, 1L, 0L);

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> verifyPostReferral(referral1, response, "http://www.google.co.uk"), ExceptionCode.FORBIDDEN_REFERRAL, status));

        testUserService.setAuthentication(boardUserId);
        DocumentDTO documentDTO = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf").setFileName("attachments1.pdf");
        transactionTemplate.execute(status -> postApi.patchPost(postId, new PostPatchDTO().setApplyDocument(Optional.of(documentDTO)).setApplyEmail(Optional.empty())));

        testUserService.setAuthentication(memberUser2);
        verifyPostReferral(referral2, response, "http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf");
        verifyViewReferralAndResponseCounts(postId, 5L, 2L, 0L);

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardException.class,
            () -> postApi.postPostResponse(postId, new ResourceEventDTO()), ExceptionCode.INVALID_RESOURCE_EVENT, status));

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> postApi.patchPost(postId, new PostPatchDTO().setApplyDocument(Optional.empty()).setApplyEmail(Optional.of("email@email.com"))));

        testUserService.setAuthentication(memberUser1);
        ResourceEventDTO resourceEventDTO = new ResourceEventDTO().setDocumentResume(documentDTO).setWebsiteResume("website").setCoveringNote("note");
        transactionTemplate.execute(status -> postApi.postPostResponse(postId, resourceEventDTO));
        verifyViewReferralAndResponseCounts(postId, 5L, 2L, 1L);

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardDuplicateException.class, () ->
            postApi.postPostResponse(postId, resourceEventDTO), ExceptionCode.DUPLICATE_RESOURCE_EVENT, status));

        testUserService.setAuthentication(memberUser2);
        transactionTemplate.execute(status -> postApi.postPostResponse(postId, resourceEventDTO));
        verifyViewReferralAndResponseCounts(postId, 5L, 2L, 2L);
    }

    @Test
    public void shouldNotifyAndListPostResponses() {
        Long boardUserId = testUserService.authenticate().getId();
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()));
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();

        User postUser = testUserService.authenticate();
        Long postUserId = postUser.getId();
        String postUserGivenName = postUser.getGivenName();
        String postUserEmail = postUser.getEmail();
        Long postId = transactionTemplate.execute(status -> postApi.postPost(boardId,
            TestHelper.smallSamplePost().setApplyWebsite(null).setApplyEmail(postUserEmail))).getId();

        User memberUser1 = testUserService.authenticate();
        Long memberUser1Id = memberUser1.getId();

        User memberUser2 = testUserService.authenticate();
        Long memberUser2Id = memberUser2.getId();

        User memberUser3 = testUserService.authenticate();
        Long memberUser3Id = memberUser3.getId();

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(new UserDTO().setId(memberUser1Id)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));
        transactionTemplate.execute(status -> resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(new UserDTO().setId(memberUser2Id)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));
        transactionTemplate.execute(status -> resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(new UserDTO().setId(memberUser3Id)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));
        transactionTemplate.execute(status -> postApi.executeAction(postId, "accept", new PostPatchDTO()));

        testUserService.setAuthentication(postUserId);
        List<ActivityRepresentation> activities = transactionTemplate.execute(status -> userApi.getActivities());
        activities.forEach(activity -> transactionTemplate.execute(status -> {
            userApi.dismissActivity(activity.getId());
            return null;
        }));

        testUserActivityService.record();
        testNotificationService.record();
        listenForNewActivities(postUserId);

        testUserService.setAuthentication(memberUser1Id);
        DocumentDTO documentDTO1 = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf").setFileName("attachments1.pdf");
        Long responseId = transactionTemplate.execute(status -> postApi.postPostResponse(postId,
            new ResourceEventDTO().setDocumentResume(documentDTO1).setWebsiteResume("website1").setCoveringNote("note1"))).getId();

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESPOND_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("post", "post").put("candidate", memberUser1.getFullName())
                    .put("coveringNote", "note1").put("profile", "website1").build(),
                makeTestAttachments("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf", "attachments1.pdf", "Application")));
        testUserActivityService.verify(postUserId, new TestUserActivityService.ActivityInstance(postId, memberUser1Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY));

        testUserService.setAuthentication(postUserId);
        List<ResourceEventRepresentation> responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(1, responses.size());
        verifyPostResponse(memberUser1Id, responses.get(0), "attachments1.pdf", "website1", "note1");

        testUserService.setAuthentication(boardUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(1, responses.size());
        verifyPostResponse(memberUser1Id, responses.get(0), null, null, null);
        transactionTemplate.execute(status -> postApi.patchPost(postId, new PostPatchDTO().setApplyEmail(Optional.of("other@other.com"))));

        testUserService.setAuthentication(memberUser2Id);
        DocumentDTO documentDTO2 = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf").setFileName("attachments2.pdf");
        transactionTemplate.execute(status -> postApi.postPostResponse(postId,
            new ResourceEventDTO().setDocumentResume(documentDTO2).setWebsiteResume("website2").setCoveringNote("note2")));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESPOND_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("post", "post").put("candidate", memberUser2.getFullName())
                    .put("coveringNote", "note2").put("profile", "website2").build(),
                makeTestAttachments("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf", "attachments2.pdf", "Application")));
        testUserActivityService.verify(postUserId,
            new TestUserActivityService.ActivityInstance(postId, memberUser2Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY),
            new TestUserActivityService.ActivityInstance(postId, memberUser1Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY));

        testUserService.setAuthentication(postUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(2, responses.size());
        verifyPostResponse(memberUser2Id, responses.get(0), null, null, null);
        verifyPostResponse(memberUser1Id, responses.get(1), "attachments1.pdf", "website1", "note1");

        testUserService.setAuthentication(boardUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(2, responses.size());
        verifyPostResponse(memberUser2Id, responses.get(0), null, null, null);
        verifyPostResponse(memberUser1Id, responses.get(1), null, null, null);
        transactionTemplate.execute(status -> postApi.patchPost(postId, new PostPatchDTO().setApplyEmail(Optional.of(postUserEmail))));

        testUserService.setAuthentication(postUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(2, responses.size());
        verifyPostResponse(memberUser2Id, responses.get(0), "attachments2.pdf", "website2", "note2");
        verifyPostResponse(memberUser1Id, responses.get(1), "attachments1.pdf", "website1", "note1");

        testUserService.setAuthentication(boardUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(2, responses.size());
        verifyPostResponse(memberUser2Id, responses.get(0), null, null, null);
        verifyPostResponse(memberUser1Id, responses.get(1), null, null, null);
        transactionTemplate.execute(status -> postApi.patchPost(postId, new PostPatchDTO().setApplyEmail(Optional.of("other@other.com"))));

        testUserService.setAuthentication(memberUser3Id);
        DocumentDTO documentDTO3 = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf").setFileName("attachments3.pdf");
        transactionTemplate.execute(status -> postApi.postPostResponse(postId,
            new ResourceEventDTO().setDocumentResume(documentDTO3).setWebsiteResume("website3").setCoveringNote("note3")));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESPOND_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder().put("recipient", postUserGivenName).put("post", "post").put("candidate", memberUser3.getFullName())
                    .put("coveringNote", "note3").put("profile", "website3").build(),
                makeTestAttachments("http://res.cloudinary.com/bitfoot/image/upload/v1504040061/test/attachments1.pdf", "attachments3.pdf", "Application")));
        testUserActivityService.verify(postUserId,
            new TestUserActivityService.ActivityInstance(postId, memberUser3Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY),
            new TestUserActivityService.ActivityInstance(postId, memberUser2Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY),
            new TestUserActivityService.ActivityInstance(postId, memberUser1Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY));

        testUserActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(postUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(3, responses.size());
        verifyPostResponse(memberUser3Id, responses.get(0), null, null, null);
        verifyPostResponse(memberUser2Id, responses.get(1), "attachments2.pdf", "website2", "note2");
        verifyPostResponse(memberUser1Id, responses.get(2), "attachments1.pdf", "website1", "note1");

        testUserService.setAuthentication(boardUserId);
        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertEquals(3, responses.size());
        verifyPostResponse(memberUser3Id, responses.get(0), null, null, null);
        verifyPostResponse(memberUser2Id, responses.get(1), null, null, null);
        verifyPostResponse(memberUser1Id, responses.get(2), null, null, null);

        testUserService.setAuthentication(postUserId);
        transactionTemplate.execute(status -> {
            postApi.putPostResponseView(postId, responseId);
            return null;
        });

        ResourceEventRepresentation response1 = transactionTemplate.execute(status -> postApi.getPostResponse(postId, responseId));
        verifyPostResponse(memberUser1Id, response1, "attachments1.pdf", "website1", "note1");

        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertFalse(responses.get(0).isViewed());
        Assert.assertFalse(responses.get(1).isViewed());
        Assert.assertTrue(responses.get(2).isViewed());

        testUserService.setAuthentication(boardUserId);
        transactionTemplate.execute(status -> postApi.getPostResponses(postId, null)).forEach(response -> Assert.assertFalse(response.isViewed()));

        transactionTemplate.execute(status -> {
            postApi.putPostResponseView(postId, responseId);
            return null;
        });

        response1 = transactionTemplate.execute(status -> postApi.getPostResponse(postId, responseId));
        verifyPostResponse(memberUser1Id, response1, null, null, null);

        responses = transactionTemplate.execute(status -> postApi.getPostResponses(postId, null));
        Assert.assertFalse(responses.get(0).isViewed());
        Assert.assertFalse(responses.get(1).isViewed());
        Assert.assertTrue(responses.get(2).isViewed());
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
        Post post = transactionTemplate.execute(status -> postService.getPost(postId));
        PostRepresentation postR = transactionTemplate.execute(status -> operation.execute());

        return transactionTemplate.execute(status -> {
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
            Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
            Optional<String> applyEmailOptional = postDTO.getApplyEmail();
            boolean applyNotPatched = applyWebsiteOptional == null && applyDocumentOptional == null && applyEmailOptional == null;

            assertEquals(applyNotPatched ? post.getApplyWebsite() :
                applyWebsiteOptional == null ? null : applyWebsiteOptional.orElse(null), postR.getApplyWebsite());
            verifyDocument(applyNotPatched ? post.getApplyDocument() :
                applyDocumentOptional == null ? null : applyDocumentOptional.orElse(null), postR.getApplyDocument());
            assertEquals(applyNotPatched ? post.getApplyEmail() :
                applyEmailOptional == null ? null : applyEmailOptional.orElse(null), postR.getApplyEmail());

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

        postR = transactionTemplate.execute(status -> postApi.getPost(postId, TestHelper.mockHttpServletRequest("address")));
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

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postR.getId());
            post.setState(state);
            post.setUpdatedTimestamp(baseline.minusSeconds(seconds));
            return post;
        });

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
            transactionTemplate.execute(status -> postApi.getPosts(null, null, null, null)),
            Collections.emptyList(),
            null);

        LinkedHashMultimap<State, String> statePostNames = getPostNamesByState(postNames);
        LinkedHashMultimap<State, PostRepresentation> statePosts = getPostsByState(
            transactionTemplate.execute(status -> postApi.getPosts(true, null, null, null)));
        statePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(statePosts.get(state)),
                Lists.newArrayList(statePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(Lists.newArrayList(PUBLIC_ACTIONS.get(state)))));

        for (Long boardId : postNames.keySet()) {
            TestHelper.verifyResources(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, null, null, null, null)),
                Collections.emptyList(),
                null);

            LinkedHashMultimap<State, String> boardStatePostNames = postNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> boardStatePosts = getPostsByState(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, true, null, null, null)));
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
        LinkedHashMultimap<State, PostRepresentation> statePosts = getPostsByState(
            transactionTemplate.execute(status -> postApi.getPosts(null, null, null, null)));
        statePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(statePosts.get(state)),
                Lists.newArrayList(statePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(getAdminActions(state, adminContext))));

        LinkedHashMultimap<State, String> publicStatePostNames = getPostNamesByState(publicPostNames);
        LinkedHashMultimap<State, PostRepresentation> mergedStatePosts = getPostsByState(
            transactionTemplate.execute(status -> postApi.getPosts(true, null, null, null)));
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
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, null, null, null, null)));
            boardStatePostNames.keySet().forEach(state ->
                TestHelper.verifyResources(
                    Lists.newArrayList(boardStatePosts.get(state)),
                    Lists.newArrayList(boardStatePostNames.get(state)),
                    new TestHelper.ExpectedActions()
                        .add(getAdminActions(state, adminContext))));

            LinkedHashMultimap<State, String> publicBoardStatePostNames = publicPostNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> mergedBoardStatePosts = getPostsByState(
                transactionTemplate.execute(status -> postApi.getPostsByBoard(boardId, null, null, null, null)));
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

    private void verifyViewReferralAndResponseCounts(Long postId, Long viewCount, Long referralCount, Long responseCount) {
        Post post = transactionTemplate.execute(status -> postRepository.findOne(postId));
        TestHelper.verifyNullableCount(viewCount, post.getViewCount());
        TestHelper.verifyNullableCount(referralCount, post.getReferralCount());
        TestHelper.verifyNullableCount(responseCount, post.getResponseCount());

        List<PostRepresentation> postRs = transactionTemplate.execute(status -> postApi.getPosts(true, null, null, null));
        TestHelper.verifyNullableCount(viewCount, postRs.get(0).getViewCount());
        TestHelper.verifyNullableCount(referralCount, postRs.get(0).getReferralCount());
        TestHelper.verifyNullableCount(responseCount, postRs.get(0).getResponseCount());

        TestHelper.verifyNullableCount(0L, postRs.get(1).getViewCount());
        TestHelper.verifyNullableCount(0L, postRs.get(1).getReferralCount());
        TestHelper.verifyNullableCount(0L, postRs.get(1).getResponseCount());
    }

    private void verifyPostReferral(String referral, TestHelper.MockHttpServletResponse response, String expectedLocation) {
        transactionTemplate.execute(status -> {
            try {
                postApi.getPostReferral(referral, response);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });

        Assert.assertEquals(expectedLocation, response.getLocation());
    }

    private void verifyPostResponse(Long userId, ResourceEventRepresentation response, String documentResumeFileName, String websiteResume, String coveringNote) {
        Assert.assertEquals(userId, response.getUser().getId());
        DocumentRepresentation documentResume = response.getDocumentResume();
        Assert.assertEquals(documentResumeFileName, documentResume == null ? null : documentResume.getFileName());
        Assert.assertEquals(websiteResume, response.getWebsiteResume());
        Assert.assertEquals(coveringNote, response.getCoveringNote());
    }

    private interface PostOperation {
        PostRepresentation execute();
    }

    private enum PostAdminContext {
        ADMIN, AUTHOR
    }

    private static List<Attachments> makeTestAttachments(String urlString, String name, String label) {
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();

            Attachments attachments = new Attachments();
            attachments.setContent(Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream)));
            attachments.setType(connection.getContentType());
            attachments.setFilename(name);
            attachments.setDisposition("attachment");
            attachments.setContentId(label);
            return Collections.singletonList(attachments);
        } catch (IOException e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
