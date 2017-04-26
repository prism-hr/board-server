package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.*;
import hr.prism.board.service.*;
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
    
    @Inject
    private UserService userService;
    
    @Inject
    private TestHelper testHelper;
    
    @Test
    public void shouldCreatePost() {
        Long boardId = postBoard("department@poczta.fm").getId();
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("Post")
                .setDescription("Description")
                .setOrganizationName("Organization Name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(ImmutableList.of("p1", "p3"))
                .setMemberCategories(ImmutableList.of("m1", "m3"))
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("CloudinaryId").setCloudinaryUrl("http://cloudinary.com"))
                .setLiveTimestamp(LocalDateTime.now().plus(1, ChronoUnit.MONTHS))
                .setDeadTimestamp(LocalDateTime.now().plus(1, ChronoUnit.YEARS));
    
            PostRepresentation postR = postApi.postPost(boardId, postDTO);
            User user = userService.findByEmail("department@poczta.fm");
            verifyPost(user, postDTO, postR);
            return null;
        });
    }
    
    @Test
    public void shouldUpdatePost() {
        Long boardId = postBoard("department@poczta.fm").getId();
        Long postId = transactionTemplate.execute(status -> postApi.postPost(boardId,
            new PostDTO()
                .setName("New Post")
                .setDescription("Description")
                .setOrganizationName("Organization Name")
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
    @SuppressWarnings("unchecked")
    public void shouldGetPosts() {
        Long boardId = postBoard("department@poczta.fm").getId();
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("Post 1")
                .setDescription("Description")
                .setOrganizationName("Organization Name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldGetPosts GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(new ArrayList<>())
                .setMemberCategories(new ArrayList<>())
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("CloudinaryId").setCloudinaryUrl("http://cloudinary.com"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            postApi.postPost(boardId, postDTO);
            return null;
        });
        
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("Post 2")
                .setDescription("Description")
                .setOrganizationName("Organization Name")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldGetPosts GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(ExistingRelation.STUDENT)
                .setPostCategories(new ArrayList<>())
                .setMemberCategories(new ArrayList<>())
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("CloudinaryId").setCloudinaryUrl("http://cloudinary.com"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            postApi.postPost(boardId, postDTO);
            return null;
        });
        
        transactionTemplate.execute(status -> {
            List<PostRepresentation> posts = postApi.getPostsByBoard(boardId);
            assertThat(posts, contains(hasProperty("name", equalTo("Post 2")),
                hasProperty("name", equalTo("Post 1"))));
            return null;
        });
    }
    
    @Test
    public void shouldNotAcceptPostWithMissingRelationDescriptionForUserWithoutAuthorRole() {
        Long boardId = postBoard("department@poczta.fm").getId();
        
        testUserService.authenticate();
        transactionTemplate.execute(status -> {
            PostDTO postDTO = new PostDTO()
                .setName("Post")
                .setDescription("Description")
                .setOrganizationName("Organization")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(new ArrayList<>())
                .setMemberCategories(new ArrayList<>())
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("cloudinaryId").setCloudinaryUrl("http://cloudinary.com"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
            ExceptionUtil.verifyApiException(ApiException.class, () -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION, status);
            return null;
        });
    }
    
    @Test
    public void shouldDepartmentUserBeAbleToAcceptPost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
        
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        testUserService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.acceptPost(postId, new PostPatchDTO().setDescription(Optional.of("Corrected desc")));
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.ACCEPTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.ACCEPTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.REJECT, Action.SUSPEND));
        
        transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.getPost(postId);
            assertEquals("Corrected desc", postR.getDescription());
            return null;
        });
    }
    
    @Test
    public void shouldPosterBeAbleToCorrectPost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
        
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        testUserService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.suspendPost(post.getId(), new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.SUSPENDED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT),
            Arrays.asList(Action.CORRECT, Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.SUSPENDED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.CORRECT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT));
        
        testUserService.authenticateAs("poster@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.correctPost(postId, new PostPatchDTO().setName(Optional.of("Corrected name")));
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.getPost(postId);
            assertEquals("Corrected name", postR.getName());
            return null;
        });
    }
    
    @Test
    public void shouldDepartmentUserBeAbleToRejectAndRestorePost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
        
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        testUserService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.rejectPost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.REJECTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.REJECTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.SUSPEND, Action.RESTORE));
        
        testUserService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.restorePost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
    }
    
    @Test
    public void shouldPosterBeAbleToWithdrawAndRestorePost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
        
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        testUserService.authenticateAs("poster@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.withdrawPost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.WITHDRAWN,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT),
            Collections.singletonList(Action.RESTORE));
        verifyPost(postId, "poster@poczta.fm", State.WITHDRAWN,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.RESTORE),
            Collections.emptyList());
        
        testUserService.authenticateAs("poster@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.restorePost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Collections.singletonList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.AUDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
    }
    
    @Test
    public void shouldNotBeAbleToCorruptPostByPatching() {
        Long boardId = postBoard("department@poczta.fm").getId();
        PostRepresentation postRepresentation = postPost(boardId, "poster@poczta.fm");
        Long postId = postRepresentation.getId();
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO().setName(Optional.empty())),
                ExceptionCode.MISSING_POST_NAME, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId, new PostPatchDTO().setName(Optional.of("name")).setDescription(Optional.empty())),
                ExceptionCode.MISSING_POST_DESCRIPTION, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId,
                        new PostPatchDTO().setName(Optional.of("name")).setDescription(Optional.of("description")).setOrganizationName(Optional.empty())),
                ExceptionCode.MISSING_POST_ORGANIZATION_NAME, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId,
                        new PostPatchDTO()
                            .setName(Optional.of("name"))
                            .setDescription(Optional.of("description"))
                            .setOrganizationName(Optional.of("organization name"))
                            .setLocation(Optional.empty())),
                ExceptionCode.MISSING_POST_LOCATION, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    postApi.updatePost(postId,
                        new PostPatchDTO()
                            .setName(Optional.of("name"))
                            .setDescription(Optional.of("description"))
                            .setOrganizationName(Optional.of("organization name"))
                            .setLocation(Optional.of(new LocationDTO().setName("name").setDomicile("PL")
                                .setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE)))
                            .setApplyWebsite(Optional.empty())
                            .setApplyEmail(Optional.empty())
                            .setApplyDocument(Optional.empty())),
                ExceptionCode.MISSING_POST_APPLY, null);
            status.setRollbackOnly();
            return null;
        });
    }
    
    private BoardRepresentation postBoard(String user) {
        testUserService.authenticateAs(user);
        return transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("p1", "p2", "p3"))
                .setDepartment(new DepartmentDTO()
                    .setName("Department")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")));
            return boardApi.postBoard(boardDTO);
        });
    }
    
    private PostRepresentation postPost(Long boardId, String user) {
        testUserService.authenticateAs(user);
        return transactionTemplate.execute(status -> postApi.postPost(boardId, TestHelper.samplePost()));
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
        assertThat(postR.getPostCategories(), containsInAnyOrder(postDTO.getPostCategories().toArray()));
        assertThat(postR.getMemberCategories(), containsInAnyOrder(postDTO.getMemberCategories().toArray()));
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
        assertThat(locationR.getLatitude(), Matchers.comparesEqualTo(locationDTO.getLatitude()));
        assertThat(locationR.getLongitude(), Matchers.comparesEqualTo(locationDTO.getLongitude()));
        
        assertThat(postR.getPostCategories(), containsInAnyOrder(postDTO.getPostCategories().orElse(null).toArray()));
        assertThat(postR.getMemberCategories(), containsInAnyOrder(postDTO.getMemberCategories().orElse(null).toArray()));
        assertEquals(postDTO.getApplyWebsite() != null ? postDTO.getApplyWebsite().orElse(null) : null, postR.getApplyWebsite());
        
        DocumentRepresentation applyDocumentR = postR.getApplyDocument();
        DocumentDTO applyDocumentDTO = postDTO.getApplyDocument().orElse(null);
        assertEquals(applyDocumentDTO.getFileName(), applyDocumentR.getFileName());
        assertEquals(applyDocumentDTO.getCloudinaryId(), applyDocumentR.getCloudinaryId());
        assertEquals(applyDocumentDTO.getCloudinaryUrl(), applyDocumentR.getCloudinaryUrl());
        
        assertEquals(postDTO.getApplyEmail() != null ? postDTO.getApplyEmail().orElse(null) : null, postR.getApplyEmail());
    }
    
    private void verifyPost(Long postId, String username, State state, Collection<Action> actions, Collection<Action> forbiddenActions) {
        User user = testUserService.authenticateAs(username);
        transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.getPost(postId);
            assertEquals(state, postR.getState());
            assertThat(postR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()), Matchers.containsInAnyOrder(actions.toArray(new Action[0])));
            return null;
        });
        
        Post post = postService.getPost(postId);
        for (Action forbiddenAction : forbiddenActions) {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> actionService.executeAction(user, post, forbiddenAction, null),
                ExceptionCode.FORBIDDEN_ACTION, null);
        }
    }
    
}
