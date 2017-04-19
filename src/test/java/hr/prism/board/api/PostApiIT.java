package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.LocationRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import hr.prism.board.service.UserTestService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
    private UserTestService userTestService;
    
    @Test
    public void shouldCreatePost() {
        User user = userTestService.authenticate();
        Long boardId = postBoard();
        
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
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L));
            
            PostRepresentation postR = postApi.postPost(boardId, postDTO);
            verifyPost(user, postDTO, postR);
            return null;
        });
    }
    
    @Test
    public void shouldUpdatePost() {
        userTestService.authenticate();
        Long boardId = postBoard();
        
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
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L)))
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
                .setApplyDocument(Optional.of(new DocumentDTO().setFileName("file2").setCloudinaryId("shouldUpdatePost CloudinaryId2").setCloudinaryUrl("http://cloudinary2.com")));
            PostRepresentation postR = postApi.updatePost(postId, postPatchDTO);
            verifyPost(postPatchDTO, postR);
            return null;
        });
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetPosts() {
        userTestService.authenticate();
        Long boardId = postBoard();
        
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
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L));
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
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L));
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
        userTestService.authenticate();
        Long boardId = postBoard();
        
        userTestService.authenticate();
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
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L));
            ExceptionUtil.verifyApiException(() -> postApi.postPost(boardId, postDTO), ExceptionCode.MISSING_POST_EXISTING_RELATION, status);
            return null;
        });
    }
    
    private Long postBoard() {
        return transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setPurpose("Purpose")
                .setPostCategories(ImmutableList.of("p1", "p2", "p3"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")));
            return boardApi.postBoard(boardDTO).getId();
        });
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
        assertEquals(postDTO.getLiveTimestamp(), postR.getLiveTimestamp());
        assertEquals(postDTO.getDeadTimestamp(), postR.getDeadTimestamp());
        assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.WITHDRAW, Action.SUSPEND, Action.REJECT));
    
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
    
}
