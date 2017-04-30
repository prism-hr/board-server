package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.*;
import hr.prism.board.service.*;
import hr.prism.board.util.ObjectUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class PostApiIT extends AbstractIT {
    
    @Inject
    private PostApi postApi;
    
    @Inject
    private BoardApi boardApi;
    
    @Inject
    private PostService postService;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private TestUserService testUserService;
    
    @Inject
    private ActionService actionService;
    
    @Test
    public void shouldCreatePost() {
        User user = testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(ImmutableList.of("p1", "p3"))
                .setMemberCategories(ImmutableList.of("m1", "m3"))
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("CloudinaryId").setCloudinaryUrl("http://cloudinary.com"))
                .setLiveTimestamp(LocalDateTime.now().plus(1, ChronoUnit.MONTHS))
                .setDeadTimestamp(LocalDateTime.now().plus(1, ChronoUnit.YEARS));
    
            PostRepresentation postR = postApi.postPost(boardId, postDTO);
            verifyPost(user, postDTO, postR);
            return null;
        });
    }
    
    @Test
    public void shouldUpdatePost() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        Long postId = transactionTemplate.execute(status -> postApi.postPost(boardId,
            new PostDTO()
                .setName("New Post")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(ImmutableList.of("p1", "p3"))
                .setMemberCategories(ImmutableList.of("m1", "m3"))
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("CloudinaryId").setCloudinaryUrl("http://cloudinary.com"))
                .setLiveTimestamp(LocalDateTime.now().plus(1, ChronoUnit.MONTHS))
                .setDeadTimestamp(LocalDateTime.now()))
            .getId());
        
        transactionTemplate.execute(status -> {
            PostPatchDTO postPatchDTO = new PostPatchDTO()
                .setName(Optional.of("shouldUpdatePost Board2"))
                .setDescription(Optional.of("Desc"))
                .setOrganizationName(Optional.of("shouldUpdatePost Organization2"))
                .setLocation(Optional.of(new LocationDTO().setName("location2").setDomicile("NG")
                    .setGoogleId("shouldUpdatePost GoogleId2").setLatitude(BigDecimal.TEN).setLongitude(BigDecimal.TEN)))
                .setPostCategories(Optional.of(ImmutableList.of("p2", "p3")))
                .setMemberCategories(Optional.of(ImmutableList.of("m2", "m3")))
                .setApplyDocument(Optional.of(new DocumentDTO().setFileName("file2").setCloudinaryId("shouldUpdatePost CloudinaryId2").setCloudinaryUrl("http://cloudinary2.com")))
                .setLiveTimestamp(Optional.empty())
                .setDeadTimestamp(Optional.empty());
            PostRepresentation postR = postApi.updatePost(postId, postPatchDTO);
            
            // make sure timestamps are set to defaults
            postPatchDTO.setLiveTimestamp(Optional.of(LocalDateTime.now()));
            postPatchDTO.setLiveTimestamp(Optional.of(LocalDateTime.now().plus(1, ChronoUnit.MONTHS)));
            verifyPost(postPatchDTO, postR);
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
            ExceptionUtil.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotAcceptPostWithCategoriesForBoardWithoutCategories() {
        
    }
    
    @Test
    public void shouldNotAcceptPostWithoutCategoriesForBoardWithCategories() {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetPosts() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post 1")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList("m1"))
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            postApi.postPost(boardId, postDTO);
            return null;
        });
        
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("post 2")
                .setDescription("description")
                .setOrganizationName("organization name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(Collections.singletonList("p1"))
                .setMemberCategories(Collections.singletonList("m1"))
                .setApplyDocument(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            postApi.postPost(boardId, postDTO);
            return null;
        });
        
        transactionTemplate.execute(status -> {
            List<PostRepresentation> posts = postApi.getPostsByBoard(boardId);
            assertThat(posts, contains(hasProperty("name", equalTo("post 2")),
                hasProperty("name", equalTo("post 1"))));
            return null;
        });
    }
    
    @Test
    public void shouldNotBeAbleToCorruptPostByPatching() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        Long postId = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.samplePost()).getId());
        
        // TODO: coverage for missing / corrupted / invalid categories
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId,
                        new PostPatchDTO()
                            .setName(Optional.of("name"))
                            .setDescription(Optional.of("description"))
                            .setOrganizationName(Optional.of("organization name"))
                            .setLocation(Optional.of(new LocationDTO().setName("name").setDomicile("PL")
                                .setGoogleId("google").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                            .setApplyWebsite(Optional.empty())
                            .setApplyEmail(Optional.empty())
                            .setApplyDocument(Optional.empty())),
                ExceptionCode.MISSING_POST_APPLY, null);
            status.setRollbackOnly();
            return null;
        });
    }
    
    @Test
    public void shouldAuditPostAndMakeChangesPrivatelyVisible() {
        User user = testUserService.authenticate();
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()));
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();
    
        User postUser = testUserService.authenticate();
        PostRepresentation postR = transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.samplePost()));
        Assert.assertEquals(State.DRAFT, postR.getState());
        Long postId = postR.getId();
    
        verifyPost(postId, user, State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Test that we do not audit viewing
        transactionTemplate.execute(status -> {
            postApi.getPost(postId);
            return null;
        });
    
        LocalDateTime liveTimestampExtend = postR.getLiveTimestamp();
        LocalDateTime deadTimestampExtend = postR.getDeadTimestamp();
    
        LocalDateTime liveTimestampSuspend = LocalDateTime.now().plusWeeks(4L).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime deadTimestampSuspend = LocalDateTime.now().plusWeeks(8L).truncatedTo(ChronoUnit.SECONDS);
    
        // Check that the administrator can make changes and suspend the post
        testUserService.setAuthentication(user.getStormpathId());
        transactionTemplate.execute(status -> postApi.suspendPost(postId,
            (PostPatchDTO) new PostPatchDTO()
                .setName(Optional.of("name 2"))
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
                .setExistingRelation(Optional.of(ExistingRelation.STAFF))
                .setExistingRelationExplanation(Optional.of(ObjectUtils.orderedMap("jobTitle", "professor")))
                .setPostCategories(Optional.of(Arrays.asList("p2", "p1")))
                .setMemberCategories(Optional.of(Arrays.asList("m2", "m1")))
                .setLiveTimestamp(Optional.of(liveTimestampSuspend))
                .setDeadTimestamp(Optional.of(deadTimestampSuspend))
                .setComment("could you please explain what you will pay the successful applicant")));
    
        verifyPost(postId, user, State.SUSPENDED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT));
        verifyPost(postId, postUser, State.SUSPENDED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW, Action.CORRECT));
        
        // Check that the author can make changes and correct the post
        testUserService.setAuthentication(postUser.getStormpathId());
        transactionTemplate.execute(status -> postApi.correctPost(postId,
            (PostPatchDTO) new PostPatchDTO()
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
                .setComment("i uploaded a document this time which explains that")));
    
        verifyPost(postId, user, State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.DRAFT, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Check that the administrator can make further changes and accept the post
        testUserService.setAuthentication(user.getStormpathId());
        transactionTemplate.execute(status -> postApi.acceptPost(postId,
            (PostPatchDTO) new PostPatchDTO()
                .setOrganizationName(Optional.of("organization name"))
                .setLocation(Optional.of(
                    new LocationDTO()
                        .setName("birmingham")
                        .setDomicile("GB")
                        .setGoogleId("uuu")
                        .setLatitude(BigDecimal.ZERO)
                        .setLongitude(BigDecimal.ZERO)))
                .setApplyWebsite(Optional.of("http://www.twitter.com"))
                .setPostCategories(Optional.of(Arrays.asList("p1", "p2")))
                .setComment("this looks good now - i replaced the document with the complete website for the opportunity")));
    
        verifyPost(postId, user, State.PENDING, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.PENDING, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Check that the post stays in pending state when the update job runs
        verifyPublishAndRetirePost(postId, State.PENDING);
    
        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setLiveTimestamp(liveTimestampExtend);
            post.setDeadTimestamp(deadTimestampExtend);
            return null;
        });
    
        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
    
        verifyPost(postId, user, State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Check that the administrator can reject the post
        testUserService.setAuthentication(user.getStormpathId());
        transactionTemplate.execute(status -> postApi.rejectPost(postId,
            (PostPatchDTO) new PostPatchDTO()
                .setComment("we have received a complaint, we're closing down the post")));
    
        verifyPost(postId, user, State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE));
        verifyPost(postId, postUser, State.REJECTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Check that the administrator can restore the post
        testUserService.setAuthentication(user.getStormpathId());
        transactionTemplate.execute(status -> postApi.restorePost(postId,
            (PostPatchDTO) new PostPatchDTO()
                .setComment("sorry we made a mistake, we're restoring the post")));
    
        verifyPost(postId, user, State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.ACCEPTED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        transactionTemplate.execute(status -> {
            Post post = postService.getPost(postId);
            post.setDeadTimestamp(liveTimestampExtend.minusSeconds(1));
            return null;
        });
    
        // Check that the post now moves to the expired state when the update job runs
        verifyPublishAndRetirePost(postId, State.EXPIRED);
    
        verifyPost(postId, user, State.EXPIRED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.EXPIRED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Check that the author can withdraw the post
        testUserService.setAuthentication(postUser.getStormpathId());
        postR = transactionTemplate.execute(status -> postApi.withdrawPost(postId,
            (PostPatchDTO) new PostPatchDTO()
                .setComment("this is rubbish, I'm withdrawing the post anyway")));
        assertEquals(State.WITHDRAWN, postR.getState());
    
        verifyPost(postId, user, State.WITHDRAWN, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT));
        verifyPost(postId, postUser, State.WITHDRAWN, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.RESTORE));
        
        // Check that the author can restore the post
        testUserService.setAuthentication(postUser.getStormpathId());
        transactionTemplate.execute(status -> postApi.restorePost(postId,
            (PostPatchDTO) new PostPatchDTO()
                .setComment("oh well, i'll give it one more chance")));
    
        verifyPost(postId, user, State.EXPIRED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND));
        verifyPost(postId, postUser, State.EXPIRED, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW));
        
        // Test that unprivileged users cannot view the audit trail
        verifyUnprivilegedUsers(departmentId, boardId, TestHelper.samplePost(), () -> postApi.getPostOperations(postId));
    }
    
    private void verifyPost(User user, PostDTO postDTO, PostRepresentation postR) {
        assertEquals(postDTO.getName(), postR.getName());
        assertEquals(postDTO.getDescription(), postR.getDescription());
        assertEquals(postDTO.getOrganizationName(), postR.getOrganizationName());
        
        LocationRepresentation locationR = postR.getLocation();
        LocationDTO locationDTO = postDTO.getLocation();
        assertEquals(locationDTO.getName(), locationR.getName());
        assertEquals(locationDTO.getDomicile(), locationR.getDomicile());
        assertEquals(locationDTO.getGoogleId(), locationR.getGoogleId());
        assertThat(locationR.getLatitude(), Matchers.comparesEqualTo(locationDTO.getLatitude()));
        assertThat(locationR.getLongitude(), Matchers.comparesEqualTo(locationDTO.getLongitude()));
        
        assertEquals(postDTO.getExistingRelation(), postR.getExistingRelation());
        assertEquals(postDTO.getPostCategories(), postR.getPostCategories());
        assertEquals(postDTO.getMemberCategories(), postR.getMemberCategories());
        assertEquals(postDTO.getApplyWebsite(), postR.getApplyWebsite());
        
        DocumentRepresentation applyDocumentR = postR.getApplyDocument();
        DocumentDTO applyDocumentDTO = postDTO.getApplyDocument();
        assertEquals(applyDocumentDTO.getFileName(), applyDocumentR.getFileName());
        assertEquals(applyDocumentDTO.getCloudinaryId(), applyDocumentR.getCloudinaryId());
        assertEquals(applyDocumentDTO.getCloudinaryUrl(), applyDocumentR.getCloudinaryUrl());
        
        assertEquals(postDTO.getApplyEmail(), postR.getApplyEmail());
        assertEquals(postDTO.getLiveTimestamp().truncatedTo(ChronoUnit.SECONDS), postR.getLiveTimestamp().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(postDTO.getDeadTimestamp().truncatedTo(ChronoUnit.SECONDS), postR.getDeadTimestamp().truncatedTo(ChronoUnit.SECONDS));
        assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
            Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW, Action.SUSPEND, Action.REJECT));
        
        Post post = postService.getPost(postR.getId());
        Assert.assertTrue(userRoleService.hasUserRole(post, user, Role.ADMINISTRATOR));
        
        Board board = boardService.getBoard(postR.getBoard().getId());
        Department department = departmentService.getDepartment(postR.getBoard().getDepartment().getId());
        assertThat(post.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(post, board, department));
    }
    
    private void verifyPost(PostPatchDTO postDTO, PostRepresentation postR) {
        assertEquals(postDTO.getName().orElse(null), postR.getName());
        assertEquals(postDTO.getDescription().orElse(null), postR.getDescription());
        assertEquals(postDTO.getOrganizationName().orElse(null), postR.getOrganizationName());
        
        LocationRepresentation locationR = postR.getLocation();
        LocationDTO locationDTO = postDTO.getLocation().orElse(null);
        assertEquals(locationDTO.getName(), locationR.getName());
        assertEquals(locationDTO.getDomicile(), locationR.getDomicile());
        assertEquals(locationDTO.getGoogleId(), locationR.getGoogleId());
        assertEquals(locationDTO.getLatitude(), locationR.getLatitude());
        assertEquals(locationDTO.getLongitude(), locationR.getLongitude());
    
        assertEquals(postDTO.getPostCategories().orElse(null), postR.getPostCategories());
        assertEquals(postDTO.getMemberCategories().orElse(null), postR.getMemberCategories());
        assertEquals(postDTO.getApplyWebsite() == null ? null : postDTO.getApplyWebsite().orElse(null), postR.getApplyWebsite());
        
        DocumentRepresentation applyDocumentR = postR.getApplyDocument();
        DocumentDTO applyDocumentDTO = postDTO.getApplyDocument().orElse(null);
        if (applyDocumentDTO == null) {
            Assert.assertNull(applyDocumentR);
        } else {
            assertEquals(applyDocumentDTO.getFileName(), applyDocumentR.getFileName());
            assertEquals(applyDocumentDTO.getCloudinaryId(), applyDocumentR.getCloudinaryId());
            assertEquals(applyDocumentDTO.getCloudinaryUrl(), applyDocumentR.getCloudinaryUrl());
        }
    
        assertEquals(postDTO.getApplyEmail() == null ? null : postDTO.getApplyEmail().orElse(null), postR.getApplyEmail());
    }
    
    private void verifyPost(Long postId, User user, State state, Collection<Action> actions) {
        testUserService.setAuthentication(user.getStormpathId());
        PostRepresentation postR = transactionTemplate.execute(status -> postApi.getPost(postId));
        Assert.assertEquals(state, postR.getState());
        
        Post post = postService.getPost(postId);
        for (Action action : Action.values()) {
            if (actions.contains(action)) {
                assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(actions.toArray(new Action[0])));
            } else {
                ExceptionUtil.verifyApiException(ApiForbiddenException.class,
                    () -> actionService.executeAction(user, post, action, null),
                    ExceptionCode.FORBIDDEN_ACTION, null);
            }
        }
    }
    
    private void verifyPublishAndRetirePost(Long postId, State expectedState) {
        PostRepresentation postR;
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });
        
        postR = transactionTemplate.execute(status -> postApi.getPost(postId));
        Assert.assertEquals(expectedState, postR.getState());
    }
    
}
