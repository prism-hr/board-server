package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.definition.DocumentDefinition;
import hr.prism.board.definition.LocationDefinition;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.*;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import hr.prism.board.service.TestUserService;
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
import static org.junit.Assert.*;

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
    private DepartmentService departmentService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private TestUserService testUserService;
    
    @Test
    public void shouldCreatePost() {
        User user = testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()).getId());
        verifyPostPost(user, boardId,
            new PostDTO()
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
                .setDeadTimestamp(LocalDateTime.now().plus(1, ChronoUnit.YEARS)));
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
    public void shouldAuditPostAndMakeChangesPrivatelyVisible() {
        // Create admin users / resources
        User dUser = testUserService.authenticate();
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.sampleBoard()));
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();
    
        User bUser = testUserService.authenticate();
        Board board = boardService.getBoard(boardId);
        transactionTemplate.execute(status -> {
            userRoleService.createUserRole(board, bUser, Role.ADMINISTRATOR);
            return null;
        });
    
        List<User> aUsers = Arrays.asList(dUser, bUser);
    
        // Create resource user
        User pUser = testUserService.authenticate();
        PostRepresentation postR = verifyPostPost(pUser, boardId, TestHelper.samplePost());
    
        transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.samplePost()));
        Long postId = postR.getId();
    
        // Create unprivileged users
        List<User> uUsers = makeUnprivilegedUsers(departmentId, boardId, TestHelper.samplePost());
    
        // Check submission
        verifyResourceActions(
            Scope.POST, postId, State.DRAFT,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND)));
    
        // Check that we do not audit viewing
        transactionTemplate.execute(status -> {
            postApi.getPost(postId);
            return null;
        });
    
        LocalDateTime liveTimestampExtend = postR.getLiveTimestamp();
        LocalDateTime deadTimestampExtend = postR.getDeadTimestamp();
    
        LocalDateTime liveTimestampSuspend = LocalDateTime.now().plusWeeks(4L).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime deadTimestampSuspend = LocalDateTime.now().plusWeeks(8L).truncatedTo(ChronoUnit.SECONDS);
    
        // Check that the administrator can make changes and suspend the post
        testUserService.setAuthentication(dUser.getStormpathId());
        PostPatchDTO postSuspendDTO = (PostPatchDTO) new PostPatchDTO()
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
            .setComment("could you please explain what you will pay the successful applicant");
    
        postR = transactionTemplate.execute(status -> postApi.suspendPost(postId, postSuspendDTO));
        Post post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postSuspendDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.SUSPENDED,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW, Action.CORRECT))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT)));
        
        // Check that the author can make changes and correct the post
        testUserService.setAuthentication(pUser.getStormpathId());
        PostPatchDTO postCorrectDTO = (PostPatchDTO) new PostPatchDTO()
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
    
        postR = transactionTemplate.execute(status -> postApi.correctPost(postId, postCorrectDTO));
        post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postCorrectDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.DRAFT,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND)));
        
        // Check that the administrator can make further changes and accept the post
        testUserService.setAuthentication(bUser.getStormpathId());
        PostPatchDTO postAcceptDTO = (PostPatchDTO) new PostPatchDTO()
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
            .setComment("this looks good now - i replaced the document with the complete website for the opportunity");
    
        postR = transactionTemplate.execute(status -> postApi.acceptPost(postId, postAcceptDTO));
        post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postAcceptDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.PENDING,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND)));
        
        // Check that the post stays in pending state when the update job runs
        verifyPublishAndRetirePost(postId, State.PENDING);
    
        transactionTemplate.execute(status -> {
            Post publishedPost = postService.getPost(postId);
            publishedPost.setLiveTimestamp(liveTimestampExtend);
            publishedPost.setDeadTimestamp(deadTimestampExtend);
            return null;
        });
    
        // Check that the post now moves to the accepted state when the update job runs
        verifyPublishAndRetirePost(postId, State.ACCEPTED);
    
        verifyResourceActions(
            Scope.POST, postId, State.ACCEPTED,
            new ExpectedUserActions()
                .add(Collections.singletonList(Action.VIEW), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.singletonList(Action.VIEW), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND)));
        
        // Check that the administrator can reject the post
        testUserService.setAuthentication(dUser.getStormpathId());
        PostPatchDTO postRejectDTO = (PostPatchDTO) new PostPatchDTO().setComment("we have received a complaint, we're closing down the post");
        postR = transactionTemplate.execute(status -> postApi.rejectPost(postId, postRejectDTO));
        post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postRejectDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.REJECTED,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE)));
        
        // Check that the administrator can restore the post
        testUserService.setAuthentication(bUser.getStormpathId());
        PostPatchDTO postRestoreFromRejectedDTO = (PostPatchDTO) new PostPatchDTO().setComment("sorry we made a mistake, we're restoring the post");
        postR = transactionTemplate.execute(status -> postApi.restorePost(postId, postRestoreFromRejectedDTO));
        post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postRestoreFromRejectedDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.ACCEPTED,
            new ExpectedUserActions()
                .add(Collections.singletonList(Action.VIEW), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.singletonList(Action.VIEW), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND)));
        
        transactionTemplate.execute(status -> {
            Post retiredPost = postService.getPost(postId);
            retiredPost.setDeadTimestamp(liveTimestampExtend.minusSeconds(1));
            return null;
        });
    
        // Check that the post now moves to the expired state when the update job runs
        verifyPublishAndRetirePost(postId, State.EXPIRED);
    
        verifyResourceActions(
            Scope.POST, postId, State.EXPIRED,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND)));
        
        // Check that the author can withdraw the post
        testUserService.setAuthentication(pUser.getStormpathId());
        PostPatchDTO postWithdrawDTO = (PostPatchDTO) new PostPatchDTO().setComment("this is rubbish, I'm withdrawing the post anyway");
        postR = transactionTemplate.execute(status -> postApi.withdrawPost(postId, postWithdrawDTO));
        post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postWithdrawDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.WITHDRAWN,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.RESTORE))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT)));
        
        // Check that the author can restore the post
        testUserService.setAuthentication(pUser.getStormpathId());
        PostPatchDTO postRestoreFromWithdrawnDTO = (PostPatchDTO) new PostPatchDTO().setComment("oh well, i'll give it one more chance");
        postR = transactionTemplate.execute(status -> postApi.restorePost(postId, postRestoreFromWithdrawnDTO));
        post = transactionTemplate.execute(status -> postService.getPost(postId));
        verifyPatchPost(post, postRestoreFromWithdrawnDTO, postR);
    
        verifyResourceActions(
            Scope.POST, postId, State.EXPIRED,
            new ExpectedUserActions()
                .add(Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .add(pUser, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW))
                .addAll(uUsers, Collections.emptyList(), () -> postApi.getPostOperations(postId))
                .addAll(aUsers, Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND)));
    
        testUserService.setAuthentication(pUser.getStormpathId());
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
            verifyApplyDocument(applyDocumentDTO, postR);
            
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
            return postR;
        });
    }
    
    private void verifyPatchPost(Post post, PostPatchDTO postDTO, PostRepresentation postR) {
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
        verifyApplyDocument(applyDocumentOptional == null ? post.getApplyDocument() : postDTO.getApplyDocument().orElse(null), postR);
        
        Optional<String> applyEmail = postDTO.getApplyEmail();
        assertEquals(applyEmail == null ? post.getApplyEmail() : applyEmail.orElse(null), postR.getApplyEmail());
        
        Optional<LocalDateTime> liveTimestampOptional = postDTO.getLiveTimestamp();
        assertEquals(liveTimestampOptional == null ? post.getLiveTimestamp() : liveTimestampOptional.orElse(null), postR.getLiveTimestamp());
        
        Optional<LocalDateTime> deadTimestampOptional = postDTO.getDeadTimestamp();
        assertEquals(deadTimestampOptional == null ? post.getDeadTimestamp() : deadTimestampOptional.orElse(null), postR.getDeadTimestamp());
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
    
    private void verifyLocation(LocationDefinition locationDefinition, PostRepresentation postR) {
        if (locationDefinition == null) {
            fail("Post location can never be set to null");
        }
        
        LocationRepresentation locationR = postR.getLocation();
        assertEquals(locationDefinition.getName(), locationR.getName());
        assertEquals(locationDefinition.getDomicile(), locationR.getDomicile());
        assertEquals(locationDefinition.getGoogleId(), locationR.getGoogleId());
        assertEquals(locationDefinition.getLatitude(), locationR.getLatitude());
        assertEquals(locationDefinition.getLongitude(), locationR.getLongitude());
    }
    
    private void verifyApplyDocument(DocumentDefinition applyDocumentDefinition, PostRepresentation postR) {
        if (applyDocumentDefinition == null) {
            assertNull(postR.getApplyDocument());
        } else {
            DocumentRepresentation applyDocumentR = postR.getApplyDocument();
            assertEquals(applyDocumentDefinition.getFileName(), applyDocumentR.getFileName());
            assertEquals(applyDocumentDefinition.getCloudinaryId(), applyDocumentR.getCloudinaryId());
            assertEquals(applyDocumentDefinition.getCloudinaryUrl(), applyDocumentR.getCloudinaryUrl());
        }
    }
    
}
