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
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.ResourceEvent;
import hr.prism.board.exception.*;
import hr.prism.board.notification.BoardAttachments;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.representation.*;
import hr.prism.board.service.TestActivityService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.ObjectUtils;
import hr.prism.board.utils.BoardUtils;
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
@SuppressWarnings("unused")
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
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        User user11 = testUserService.authenticate();
        Long departmentId1 =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department1").setSummary("department summary")).getId();

        BoardDTO boardDTO11 = TestHelper.sampleBoard().setName("board11");
        BoardRepresentation boardR11 = boardApi.postBoard(departmentId1, boardDTO11);

        Long board11Id = boardR11.getId();
        String board11PostName = boardR11.getName() + " " + State.DRAFT.name().toLowerCase() + " " + 0;
        unprivilegedUsers.put(board11Id, makeUnprivilegedUsers(boardR11.getId(), 110,
            TestHelper.samplePost()
                .setName(board11PostName)));
        unprivilegedUserPosts.put(board11Id, board11PostName);

        User user21 = testUserService.authenticate();
        Long departmentId2 =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department2").setSummary("department summary")).getId();

        BoardDTO boardDTO21 = TestHelper.smallSampleBoard().setName("board21");
        BoardRepresentation boardR21 = boardApi.postBoard(departmentId2, boardDTO21);

        Long board21Id = boardR21.getId();
        String board21PostName = boardR21.getName() + " " + State.DRAFT.name().toLowerCase() + " " + 0;
        unprivilegedUsers.put(board21Id, makeUnprivilegedUsers(boardR21.getId(), 210,
            TestHelper.smallSamplePost().setName(board21PostName).setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))));
        unprivilegedUserPosts.put(board21Id, board21PostName);

        LinkedHashMultimap<State, String> boardPostNames11 = LinkedHashMultimap.create();
        LinkedHashMultimap<State, String> boardPostNames21 = LinkedHashMultimap.create();

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
                reschedulePost(board21PostName, baseline, postCount);
                boardPostNames21.put(state, board21PostName);
                postCount++;
            }

            User postUser2 = testUserService.authenticate();
            for (int i = 1; i < 3; i++) {
                String name = boardR21.getName() + " " + state.name().toLowerCase() + " " + i;
                verifyPostPostAndSetState(postUser2, board21Id,
                    TestHelper.smallSamplePost().setName(name).setMemberCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)),
                    state, posts, baseline, postCount);
                boardPostNames21.put(state, name);
                postCount++;
            }
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> boardPostNames = new LinkedHashMap<>();
        boardPostNames.put(board11Id, boardPostNames11);
        boardPostNames.put(board21Id, boardPostNames21);

        LinkedHashMap<Long, LinkedHashMultimap<State, String>> publicPostNames = new LinkedHashMap<>();
        LinkedHashMultimap<State, String> publicPostNames11 = LinkedHashMultimap.create();
        publicPostNames11.putAll(State.ACCEPTED, Arrays.asList("board11 accepted 1", "board11 accepted 2"));
        publicPostNames.put(board11Id, publicPostNames11);

        LinkedHashMultimap<State, String> publicPostNames21 = LinkedHashMultimap.create();
        publicPostNames21.putAll(State.ACCEPTED, Arrays.asList("board21 accepted 1", "board21 accepted 2"));
        publicPostNames.put(board21Id, publicPostNames21);

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
        verifyPrivilegedPostUser(publicPostNames, user11BoardPostNames, PostAdminContext.ADMIN);

        testUserService.setAuthentication(user21.getId());
        LinkedHashMap<Long, LinkedHashMultimap<State, String>> user21BoardPostNames = new LinkedHashMap<>();
        user11BoardPostNames.put(board21Id, boardPostNames21);
        verifyPrivilegedPostUser(publicPostNames, user21BoardPostNames, PostAdminContext.ADMIN);

        for (User postUser : posts.keySet()) {
            testUserService.setAuthentication(postUser.getId());
            verifyPrivilegedPostUser(publicPostNames, posts.get(postUser), PostAdminContext.AUTHOR);
        }
    }

    @Test
    public void shouldNotAcceptPostWithMissingApply() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.sampleBoard()).getId();

        PostDTO postDTO =
            new PostDTO()
                .setName("post")
                .setSummary("summary")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_APPLY);
    }

    @Test
    public void shouldNotAcceptPostWithCorruptedApply() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.sampleBoard()).getId();

        PostDTO postDTO =
            new PostDTO()
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
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_APPLY);
    }

    @Test
    public void shouldNotAcceptPostWithMissingRelationForUserWithoutAuthorRole() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.sampleBoard()).getId();
        testUserService.authenticate();

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
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION);
    }

    @Test
    public void shouldNotAcceptPostWithCategoriesForBoardWithoutCategories() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.smallSampleBoard()).getId();

        PostDTO postDTO0 = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO0), ExceptionCode.CORRUPTED_POST_POST_CATEGORIES);

        departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.empty()));
        PostDTO postDTO1 = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO1), ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES);
    }

    @Test
    public void shouldNotAcceptPostWithoutCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.sampleBoard()).getId();

        PostDTO postDTO0 = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO0), ExceptionCode.MISSING_POST_POST_CATEGORIES);

        PostDTO postDTO1 = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO1), ExceptionCode.MISSING_POST_MEMBER_CATEGORIES);
    }

    @Test
    public void shouldNotAcceptPostWithInvalidCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.sampleBoard()).getId();

        PostDTO postDTO0 = TestHelper.samplePost().setPostCategories(Collections.singletonList("p4"));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO0), ExceptionCode.INVALID_POST_POST_CATEGORIES);

        departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.of(Collections.singletonList(MemberCategory.MASTER_STUDENT))));
        PostDTO postDTO1 = TestHelper.samplePost().setMemberCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT));
        ExceptionUtils.verifyException(BoardException.class, () -> postApi.postPost(boardId, postDTO1), ExceptionCode.INVALID_POST_MEMBER_CATEGORIES);
    }

    @Test
    public void shouldNotCorruptPostByPatching() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        BoardRepresentation boardRepresentation = boardApi.postBoard(departmentId, TestHelper.sampleBoard());
        Long boardId = boardRepresentation.getId();
        Long postId = postApi.postPost(boardId, TestHelper.samplePost()).getId();

        ExceptionUtils.verifyException(BoardException.class, () ->
                postApi.patchPost(postId, new PostPatchDTO()
                    .setPostCategories(Optional.empty())),
            ExceptionCode.MISSING_POST_POST_CATEGORIES);

        ExceptionUtils.verifyException(BoardException.class, () ->
                postApi.patchPost(postId, new PostPatchDTO()
                    .setMemberCategories(Optional.empty())),
            ExceptionCode.MISSING_POST_MEMBER_CATEGORIES);

        ExceptionUtils.verifyException(BoardException.class, () ->
                postApi.patchPost(postId, new PostPatchDTO()
                    .setPostCategories(Optional.of(Collections.singletonList("p4")))),
            ExceptionCode.INVALID_POST_POST_CATEGORIES);

        departmentApi.patchDepartment(departmentId,
            new DepartmentPatchDTO().setMemberCategories(Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))));
        ExceptionUtils.verifyException(BoardException.class, () ->
                postApi.patchPost(postId, new PostPatchDTO()
                    .setMemberCategories(Optional.of(Collections.singletonList(MemberCategory.RESEARCH_STUDENT)))),
            ExceptionCode.INVALID_POST_MEMBER_CATEGORIES);

        departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.empty()));
        boardApi.patchBoard(boardId, new BoardPatchDTO().setPostCategories(Optional.empty()));

        ExceptionUtils.verifyException(BoardException.class, () ->
                postApi.patchPost(postId, new PostPatchDTO()
                    .setPostCategories(Optional.of(Collections.singletonList("p1")))),
            ExceptionCode.CORRUPTED_POST_POST_CATEGORIES);

        ExceptionUtils.verifyException(BoardException.class, () ->
                postApi.patchPost(postId, new PostPatchDTO()
                    .setMemberCategories(Optional.of(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)))),
            ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES);
    }

    @Test
    public void shouldSupportPostActionsAndPermissions() {
        // Create departmentResource and board
        User departmentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("departmentResource summary")).getId();
        BoardRepresentation boardR = boardApi.postBoard(departmentId, TestHelper.sampleBoard());
        Long boardId = boardR.getId();

        // Allow departmentResource to have research students
        departmentApi.patchDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(
            Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT, MemberCategory.RESEARCH_STUDENT))));

        User boardUser = testUserService.authenticate();
        Board board = boardService.getBoard(boardId);
        userRoleService.createOrUpdateUserRole(board, boardUser, Role.ADMINISTRATOR);

        List<User> adminUsers = Arrays.asList(departmentUser, boardUser);

        // Create post
        testActivityService.record();
        testNotificationService.record();

        Long departmentUserId = departmentUser.getId();
        listenForActivities(departmentUserId);

        Long boardUserId = boardUser.getId();
        listenForActivities(boardUserId);

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

        testActivityService.verify(departmentUserId, new TestActivityService.ActivityInstance(postId, Activity.NEW_POST_PARENT_ACTIVITY));
        testActivityService.verify(boardUserId, new TestActivityService.ActivityInstance(postId, Activity.NEW_POST_PARENT_ACTIVITY));

        Resource departmentResource = resourceService.findOne(departmentId);
        Resource boardResource = resourceService.findOne(boardId);
        Resource postResource = resourceService.findOne(postId);
        String departmentAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(departmentResource, departmentUser, Role.ADMINISTRATOR).getUuid();
        String boardAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(boardResource, boardUser, Role.ADMINISTRATOR).getUuid();
        String postAdminRoleUuid = userRoleService.findByResourceAndUserAndRole(postResource, postUser, Role.ADMINISTRATOR).getUuid();

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_PARENT_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", departmentUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_PARENT_NOTIFICATION, boardUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", boardUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", boardAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.NEW_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()));

        testActivityService.stop();
        testNotificationService.stop();

        // Create unprivileged users
        Collection<User> unprivilegedUsers = makeUnprivilegedUsers(boardId, 2, TestHelper.samplePost()).values();

        testActivityService.record();
        testNotificationService.record();

        // Clear activity streams for the admin users
        for (Long userId : new Long[]{departmentUserId, boardUserId}) {
            testUserService.setAuthentication(userId);
            List<Long> activityIds =
                activityService.getActivities(userId).stream().map(ActivityRepresentation::getId).collect(Collectors.toList());
            Assert.assertEquals(2, activityIds.size());
            for (Long activityId : activityIds) {
                userApi.dismissActivity(activityId);
            }

            listenForActivities(userId);
            testActivityService.verify(userId);
        }

        Long postUserId = postUser.getId();
        listenForActivities(postUserId);

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
        postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));

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

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.SUSPEND_POST_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.SUSPEND_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", postUserGivenName)
                .put("department", departmentName).put("board", boardName)
                .put("post", postName)
                .put("comment", "could you please explain what you will pay the successful applicant")
                .put("resourceRedirect", resourceRedirect)
                .put("invitationUuid", postAdminRoleUuid)
                .build()));

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

        testActivityService.verify(departmentUserId, new TestActivityService.ActivityInstance(postId, Activity.CORRECT_POST_ACTIVITY));
        testActivityService.verify(boardUserId, new TestActivityService.ActivityInstance(postId, Activity.CORRECT_POST_ACTIVITY));
        testActivityService.verify(postUserId);

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.CORRECT_POST_NOTIFICATION, departmentUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", departmentUserGivenName)
                    .put("post", postName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.CORRECT_POST_NOTIFICATION, boardUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", boardUserGivenName)
                    .put("post", postName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", boardAdminRoleUuid)
                    .build()));

        // Check that the administrator can accept post in the suspended state
        PostPatchDTO acceptDTO = new PostPatchDTO()
            .setLiveTimestamp(Optional.empty())
            .setDeadTimestamp(Optional.empty())
            .setComment("accepting without time constraints");

        verifyPatchPost(boardUser, postId, acceptDTO, () -> postApi.executeAction(postId, "accept", acceptDTO), State.PENDING);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.ACCEPT_POST_ACTIVITY));

        postService.publishAndRetirePosts(LocalDateTime.now());
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_ACTIVITY));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.ACCEPT_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("publicationSchedule", "imminently. We will send you a follow-up message when your post has gone live")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()));

        // Suspend the post so that it can be accepted again
        verifyPatchPost(boardUser, postId, new PostPatchDTO(),
            () -> postApi.executeAction(postId, "suspend", new PostPatchDTO().setComment("comment")), State.SUSPENDED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.SUSPENDED, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.SUSPEND_POST_ACTIVITY));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.SUSPEND_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("comment", "comment")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()));

        // Check that the administrator can make further changes and accept the post again
        PostPatchDTO acceptPendingDTO = new PostPatchDTO()
            .setApplyWebsite(Optional.of("http://www.twitter.com"))
            .setPostCategories(Optional.of(Arrays.asList("p1", "p2")))
            .setLiveTimestamp(Optional.of(liveTimestampDelayed))
            .setDeadTimestamp(Optional.of(deadTimestampDelayed))
            .setComment("this looks good now - i replaced the document with the complete website for the opportunity");

        verifyPatchPost(boardUser, postId, acceptPendingDTO, () -> postApi.executeAction(postId, "accept", acceptPendingDTO), State.PENDING);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.PENDING, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.ACCEPT_POST_ACTIVITY));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.ACCEPT_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("publicationSchedule",
                        "on or around " + postR.getLiveTimestamp().format(BoardUtils.DATETIME_FORMATTER) + ". We will send you a follow-up message when your post has gone live")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()));

        // Check that the post stays in pending state when the update job runs
        verifyPublishAndRetirePost(postId, State.PENDING);
        Post localPost0 = postService.getPost(postId);
        localPost0.setLiveTimestamp(liveTimestamp);
        localPost0.setDeadTimestamp(deadTimestamp);
        resourceRepository.updateSilently(localPost0);

        // Should be notified
        testUserService.setAuthentication(departmentUserId);
        Long departmentMember1Id =
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new UserRoleDTO().setUser(
                    new UserDTO()
                        .setGivenName("student1")
                        .setSurname("student1")
                        .setEmail("student1@student1.com"))
                    .setRole(Role.MEMBER)
                    .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT)
                    .setExpiryDate(LocalDate.now().plusDays(1))).getUser().getId();

        // Should be notified
        Long departmentMember2Id =
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new UserRoleDTO().setUser(
                    new UserDTO()
                        .setGivenName("student2")
                        .setSurname("student2")
                        .setEmail("student2@student2.com"))
                    .setRole(Role.MEMBER)
                    .setMemberCategory(MemberCategory.MASTER_STUDENT)).getUser().getId();

        // Should not be notified - suppressed
        Long departmentMember3Id =
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new UserRoleDTO().setUser(
                    new UserDTO()
                        .setGivenName("student3")
                        .setSurname("student3")
                        .setEmail("student3@student3.com"))
                    .setRole(Role.MEMBER)
                    .setMemberCategory(MemberCategory.MASTER_STUDENT)).getUser().getId();

        testUserService.setAuthentication(departmentMember3Id);
        userApi.postSuppressions();

        // Should not be notified
        testUserService.setAuthentication(departmentUserId);
        Long departmentMember4Id =
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new UserRoleDTO().setUser(
                    new UserDTO()
                        .setGivenName("student4")
                        .setSurname("student4")
                        .setEmail("student4@student4.com"))
                    .setRole(Role.MEMBER)
                    .setMemberCategory(MemberCategory.RESEARCH_STUDENT)
                    .setExpiryDate(LocalDate.now().plusDays(1))).getUser().getId();

        // Should not be notified
        Long departmentMember5Id =
            resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
                new UserRoleDTO().setUser(
                    new UserDTO()
                        .setGivenName("student5")
                        .setSurname("student5")
                        .setEmail("student5@student5.com"))
                    .setRole(Role.MEMBER)
                    .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT)
                    .setExpiryDate(LocalDate.now().minusDays(1))).getUser().getId();

        listenForActivities(departmentMember1Id);
        listenForActivities(departmentMember2Id);
        listenForActivities(departmentMember3Id);
        listenForActivities(departmentMember4Id);
        listenForActivities(departmentMember5Id);

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        User departmentMember1 = userCacheService.findOne(departmentMember1Id);
        User departmentMember2 = userCacheService.findOne(departmentMember2Id);

        String departmentMember1Uuid = departmentMember1.getUuid();
        String departmentMember2Uuid = departmentMember2.getUuid();
        String parentRedirect = serverUrl + "/redirect?resource=" + boardId;

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_ACTIVITY));
        testActivityService.verify(departmentMember1Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember2Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember3Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember4Id);
        testActivityService.verify(departmentMember5Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));

        UserRole departmentMemberRole1 = userRoleService.findByResourceAndUserAndRole(departmentResource, departmentMember1, Role.MEMBER);
        UserRole departmentMemberRole2 = userRoleService.findByResourceAndUserAndRole(departmentResource, departmentMember2, Role.MEMBER);

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember1,
                ImmutableMap.<String, String>builder()
                    .put("recipient", "student1")
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("organization", "organization name")
                    .put("summary", "summary 2")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentMemberRole1.getUuid())
                    .put("parentRedirect", parentRedirect)
                    .put("recipientUuid", departmentMember1Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember2,
                ImmutableMap.<String, String>builder()
                    .put("recipient", "student2")
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("organization", "organization name")
                    .put("summary", "summary 2")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentMemberRole2.getUuid())
                    .put("parentRedirect", parentRedirect)
                    .put("recipientUuid", departmentMember2Uuid)
                    .build()));

        // Check that the administrator can reject the post
        PostPatchDTO rejectDTO = new PostPatchDTO()
            .setComment("we have received a complaint, we're closing down the post");

        verifyPatchPost(departmentUser, postId, rejectDTO, () -> postApi.executeAction(postId, "reject", rejectDTO), State.REJECTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.REJECTED, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.REJECT_POST_ACTIVITY));
        testActivityService.verify(departmentMember1Id);
        testActivityService.verify(departmentMember2Id);
        testActivityService.verify(departmentMember3Id);
        testActivityService.verify(departmentMember4Id);
        testActivityService.verify(departmentMember5Id);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.REJECT_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", postUserGivenName)
                .put("department", departmentName)
                .put("board", boardName)
                .put("post", postName)
                .put("comment", "we have received a complaint, we're closing down the post")
                .put("homeRedirect", serverUrl + "/redirect")
                .put("invitationUuid", postAdminRoleUuid)
                .build()));

        // Check that the administrator can restore the post
        PostPatchDTO restoreFromRejectedDTO = new PostPatchDTO()
            .setComment("sorry we made a mistake, we're restoring the post");

        verifyPatchPost(boardUser, postId, restoreFromRejectedDTO, () -> postApi.executeAction(postId, "restore", restoreFromRejectedDTO), State.PENDING);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.RESTORE_POST_ACTIVITY));
        postService.publishAndRetirePosts(LocalDateTime.now());
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_ACTIVITY));
        testActivityService.verify(departmentMember1Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember2Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember3Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember4Id);
        testActivityService.verify(departmentMember5Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESTORE_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember1,
                ImmutableMap.<String, String>builder()
                    .put("recipient", "student1")
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("organization", "organization name")
                    .put("summary", "summary 2")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentMemberRole1.getUuid())
                    .put("parentRedirect", parentRedirect)
                    .put("recipientUuid", departmentMember1Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember2,
                ImmutableMap.<String, String>builder()
                    .put("recipient", "student2")
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("organization", "organization name")
                    .put("summary", "summary 2").put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentMemberRole2.getUuid()).put("parentRedirect", parentRedirect)
                    .put("recipientUuid", departmentMember2Uuid)
                    .build()));

        Post localPost1 = postService.getPost(postId);
        localPost1.setDeadTimestamp(liveTimestamp.minusSeconds(1));
        resourceRepository.updateSilently(localPost1);

        // Check that the post now moves to the expired state when the update job runs
        verifyPublishAndRetirePost(postId, State.EXPIRED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.EXPIRED, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.RETIRE_POST_ACTIVITY));
        testActivityService.verify(departmentMember1Id);
        testActivityService.verify(departmentMember2Id);
        testActivityService.verify(departmentMember3Id);
        testActivityService.verify(departmentMember4Id);
        testActivityService.verify(departmentMember5Id);

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RETIRE_POST_NOTIFICATION, postUser,
            ImmutableMap.<String, String>builder()
                .put("recipient", postUserGivenName)
                .put("department", departmentName)
                .put("board", boardName)
                .put("post", postName)
                .put("resourceRedirect", resourceRedirect)
                .put("invitationUuid", postAdminRoleUuid)
                .build()));

        // Check that the author can withdraw the post
        PostPatchDTO withdrawDTO = new PostPatchDTO();
        verifyPatchPost(postUser, postId, withdrawDTO, () -> postApi.executeAction(postId, "withdraw", withdrawDTO), State.WITHDRAWN);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.WITHDRAWN, operations);

        // Check that the author can restore the post
        PostPatchDTO restoreFromWithdrawnDTO = new PostPatchDTO();

        verifyPatchPost(postUser, postId, restoreFromWithdrawnDTO, () -> postApi.executeAction(postId, "restore", restoreFromWithdrawnDTO), State.EXPIRED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.EXPIRED, operations);

        Post localPost2 = postService.getPost(postId);
        localPost2.setDeadTimestamp(null);
        resourceRepository.updateSilently(localPost2);

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActions(adminUsers, postUser, unprivilegedUsers, postId, State.ACCEPTED, operations);

        testActivityService.verify(departmentUserId);
        testActivityService.verify(boardUserId);
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_ACTIVITY));
        testActivityService.verify(departmentMember1Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember2Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember3Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));
        testActivityService.verify(departmentMember4Id);
        testActivityService.verify(departmentMember5Id, new TestActivityService.ActivityInstance(postId, Activity.PUBLISH_POST_MEMBER_ACTIVITY));

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, postUser,
                ImmutableMap.<String, String>builder()
                    .put("recipient", postUserGivenName)
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", postAdminRoleUuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember1,
                ImmutableMap.<String, String>builder()
                    .put("recipient", "student1")
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("organization", "organization name")
                    .put("summary", "summary 2")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentMemberRole1.getUuid())
                    .put("parentRedirect", parentRedirect)
                    .put("recipientUuid", departmentMember1Uuid)
                    .build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, departmentMember2,
                ImmutableMap.<String, String>builder()
                    .put("recipient", "student2")
                    .put("department", departmentName)
                    .put("board", boardName)
                    .put("post", postName)
                    .put("organization", "organization name")
                    .put("summary", "summary 2")
                    .put("resourceRedirect", resourceRedirect)
                    .put("invitationUuid", departmentMemberRole2.getUuid())
                    .put("parentRedirect", parentRedirect)
                    .put("recipientUuid", departmentMember2Uuid)
                    .build()));
        testActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(postUser.getId());
        List<ResourceOperationRepresentation> resourceOperationRs = postApi.getPostOperations(postId);
        Assert.assertEquals(20, resourceOperationRs.size());

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

        TestHelper.verifyResourceOperation(resourceOperationRs.get(8), Action.PUBLISH);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(9), Action.SUSPEND, boardUser, "comment");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(10), Action.EDIT, boardUser,
            new ChangeListRepresentation()
                .put("applyWebsite", null, "http://www.twitter.com")
                .put("applyDocument", ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"), null)
                .put("postCategories", Arrays.asList("p2", "p1"), Arrays.asList("p1", "p2"))
                .put("liveTimestamp", null, TestHelper.toString(liveTimestampDelayed))
                .put("deadTimestamp", null, TestHelper.toString(deadTimestampDelayed)));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(11), Action.ACCEPT, boardUser,
            "this looks good now - i replaced the document with the complete website for the opportunity");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(12), Action.PUBLISH);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(13), Action.REJECT, departmentUser,
            "we have received a complaint, we're closing down the post");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(14), Action.RESTORE, boardUser,
            "sorry we made a mistake, we're restoring the post");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(15), Action.PUBLISH);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(16), Action.RETIRE);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(17), Action.WITHDRAW, postUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(18), Action.RESTORE, postUser);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(19), Action.PUBLISH);
    }

    @Test
    @Sql("classpath:data/organization_autosuggest_setup.sql")
    public void shouldSuggestOrganizations() {
        List<OrganizationRepresentation> organizations = postApi.lookupOrganizations("Computer");
        Assert.assertEquals(3, organizations.size());

        Assert.assertEquals("Computer Science Department", organizations.get(0).getName());
        Assert.assertEquals("Department of Computer Science", organizations.get(1).getName());
        Assert.assertEquals("Laboratory for the Foundations of Computer Science", organizations.get(2).getName());

        organizations = postApi.lookupOrganizations("Computer Science Laboratory");
        Assert.assertEquals(3, organizations.size());

        Assert.assertEquals("Laboratory for the Foundations of Computer Science", organizations.get(0).getName());
        Assert.assertEquals("Computer Science Department", organizations.get(1).getName());
        Assert.assertEquals("Department of Computer Science", organizations.get(2).getName());

        organizations = postApi.lookupOrganizations("School of Informatics");
        Assert.assertEquals(1, organizations.size());

        Assert.assertEquals("School of Informatics", organizations.get(0).getName());

        organizations = postApi.lookupOrganizations("Physics");
        Assert.assertEquals(1, organizations.size());

        Assert.assertEquals("Physics Department", organizations.get(0).getName());

        organizations = postApi.lookupOrganizations("Mathematics");
        Assert.assertEquals(0, organizations.size());
    }

    @Test
    public void shouldCountPostViewsReferralsAndResponses() throws IOException {
        Long boardUserId = testUserService.authenticate().getId();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        BoardRepresentation boardR = boardApi.postBoard(departmentId, TestHelper.smallSampleBoard());
        Long boardId = boardR.getId();

        postApi.postPost(boardId,
            TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)));
        Long postId = postApi.postPost(boardId,
            TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))).getId();
        postService.publishAndRetirePosts(LocalDateTime.now());

        Long memberUser1 = testUserService.authenticate().getId();
        Long memberUser2 = testUserService.authenticate().getId();

        testUserService.setAuthentication(boardUserId);
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO().setUser(new UserDTO().setId(memberUser1)).setRole(Role.MEMBER));
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO().setUser(new UserDTO().setId(memberUser2)).setRole(Role.MEMBER));

        testUserService.setAuthentication(memberUser1);
        departmentApi.putMembershipUpdate(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2015));
        PostRepresentation viewPostMemberUser1 = postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser1"));
        verifyViewReferralAndResponseCounts(postId, 1L, 0L, 0L);
        String referral1 = viewPostMemberUser1.getReferral().getReferral();
        Assert.assertNotNull(referral1);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser1"));
        verifyViewReferralAndResponseCounts(postId, 1L, 0L, 0L);

        testUserService.setAuthentication(memberUser2);
        departmentApi.putMembershipUpdate(departmentId,
            new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.THIRTY_THIRTYNINE).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2015));
        PostRepresentation viewPostMemberUser2 = postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser2"));
        verifyViewReferralAndResponseCounts(postId, 2L, 0L, 0L);
        String referral2 = viewPostMemberUser2.getReferral().getReferral();
        Assert.assertNotNull(referral2);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("memberUser2"));
        verifyViewReferralAndResponseCounts(postId, 2L, 0L, 0L);

        testUserService.unauthenticate();
        postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown1"));
        verifyViewReferralAndResponseCounts(postId, 3L, 0L, 0L);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown1"));
        verifyViewReferralAndResponseCounts(postId, 3L, 0L, 0L);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown2"));
        verifyViewReferralAndResponseCounts(postId, 4L, 0L, 0L);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown2", "proxy2"));
        verifyViewReferralAndResponseCounts(postId, 4L, 0L, 0L);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown3", "proxy3"));
        verifyViewReferralAndResponseCounts(postId, 5L, 0L, 0L);

        postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown3", "proxy3"));
        verifyViewReferralAndResponseCounts(postId, 5L, 0L, 0L);

        PostRepresentation viewPostUnknown1 = postApi.getPost(postId, TestHelper.mockHttpServletRequest("unknown1"));
        Assert.assertNull(viewPostUnknown1.getReferral());

        TestHelper.MockHttpServletResponse response = TestHelper.mockHttpServletResponse();
        verifyPostReferral(referral1, response, "http://www.google.co.uk");
        verifyViewReferralAndResponseCounts(postId, 5L, 1L, 0L);

        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> verifyPostReferral(referral1, response, "http://www.google.co.uk"), ExceptionCode.FORBIDDEN_REFERRAL);

        testUserService.setAuthentication(boardUserId);
        DocumentDTO documentDTO = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/board-prism-hr/image/upload/v1506846526/test/attachment.pdf").setFileName("attachments1.pdf");
        postApi.patchPost(postId, new PostPatchDTO().setApplyDocument(Optional.of(documentDTO)).setApplyEmail(Optional.empty()));

        testUserService.setAuthentication(memberUser2);
        verifyPostReferral(referral2, response, "http://res.cloudinary.com/board-prism-hr/image/upload/v1506846526/test/attachment.pdf");
        verifyViewReferralAndResponseCounts(postId, 5L, 2L, 0L);

        ExceptionUtils.verifyException(BoardException.class,
            () -> postApi.postPostResponse(postId, new ResourceEventDTO()), ExceptionCode.INVALID_RESOURCE_EVENT);

        testUserService.setAuthentication(boardUserId);
        postApi.patchPost(postId, new PostPatchDTO().setApplyDocument(Optional.empty()).setApplyEmail(Optional.of("email@email.com")));

        testUserService.setAuthentication(memberUser1);
        ResourceEventDTO resourceEventDTO = new ResourceEventDTO().setDocumentResume(documentDTO).setWebsiteResume("website").setCoveringNote("note");
        postApi.postPostResponse(postId, resourceEventDTO);
        verifyViewReferralAndResponseCounts(postId, 5L, 2L, 1L);

        ExceptionUtils.verifyException(BoardDuplicateException.class, () ->
            postApi.postPostResponse(postId, resourceEventDTO), ExceptionCode.DUPLICATE_RESOURCE_EVENT);

        testUserService.setAuthentication(memberUser2);
        postApi.postPostResponse(postId, resourceEventDTO);
        verifyViewReferralAndResponseCounts(postId, 5L, 2L, 2L);
    }

    @Test
    public void shouldNotifyAndListPostResponses() throws IOException {
        Long boardUserId = testUserService.authenticate().getId();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        BoardRepresentation boardR = boardApi.postBoard(departmentId, TestHelper.smallSampleBoard());
        Long boardId = boardR.getId();

        User postUser = testUserService.authenticate();
        Long postUserId = postUser.getId();
        String postUserEmail = postUser.getEmail();
        Long postId = postApi.postPost(boardId, TestHelper.smallSamplePost()
            .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)).setApplyWebsite(null).setApplyEmail(postUserEmail)).getId();

        User memberUser1 = testUserService.authenticate();
        Long memberUser1Id = memberUser1.getId();

        User memberUser2 = testUserService.authenticate();
        Long memberUser2Id = memberUser2.getId();

        User memberUser3 = testUserService.authenticate();
        Long memberUser3Id = memberUser3.getId();

        testUserService.setAuthentication(boardUserId);
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO().setUser(new UserDTO().setId(memberUser1Id)).setRole(Role.MEMBER));
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO().setUser(new UserDTO().setId(memberUser2Id)).setRole(Role.MEMBER));
        resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO().setUser(new UserDTO().setId(memberUser3Id)).setRole(Role.MEMBER));
        postApi.executeAction(postId, "accept", new PostPatchDTO());
        postService.publishAndRetirePosts(LocalDateTime.now());

        testUserService.setAuthentication(postUserId);
        List<ActivityRepresentation> activities = activityService.getActivities(postUserId);
        activities.forEach(activity -> userApi.dismissActivity(activity.getId()));

        testActivityService.record();
        testNotificationService.record();
        listenForActivities(postUserId);

        testUserService.setAuthentication(memberUser1Id);
        departmentApi.putMembershipUpdate(departmentId, new UserRoleDTO().setUser(
            new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.NINETEEN_TWENTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
            .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2010));
        DocumentDTO documentDTO1 = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/board-prism-hr/image/upload/v1506846526/test/attachment.pdf").setFileName("attachments1.pdf");
        Long responseId = postApi.postPostResponse(postId,
            new ResourceEventDTO().setDocumentResume(documentDTO1).setWebsiteResume("website1").setCoveringNote("note1")).getId();

        User postEmailUser = new User();
        postEmailUser.setGivenName("Author");
        postEmailUser.setSurname("Author");
        postEmailUser.setEmail(postUserEmail);

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESPOND_POST_NOTIFICATION, postEmailUser,
                ImmutableMap.<String, String>builder().put("recipient", "Author").put("post", "post").put("candidate", memberUser1.getFullName())
                    .put("coveringNote", "note1").put("profile", "website1").build(),
                makeTestAttachments("attachments1.pdf")));
        testActivityService.verify(postUserId, new TestActivityService.ActivityInstance(postId, memberUser1Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY));

        testUserService.setAuthentication(postUserId);
        List<ResourceEventRepresentation> responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(memberUser1Id, responses.get(0).getUser().getId());

        testUserService.setAuthentication(boardUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(memberUser1Id, responses.get(0).getUser().getId());
        postApi.patchPost(postId, new PostPatchDTO().setApplyEmail(Optional.of("other@other.com")));

        testUserService.setAuthentication(memberUser2Id);
        departmentApi.putMembershipUpdate(departmentId, new UserRoleDTO().setUser(
            new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.NINETEEN_TWENTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
            .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2010));
        DocumentDTO documentDTO2 = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/board-prism-hr/image/upload/v1506846526/test/attachment.pdf").setFileName("attachments2.pdf");
        postApi.postPostResponse(postId,
            new ResourceEventDTO().setDocumentResume(documentDTO2).setWebsiteResume("website2").setCoveringNote("note2"));

        postEmailUser.setEmail("other@other.com");
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESPOND_POST_NOTIFICATION, postEmailUser,
                ImmutableMap.<String, String>builder().put("recipient", "Author").put("post", "post").put("candidate", memberUser2.getFullName())
                    .put("coveringNote", "note2").put("profile", "website2").build(),
                makeTestAttachments("attachments2.pdf")));
        testActivityService.verify(postUserId,
            new TestActivityService.ActivityInstance(postId, memberUser2Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY),
            new TestActivityService.ActivityInstance(postId, memberUser1Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY));

        testUserService.setAuthentication(postUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(2, responses.size());
        Assert.assertEquals(memberUser2Id, responses.get(0).getUser().getId());
        Assert.assertEquals(memberUser1Id, responses.get(1).getUser().getId());

        testUserService.setAuthentication(boardUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(2, responses.size());
        Assert.assertEquals(memberUser2Id, responses.get(0).getUser().getId());
        Assert.assertEquals(memberUser1Id, responses.get(1).getUser().getId());
        postApi.patchPost(postId, new PostPatchDTO().setApplyEmail(Optional.of(postUserEmail)));

        testUserService.setAuthentication(postUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(2, responses.size());
        Assert.assertEquals(memberUser2Id, responses.get(0).getUser().getId());
        Assert.assertEquals(memberUser1Id, responses.get(1).getUser().getId());

        testUserService.setAuthentication(boardUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(2, responses.size());
        Assert.assertEquals(memberUser2Id, responses.get(0).getUser().getId());
        Assert.assertEquals(memberUser1Id, responses.get(1).getUser().getId());
        postApi.patchPost(postId, new PostPatchDTO().setApplyEmail(Optional.of("other@other.com")));

        testUserService.setAuthentication(memberUser3Id);
        departmentApi.putMembershipUpdate(departmentId, new UserRoleDTO().setUser(
            new UserDTO().setGender(Gender.MALE).setAgeRange(AgeRange.NINETEEN_TWENTYFOUR).setLocationNationality(
                new LocationDTO().setName("London, United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
            .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT).setMemberProgram("program").setMemberYear(2010));
        DocumentDTO documentDTO3 = new DocumentDTO().setCloudinaryId("v1504040061")
            .setCloudinaryUrl("http://res.cloudinary.com/board-prism-hr/image/upload/v1506846526/test/attachment.pdf").setFileName("attachments3.pdf");
        postApi.postPostResponse(postId,
            new ResourceEventDTO().setDocumentResume(documentDTO3).setWebsiteResume("website3").setCoveringNote("note3"));

        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.RESPOND_POST_NOTIFICATION, postEmailUser,
                ImmutableMap.<String, String>builder().put("recipient", "Author").put("post", "post").put("candidate", memberUser3.getFullName())
                    .put("coveringNote", "note3").put("profile", "website3").build(),
                makeTestAttachments("attachments3.pdf")));
        testActivityService.verify(postUserId,
            new TestActivityService.ActivityInstance(postId, memberUser3Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY),
            new TestActivityService.ActivityInstance(postId, memberUser2Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY),
            new TestActivityService.ActivityInstance(postId, memberUser1Id, ResourceEvent.RESPONSE, Activity.RESPOND_POST_ACTIVITY));

        testActivityService.stop();
        testNotificationService.stop();

        testUserService.setAuthentication(postUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(3, responses.size());
        Assert.assertEquals(memberUser3Id, responses.get(0).getUser().getId());
        Assert.assertEquals(memberUser2Id, responses.get(1).getUser().getId());
        Assert.assertEquals(memberUser1Id, responses.get(2).getUser().getId());

        testUserService.setAuthentication(boardUserId);
        responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(3, responses.size());
        Assert.assertEquals(memberUser3Id, responses.get(0).getUser().getId());
        Assert.assertEquals(memberUser2Id, responses.get(1).getUser().getId());
        Assert.assertEquals(memberUser1Id, responses.get(2).getUser().getId());

        testUserService.setAuthentication(postUserId);
        postApi.putPostResponseView(postId, responseId);

        ResourceEventRepresentation response1 = postApi.getPostResponse(postId, responseId);
        Assert.assertEquals(memberUser1Id, response1.getUser().getId());

        responses = postApi.getPostResponses(postId, null);
        Assert.assertFalse(responses.get(0).isViewed());
        Assert.assertFalse(responses.get(1).isViewed());
        Assert.assertTrue(responses.get(2).isViewed());

        testUserService.setAuthentication(boardUserId);
        postApi.getPostResponses(postId, null).forEach(response -> Assert.assertFalse(response.isViewed()));
        postApi.putPostResponseView(postId, responseId);

        response1 = postApi.getPostResponse(postId, responseId);
        Assert.assertEquals(memberUser1Id, response1.getUser().getId());

        responses = postApi.getPostResponses(postId, null);
        Assert.assertFalse(responses.get(0).isViewed());
        Assert.assertFalse(responses.get(1).isViewed());
        Assert.assertTrue(responses.get(2).isViewed());
    }

    @Test
    @Sql("classpath:data/post_response_filter_setup.sql")
    public void shouldListAndFilterPostResponses() {
        resourceEventRepository.findAll().forEach(resourceEvent -> {
            resourceEventService.setIndexData(resourceEvent);
            resourceEventRepository.update(resourceEvent);
        });

        Long userId = userRepository.findByEmail("alastair@knowles.com").getId();
        Long postId = resourceRepository.findByHandle("cs/opportunities/4").getId();
        testUserService.setAuthentication(userId);

        List<ResourceEventRepresentation> responses = postApi.getPostResponses(postId, null);
        Assert.assertEquals(2, responses.size());
        verifyContains(responses.stream().map(response -> response.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("jakub@fibinger.com"), BoardUtils.obfuscateEmail("juan@mingo.com"));

        responses = postApi.getPostResponses(postId, "madrid");
        Assert.assertEquals(1, responses.size());
        verifyContains(responses.stream().map(response -> response.getUser().getEmail()).collect(Collectors.toList()), BoardUtils.obfuscateEmail("juan@mingo.com"));
    }

    @Test
    public void shouldArchivePosts() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId =
            departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.smallSampleBoard()).getId();
        Long postId1 = postApi.postPost(boardId,
            TestHelper.smallSamplePost().setName("post1").setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))).getId();

        testUserService.authenticate();
        Long postId2 = postApi.postPost(boardId,
            TestHelper.smallSamplePost().setName("post2").setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))).getId();
        Long postId3 = postApi.postPost(boardId,
            TestHelper.smallSamplePost().setName("post3").setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))).getId();

        Post post2 = (Post) resourceRepository.findOne(postId2);
        Post post3 = (Post) resourceRepository.findOne(postId3);
        resourceRepository.updateUpdatedTimestampById(postId2,
            post2.getUpdatedTimestamp().minusSeconds(resourceArchiveDurationSeconds + 1));
        resourceRepository.updateUpdatedTimestampById(postId3,
            post3.getUpdatedTimestamp().minusSeconds(resourceArchiveDurationSeconds + 1));

        resourceService.archiveResources();
        Assert.assertEquals(State.PENDING, resourceRepository.findOne(postId1).getState());
        Assert.assertEquals(State.ARCHIVED, resourceRepository.findOne(postId2).getState());
        Assert.assertEquals(State.ARCHIVED, resourceRepository.findOne(postId3).getState());
    }

    private PostRepresentation verifyPostPost(Long boardId, PostDTO postDTO) {
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
        University university = universityService.getUniversity(postR.getBoard().getDepartment().getUniversity().getId());

        List<ResourceRelation> parents = resourceRelationRepository.findByResource2(post);
        assertThat(parents.stream().map(ResourceRelation::getResource1).collect(Collectors.toList()),
            Matchers.containsInAnyOrder(university, department, board, post));
        return postR;
    }

    private PostRepresentation verifyPatchPost(User user, Long postId, PostPatchDTO postDTO, PostOperation operation, State expectedState) {
        testUserService.setAuthentication(user.getId());
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
    }

    private void verifyPublishAndRetirePost(Long postId, State expectedState) {// Check that the scheduler does not create duplicate operations
        for (int i = 0; i < 2; i++) {
            postService.publishAndRetirePosts(LocalDateTime.now());
        }

        PostRepresentation postR = postApi.getPost(postId, TestHelper.mockHttpServletRequest("address"));
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

        Post post = postService.getPost(postR.getId());
        post.setState(state);
        post.setUpdatedTimestamp(baseline.minusSeconds(seconds));
        resourceRepository.updateSilently(post);

        userStatePosts.put(state, postDTO.getName());
    }

    private void reschedulePost(String postName, LocalDateTime baseline, int seconds) {
        Post post = postService.getByName(postName).get(0);
        post.setUpdatedTimestamp(baseline.minusSeconds(seconds));
        resourceRepository.updateSilently(post);
    }

    private void verifyUnprivilegedPostUser(LinkedHashMap<Long, LinkedHashMultimap<State, String>> postNames) {
        TestHelper.verifyResources(
            postApi.getPosts(null, null, null, null, null),
            Collections.emptyList(),
            null);

        LinkedHashMultimap<State, String> statePostNames = getPostNamesByState(postNames);
        LinkedHashMultimap<State, PostRepresentation> statePosts = getPostsByState(
            postApi.getPosts(null, true, null, null, null));
        statePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(statePosts.get(state)),
                Lists.newArrayList(statePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(Lists.newArrayList(PUBLIC_ACTIONS.get(state)))));

        for (Long boardId : postNames.keySet()) {
            TestHelper.verifyResources(
                postApi.getPosts(boardId, null, null, null, null),
                Collections.emptyList(),
                null);

            LinkedHashMultimap<State, String> boardStatePostNames = postNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> boardStatePosts = getPostsByState(
                postApi.getPosts(boardId, true, null, null, null));
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
            postApi.getPosts(null, null, null, null, null));
        statePostNames.keySet().forEach(state ->
            TestHelper.verifyResources(
                Lists.newArrayList(statePosts.get(state)),
                Lists.newArrayList(statePostNames.get(state)),
                new TestHelper.ExpectedActions()
                    .add(getAdminActions(state, adminContext))));

        LinkedHashMultimap<State, String> publicStatePostNames = getPostNamesByState(publicPostNames);
        LinkedHashMultimap<State, PostRepresentation> mergedStatePosts = getPostsByState(
            postApi.getPosts(null, true, null, null, null));
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
                postApi.getPosts(boardId, null, null, null, null));
            boardStatePostNames.keySet().forEach(state ->
                TestHelper.verifyResources(
                    Lists.newArrayList(boardStatePosts.get(state)),
                    Lists.newArrayList(boardStatePostNames.get(state)),
                    new TestHelper.ExpectedActions()
                        .add(getAdminActions(state, adminContext))));

            LinkedHashMultimap<State, String> publicBoardStatePostNames = publicPostNames.get(boardId);
            LinkedHashMultimap<State, PostRepresentation> mergedBoardStatePosts = getPostsByState(
                postApi.getPosts(boardId, null, null, null, null));
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
        boardPostNameMap.forEach((key, value) -> postNamesByState.putAll(value));
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
        Post post = postRepository.findOne(postId);
        TestHelper.verifyNullableCount(viewCount, post.getViewCount());
        TestHelper.verifyNullableCount(referralCount, post.getReferralCount());
        TestHelper.verifyNullableCount(responseCount, post.getResponseCount());

        List<PostRepresentation> postRs = postApi.getPosts(null, true, null, null, null);
        TestHelper.verifyNullableCount(viewCount, postRs.get(0).getViewCount());
        TestHelper.verifyNullableCount(referralCount, postRs.get(0).getReferralCount());
        TestHelper.verifyNullableCount(responseCount, postRs.get(0).getResponseCount());

        TestHelper.verifyNullableCount(0L, postRs.get(1).getViewCount());
        TestHelper.verifyNullableCount(0L, postRs.get(1).getReferralCount());
        TestHelper.verifyNullableCount(0L, postRs.get(1).getResponseCount());
    }

    private void verifyPostReferral(String referral, TestHelper.MockHttpServletResponse response, String expectedLocation) {
        try {
            postApi.getPostReferral(referral, response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(expectedLocation, response.getLocation());
    }

    private static List<BoardAttachments> makeTestAttachments(String name) throws IOException {
        URL url = new URL("http://res.cloudinary.com/board-prism-hr/image/upload/v1506846526/test/attachment.pdf");
        URLConnection connection = url.openConnection();
        try (InputStream inputStream = connection.getInputStream()) {
            BoardAttachments attachments = new BoardAttachments();
            attachments.setContent(Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream)));
            attachments.setType(connection.getContentType());
            attachments.setFilename(name);
            attachments.setDisposition("attachment");
            attachments.setContentId("Application");
            return Collections.singletonList(attachments);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private enum PostAdminContext {
        ADMIN, AUTHOR
    }

    private interface PostOperation {
        PostRepresentation execute();
    }

}
