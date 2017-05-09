package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.definition.LocationDefinition;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import hr.prism.board.service.TestUserService;
import hr.prism.board.util.ObjectUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@TestContext
@RunWith(SpringRunner.class)
public class PostApiIT extends AbstractIT {

    @Inject
    private PostApi postApi;

    @Inject
    private BoardApi boardApi;

    @Inject
    private DepartmentApi departmentApi;

    @Inject
    private PostService postService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private TestUserService testUserService;

    @Test
    public void shouldCreateAndListPosts() {
        Map<String, Map<Scope, User>> unprivilegedUsers = new HashMap<>();

        User user11 = testUserService.authenticate();
        BoardDTO boardDTO11 = TestHelper.sampleBoard();
        boardDTO11.getDepartment().setName("department1");
        boardDTO11.setName("board11");
        BoardRepresentation boardR11 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO11));
        unprivilegedUsers.put("board11", makeUnprivilegedUsers(boardR11.getDepartment().getId(), boardR11.getId(), 110, 1100, TestHelper.samplePost()));

        User user12 = testUserService.authenticate();
        BoardDTO boardDTO12 = TestHelper.smallSampleBoard();
        boardDTO12.getDepartment().setName("department1");
        boardDTO12.setName("board12");
        BoardRepresentation boardR12 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO12));
        unprivilegedUsers.put("board12", makeUnprivilegedUsers(boardR12.getDepartment().getId(), boardR12.getId(), 120, 1200,
            TestHelper.smallSamplePost()
                .setMemberCategories(Collections.singletonList("m1"))));

        User user21 = testUserService.authenticate();
        BoardDTO boardDTO21 = TestHelper.smallSampleBoard();
        boardDTO21.getDepartment().setName("department2");
        boardDTO21.setName("board21");
        BoardRepresentation boardR21 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO21));
        unprivilegedUsers.put("board21", makeUnprivilegedUsers(boardR21.getDepartment().getId(), boardR21.getId(), 210, 2100, TestHelper.smallSamplePost()));

        User user22 = testUserService.authenticate();
        BoardDTO boardDTO22 = TestHelper.sampleBoard();
        boardDTO22.getDepartment().setName("department2");
        boardDTO22.setName("board22");
        BoardRepresentation boardR22 = transactionTemplate.execute(status -> boardApi.postBoard(boardDTO22));
        unprivilegedUsers.put("board22", makeUnprivilegedUsers(boardR22.getDepartment().getId(), boardR22.getId(), 220, 2200,
            TestHelper.smallSamplePost()
                .setPostCategories(Collections.singletonList("p1"))));

        LinkedHashMap<State, LinkedHashMap<User, PostRepresentation>> posts = new LinkedHashMap<>();
        for (State state : State.values()) {
            User postUser1 = testUserService.authenticate();
            for (int i = 1; i < 3; i++) {
                LinkedHashMap<User, PostRepresentation> userPosts = posts.computeIfAbsent(state, k -> new LinkedHashMap<>());

                userPosts.put(postUser1, verifyPostPostAndSetState(postUser1, boardR11.getId(),
                    TestHelper.samplePost()
                        .setName(boardR11.getName() + " " + state.name().toLowerCase() + " " + i),
                    state));

                userPosts.put(postUser1, verifyPostPostAndSetState(postUser1, boardR12.getId(),
                    TestHelper.smallSamplePost()
                        .setName(boardR12.getName() + " " + state.name().toLowerCase() + " " + i)
                        .setMemberCategories(Collections.singletonList("m1")),
                    state));
            }

            User postUser2 = testUserService.authenticate();
            for (int i = 1; i < 3; i++) {
                LinkedHashMap<User, PostRepresentation> userPosts = posts.computeIfAbsent(state, k -> new LinkedHashMap<>());

                userPosts.put(postUser2, verifyPostPostAndSetState(postUser2, boardR21.getId(),
                    TestHelper.smallSamplePost()
                        .setName(boardR21.getName() + " " + state.name().toLowerCase() + " " + i),
                    state));

                userPosts.put(postUser2, verifyPostPostAndSetState(postUser2, boardR22.getId(),
                    TestHelper.smallSamplePost()
                        .setName(boardR22.getName() + " " + state.name().toLowerCase() + " " + i)
                        .setPostCategories(Collections.singletonList("p1")),
                    state));
            }
        }
    }

    @Test
    public void shouldNotAcceptPostWithMissingApply() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList("m1"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_APPLY, status);
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
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList("m1"))
                .setApplyWebsite("http://www.google.com")
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_APPLY, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithMissingRelationDescriptionForUserWithoutAuthorRole() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        testUserService.authenticate();
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList("m1"))
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithCategoriesForBoardWithoutCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList("m1"));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithoutCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setMemberCategories(Collections.singletonList("m1"));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.smallSamplePost().setPostCategories(Collections.singletonList("p1"));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_MEMBER_CATEGORIES, status);
            return null;
        });
    }

    @Test
    public void shouldNotAcceptPostWithInvalidCategoriesForBoardWithCategories() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.samplePost().setPostCategories(Collections.singletonList("p4"));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.INVALID_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            PostDTO postDTO = TestHelper.samplePost().setMemberCategories(Collections.singletonList("m4"));
            ExceptionUtils.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.INVALID_POST_MEMBER_CATEGORIES, status);
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
            ExceptionUtils.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.empty())),
                ExceptionCode.MISSING_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.empty())),
                ExceptionCode.MISSING_POST_MEMBER_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.of(Collections.singletonList("p4")))),
                ExceptionCode.INVALID_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.of(Collections.singletonList("m4")))),
                ExceptionCode.INVALID_POST_MEMBER_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> departmentApi.updateDepartment(departmentId, new DepartmentPatchDTO().setMemberCategories(Optional.empty())));
        transactionTemplate.execute(status -> boardApi.updateBoard(boardId, new BoardPatchDTO().setPostCategories(Optional.empty())));

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setPostCategories(Optional.of(Collections.singletonList("p1")))),
                ExceptionCode.CORRUPTED_POST_POST_CATEGORIES, status);
            return null;
        });

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO()
                        .setMemberCategories(Optional.of(Collections.singletonList("m1")))),
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

        User boardUser = testUserService.authenticate();
        transactionTemplate.execute(status -> {
            userRoleService.createUserRole(boardId, boardUser.getId(), Role.ADMINISTRATOR);
            return null;
        });

        List<User> adminUsers = Arrays.asList(departmentUser, boardUser);

        // Create post
        User postUser = testUserService.authenticate();
        PostRepresentation postR = verifyPostPost(postUser, boardId, TestHelper.samplePost());
        Long postId = postR.getId();

        // Create unprivileged users
        Collection<User> unprivilegedUsers = makeUnprivilegedUsers(departmentId, boardId, 2, 2, TestHelper.samplePost()).values();

        Map<Action, Runnable> operations = ImmutableMap.<Action, Runnable>builder()
            .put(Action.VIEW, () -> postApi.getPost(postId))
            .put(Action.AUDIT, () -> postApi.getPostOperations(postId))
            .put(Action.EDIT, () -> postApi.updatePost(postId, new PostPatchDTO()))
            .put(Action.ACCEPT, () -> postApi.acceptPost(postId, new PostPatchDTO()))
            .put(Action.SUSPEND, () -> postApi.suspendPost(postId, new PostPatchDTO()))
            .put(Action.CORRECT, () -> postApi.correctPost(postId, new PostPatchDTO()))
            .put(Action.REJECT, () -> postApi.rejectPost(postId, new PostPatchDTO()))
            .put(Action.RESTORE, () -> postApi.restorePost(postId, new PostPatchDTO()))
            .put(Action.WITHDRAW, () -> postApi.withdrawPost(postId, new PostPatchDTO()))
            .build();

        verifyPostActionsInDraft(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Check that we do not audit viewing
        transactionTemplate.execute(status -> postApi.getPost(postId));

        LocalDateTime liveTimestamp = postR.getLiveTimestamp();
        LocalDateTime deadTimestamp = postR.getDeadTimestamp();

        LocalDateTime liveTimestampDelayed = LocalDateTime.now().plusWeeks(4L).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime deadTimestampDelayed = LocalDateTime.now().plusWeeks(8L).truncatedTo(ChronoUnit.SECONDS);

        // Check that the author can update the post
        PostPatchDTO updateDTO = new PostPatchDTO()
            .setName(Optional.of("post 2"))
            .setDescription(Optional.of("description 2"))
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
            .setMemberCategories(Optional.of(Arrays.asList("m2", "m1")))
            .setExistingRelation(Optional.of(ExistingRelation.STAFF))
            .setExistingRelationExplanation(Optional.of(ObjectUtils.orderedMap("jobTitle", "professor")))
            .setLiveTimestamp(Optional.of(liveTimestampDelayed))
            .setDeadTimestamp(Optional.of(deadTimestampDelayed));

        verifyPatchPost(postUser, postId, updateDTO, () -> postApi.updatePost(postId, updateDTO), State.DRAFT);
        verifyPostActionsInDraft(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Check that the administrator can make changes and suspend the post
        PostPatchDTO suspendDTO = new PostPatchDTO()
            .setLiveTimestamp(Optional.of(liveTimestamp))
            .setDeadTimestamp(Optional.of(deadTimestamp))
            .setComment("could you please explain what you will pay the successful applicant");

        verifyPatchPost(departmentUser, postId, suspendDTO, () -> postApi.suspendPost(postId, suspendDTO), State.SUSPENDED);

        verifyResourceActions(Scope.POST, postId, operations);
        verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations);
        verifyResourceActions(adminUsers, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT);
        verifyResourceActions(postUser, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW, Action.CORRECT);

        // Check that the author can make changes and correct the post
        PostPatchDTO correctDTO = new PostPatchDTO()
            .setOrganizationName(Optional.of("organization name"))
            .setLocation(Optional.of(
                new LocationDTO()
                    .setName("birmingham")
                    .setDomicile("GB")
                    .setGoogleId("uuu")
                    .setLatitude(BigDecimal.ZERO)
                    .setLongitude(BigDecimal.ZERO)))
            .setApplyDocument(Optional.of(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f")))
            .setMemberCategories(Optional.of(Arrays.asList("m1", "m2")))
            .setComment("i uploaded a document this time which explains that");

        verifyPatchPost(postUser, postId, correctDTO, () -> postApi.correctPost(postId, correctDTO), State.DRAFT);
        verifyPostActionsInDraft(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Check that the administrator can accept post in the accepted state
        PostPatchDTO acceptDTO = new PostPatchDTO()
            .setLiveTimestamp(Optional.empty())
            .setDeadTimestamp(Optional.empty())
            .setComment("accepting without time constraints");

        verifyPatchPost(boardUser, postId, acceptDTO, () -> postApi.acceptPost(postId, acceptDTO), State.ACCEPTED);
        verifyPostActionsInAccepted(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Suspend the post so that it can be accepted again
        verifyPatchPost(boardUser, postId, new PostPatchDTO(), () -> postApi.suspendPost(postId, new PostPatchDTO()), State.SUSPENDED);

        // Check that the administrator can make further changes and accept the post in the pending state
        PostPatchDTO acceptPendingDTO = new PostPatchDTO()
            .setApplyWebsite(Optional.of("http://www.twitter.com"))
            .setPostCategories(Optional.of(Arrays.asList("p1", "p2")))
            .setLiveTimestamp(Optional.of(liveTimestampDelayed))
            .setDeadTimestamp(Optional.of(deadTimestampDelayed))
            .setComment("this looks good now - i replaced the document with the complete website for the opportunity");

        verifyPatchPost(boardUser, postId, acceptPendingDTO, () -> postApi.acceptPost(postId, acceptPendingDTO), State.PENDING);
        verifyPostActionsInPendingOrExpired(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Check that the post stays in pending state when the update job runs
        verifyPublishAndRetirePost(postId, State.PENDING);

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setLiveTimestamp(liveTimestamp);
            post.setDeadTimestamp(deadTimestamp);
            return null;
        });

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActionsInAccepted(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Check that the administrator can reject the post
        PostPatchDTO rejectDTO = new PostPatchDTO()
            .setComment("we have received a complaint, we're closing down the post");

        verifyPatchPost(departmentUser, postId, rejectDTO, () -> postApi.rejectPost(postId, rejectDTO), State.REJECTED);

        verifyResourceActions(Scope.POST, postId, operations);
        verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations);
        verifyResourceActions(adminUsers, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE);
        verifyResourceActions(postUser, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW);

        // Check that the administrator can restore the post
        PostPatchDTO restoreFromRejectedDTO = new PostPatchDTO()
            .setComment("sorry we made a mistake, we're restoring the post");

        verifyPatchPost(boardUser, postId, restoreFromRejectedDTO, () -> postApi.restorePost(postId, restoreFromRejectedDTO), State.ACCEPTED);
        verifyPostActionsInAccepted(adminUsers, postUser, unprivilegedUsers, postId, operations);

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setDeadTimestamp(liveTimestamp.minusSeconds(1));
            return null;
        });

        // Check that the post now moves to the expired state when the update job runs
        verifyPublishAndRetirePost(postId, State.EXPIRED);
        verifyPostActionsInPendingOrExpired(adminUsers, postUser, unprivilegedUsers, postId, operations);

        // Check that the author can withdraw the post
        // It is likely that the comment would be empty in this case - we can make it optional
        PostPatchDTO withdrawDTO = new PostPatchDTO();

        verifyPatchPost(postUser, postId, withdrawDTO, () -> postApi.withdrawPost(postId, withdrawDTO), State.WITHDRAWN);

        verifyResourceActions(Scope.POST, postId, operations);
        verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations);
        verifyResourceActions(adminUsers, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT);
        verifyResourceActions(postUser, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.RESTORE);

        // Check that the author can restore the post
        // It is likely that the comment would be empty in this case - we can make it optional
        PostPatchDTO restoreFromWithdrawnDTO = new PostPatchDTO();

        verifyPatchPost(postUser, postId, restoreFromWithdrawnDTO, () -> postApi.restorePost(postId, restoreFromWithdrawnDTO), State.EXPIRED);
        verifyPostActionsInPendingOrExpired(adminUsers, postUser, unprivilegedUsers, postId, operations);

        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setDeadTimestamp(null);
            return null;
        });

        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
        verifyPostActionsInAccepted(adminUsers, postUser, unprivilegedUsers, postId, operations);

        testUserService.setAuthentication(postUser.getStormpathId());
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> postApi.getPostOperations(postId));
        Assert.assertEquals(18, resourceOperationRs.size());

        // Operations are returned most recent first - reverse the order to make it easier to test
        resourceOperationRs = Lists.reverse(resourceOperationRs);
        TestHelper.verifyResourceOperation(resourceOperationRs.get(0), Action.EXTEND, postUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(1), Action.EDIT, postUser,
            new ResourceChangeListRepresentation()
                .put("name", "post", "post 2")
                .put("description", "description", "description 2")
                .put("organizationName", "organization name", "organization name 2")
                .put("location",
                    ObjectUtils.orderedMap("name", "krakow", "domicile", "PL", "googleId", "sss",
                        "latitude", BigDecimal.ONE.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.ONE.stripTrailingZeros().toPlainString()),
                    ObjectUtils.orderedMap("name", "london", "domicile", "GB", "googleId", "ttt",
                        "latitude", BigDecimal.TEN.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.TEN.stripTrailingZeros().toPlainString()))
                .put("applyWebsite", "http://www.google.co.uk", "http://www.facebook.com")
                .put("postCategories", Arrays.asList("p1", "p2"), Arrays.asList("p2", "p1"))
                .put("memberCategories", Arrays.asList("m1", "m2"), Arrays.asList("m2", "m1"))
                .put("existingRelation", "STUDENT", "STAFF")
                .put("existingRelationExplanation",
                    ObjectUtils.orderedMap("studyLevel", "MASTER"),
                    ObjectUtils.orderedMap("jobTitle", "professor"))
                .put("liveTimestamp", liveTimestamp.toString(), liveTimestampDelayed.toString())
                .put("deadTimestamp", deadTimestamp.toString(), deadTimestampDelayed.toString()));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(2), Action.EDIT, departmentUser,
            new ResourceChangeListRepresentation()
                .put("liveTimestamp", liveTimestampDelayed.toString(), liveTimestamp.toString())
                .put("deadTimestamp", deadTimestampDelayed.toString(), deadTimestamp.toString()));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(3), Action.SUSPEND, departmentUser,
            "could you please explain what you will pay the successful applicant");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(4), Action.EDIT, postUser,
            new ResourceChangeListRepresentation()
                .put("organizationName", "organization name 2", "organization name")
                .put("location",
                    ObjectUtils.orderedMap("name", "london", "domicile", "GB", "googleId", "ttt",
                        "latitude", BigDecimal.TEN.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.TEN.stripTrailingZeros().toPlainString()),
                    ObjectUtils.orderedMap("name", "birmingham", "domicile", "GB", "googleId", "uuu",
                        "latitude", BigDecimal.ZERO.stripTrailingZeros().toPlainString(), "longitude", BigDecimal.ZERO.stripTrailingZeros().toPlainString()))
                .put("applyWebsite", "http://www.facebook.com", null)
                .put("applyDocument", null, ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"))
                .put("memberCategories", Arrays.asList("m2", "m1"), Arrays.asList("m1", "m2")));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(5), Action.CORRECT, postUser,
            "i uploaded a document this time which explains that");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(6), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("liveTimestamp", liveTimestamp.toString(), null)
                .put("deadTimestamp", deadTimestamp.toString(), null));

        TestHelper.verifyResourceOperation(resourceOperationRs.get(7), Action.ACCEPT, boardUser, "accepting without time constraints");

        TestHelper.verifyResourceOperation(resourceOperationRs.get(8), Action.SUSPEND, boardUser);

        TestHelper.verifyResourceOperation(resourceOperationRs.get(9), Action.EDIT, boardUser,
            new ResourceChangeListRepresentation()
                .put("applyWebsite", null, "http://www.twitter.com")
                .put("applyDocument", ObjectUtils.orderedMap("cloudinaryId", "c", "cloudinaryUrl", "u", "fileName", "f"), null)
                .put("postCategories", Arrays.asList("p2", "p1"), Arrays.asList("p1", "p2"))
                .put("liveTimestamp", null, liveTimestampDelayed.toString())
                .put("deadTimestamp", null, deadTimestampDelayed.toString()));

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

    private PostRepresentation verifyPostPost(User user, Long boardId, PostDTO postDTO) {
        return transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.postPost(boardId, postDTO);

            assertEquals(postDTO.getName(), postR.getName());
            assertEquals(postDTO.getDescription(), postR.getDescription());
            assertEquals(postDTO.getOrganizationName(), postR.getOrganizationName());
            verifyLocation(postDTO.getLocation(), postR);

            assertEquals(postDTO.getPostCategories(), postR.getPostCategories());
            assertEquals(postDTO.getMemberCategories(), postR.getMemberCategories());
            assertEquals(postDTO.getExistingRelation(), postR.getExistingRelation());
            assertEquals(postDTO.getExistingRelationExplanation(), postR.getExistingRelationExplanation());
            assertEquals(postDTO.getApplyWebsite(), postR.getApplyWebsite());

            DocumentDTO applyDocumentDTO = postDTO.getApplyDocument();
            verifyDocument(applyDocumentDTO, postR.getApplyDocument());

            assertEquals(postDTO.getApplyEmail(), postR.getApplyEmail());
            assertEquals(postDTO.getLiveTimestamp().truncatedTo(ChronoUnit.SECONDS), postR.getLiveTimestamp().truncatedTo(ChronoUnit.SECONDS));
            assertEquals(postDTO.getDeadTimestamp().truncatedTo(ChronoUnit.SECONDS), postR.getDeadTimestamp().truncatedTo(ChronoUnit.SECONDS));

            Post post = postService.getPost(postR.getId());
            Assert.assertTrue(userRoleService.hasUserRole(post, user, Role.ADMINISTRATOR));

            Board board = boardService.getBoard(postR.getBoard().getId());
            Department department = departmentService.getDepartment(postR.getBoard().getDepartment().getId());
            assertThat(post.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(post, board, department));
            return postR;
        });
    }

    private PostRepresentation verifyPatchPost(User user, Long postId, PostPatchDTO postDTO, PostOperation operation, State expectedState) {
        testUserService.setAuthentication(user.getStormpathId());
        return transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            PostRepresentation postR = operation.execute();

            Optional<String> nameOptional = postDTO.getName();
            assertEquals(nameOptional == null ? post.getName() : nameOptional.orElse(null), postR.getName());

            Optional<String> descriptionOptional = postDTO.getDescription();
            assertEquals(descriptionOptional == null ? post.getDescription() : descriptionOptional.orElse(null), postR.getDescription());

            Optional<String> organizationNameOptional = postDTO.getOrganizationName();
            assertEquals(organizationNameOptional == null ? post.getOrganizationName() : organizationNameOptional.orElse(null), postR.getOrganizationName());

            Optional<LocationDTO> locationOptional = postDTO.getLocation();
            verifyLocation(locationOptional == null ? post.getLocation() : locationOptional.orElse(null), postR);

            Optional<List<String>> postCategoriesOptional = postDTO.getPostCategories();
            assertEquals(postCategoriesOptional == null ? resourceService.getCategories(post, CategoryType.POST) : postCategoriesOptional.orElse(null),
                postR.getPostCategories());

            Optional<List<String>> memberCategoriesOptional = postDTO.getMemberCategories();
            assertEquals(memberCategoriesOptional == null ? resourceService.getCategories(post, CategoryType.MEMBER) : memberCategoriesOptional.orElse(null),
                postR.getMemberCategories());

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

    private void verifyPostActionsInDraft(List<User> adminUsers, User postUser, Collection<User> unprivilegedUsers, Long postId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.POST, postId, operations);
        verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations);
        verifyResourceActions(adminUsers, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND);
        verifyResourceActions(postUser, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW);
    }

    private void verifyPostActionsInPendingOrExpired(List<User> adminUsers, User postUser, Collection<User> unprivilegedUsers, Long postId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.POST, postId, operations);
        verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations);
        verifyResourceActions(adminUsers, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND);
        verifyResourceActions(postUser, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW);
    }

    private void verifyPostActionsInAccepted(List<User> adminUsers, User postUser, Collection<User> unprivilegedUsers, Long postId, Map<Action, Runnable> operations) {
        verifyResourceActions(Scope.POST, postId, operations, Action.VIEW);
        verifyResourceActions(unprivilegedUsers, Scope.POST, postId, operations, Action.VIEW);
        verifyResourceActions(adminUsers, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND);
        verifyResourceActions(postUser, Scope.POST, postId, operations, Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW);
    }

    private PostRepresentation verifyPostPostAndSetState(User user, Long boardId, PostDTO postDTO, State state) {
        PostRepresentation postR = verifyPostPost(user, boardId, postDTO);
        transactionTemplate.execute(status -> postService.getPost(postR.getId()).setState(state));
        return postR;
    }

    private interface PostOperation {

        PostRepresentation execute();

    }

}
