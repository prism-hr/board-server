package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
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
import java.util.ArrayList;
import java.util.List;
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
    public void shouldCreatePostWithAllPossibleFieldsSet() {
        User user = userTestService.authenticate();
        
        BoardRepresentation boardR = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldCreatePost Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldCreatePost Department")
                    .setHandle("scp")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("scp")
                    .setPostCategories(ImmutableList.of("p1", "p2", "p3")));
            return boardApi.postBoard(boardDTO);
        });
        
        transactionTemplate.execute(transactionStatus -> {
            PostDTO postDTO = new PostDTO()
                .setName("shouldCreatePost Post")
                .setDescription("Desc")
                .setOrganizationName("shouldCreatePost Organization")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldCreatePost GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation("Description")
                .setPostCategories(ImmutableList.of("p1", "p3"))
                .setMemberCategories(ImmutableList.of("m1", "m3"))
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("shouldCreatePost CloudinaryId").setCloudinaryUrl("http://cloudinary.com"));
    
            PostRepresentation postR = postApi.postPost(boardR.getId(), postDTO);
            postR = postApi.getPost(postR.getId());
            verifyPost(user, postDTO, postR);
            return null;
        });
    }
    
    @Test
    public void shouldUpdatePost() {
        User user = userTestService.authenticate();
        
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldUpdatePost Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldUpdatePost Department")
                    .setHandle("sup")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sup")
                    .setPostCategories(ImmutableList.of("p1", "p2", "p3")));
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            
            PostDTO postDTO = new PostDTO()
                .setName("shouldUpdatePost Post")
                .setDescription("Desc")
                .setOrganizationName("shouldUpdatePost Organization")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldUpdatePost GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation("Description")
                .setPostCategories(ImmutableList.of("p1", "p3"))
                .setMemberCategories(ImmutableList.of("m1", "m3"))
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("shouldUpdatePost CloudinaryId").setCloudinaryUrl("http://cloudinary.com"));
    
            PostRepresentation postR = postApi.postPost(boardR.getId(), postDTO);
            postDTO.setName("shouldUpdatePost Board2")
                .setDescription("Desc")
                .setOrganizationName("shouldUpdatePost Organization2")
                .setLocation(new LocationDTO().setName("location2").setDomicile("NG")
                    .setGoogleId("shouldUpdatePost GoogleId2").setLatitude(BigDecimal.TEN).setLongitude(BigDecimal.TEN))
                .setPostCategories(ImmutableList.of("p2", "p3"))
                .setMemberCategories(ImmutableList.of("m2", "m3"))
                .setApplyDocument(new DocumentDTO().setFileName("file2").setCloudinaryId("shouldUpdatePost CloudinaryId2").setCloudinaryUrl("http://cloudinary2.com"));
            postApi.updatePost(postR.getId(), postDTO);
            postR = postApi.getPost(postR.getId());
            verifyPost(user, postDTO, postR);
    
            return postR;
        });
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetPosts() {
        userTestService.authenticate();
        
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldGetPosts Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("shouldGetPosts Department")
                    .setHandle("sgp")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sgp")
                    .setPostCategories(ImmutableList.of("p1", "p2", "p3")));
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            
            PostDTO postDTO = new PostDTO()
                .setName("shouldGetPosts Post1")
                .setDescription("Desc")
                .setOrganizationName("shouldGetPosts Organization")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldGetPosts GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation("Description")
                .setPostCategories(new ArrayList<>())
                .setMemberCategories(new ArrayList<>())
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("shouldGetPosts CloudinaryId").setCloudinaryUrl("http://cloudinary.com"));
            postApi.postPost(boardR.getId(), postDTO);
            
            PostDTO postDTO2 = new PostDTO()
                .setName("shouldGetPosts Post2")
                .setDescription("Desc2")
                .setOrganizationName("shouldGetPosts Organization")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldGetPosts GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation("Description")
                .setPostCategories(new ArrayList<>())
                .setMemberCategories(new ArrayList<>())
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("shouldGetPosts CloudinaryId").setCloudinaryUrl("http://cloudinary.com"));
            postApi.postPost(boardR.getId(), postDTO2);
    
            List<PostRepresentation> posts = postApi.getPostsByBoard(boardR.getId());
            assertThat(posts, contains(hasProperty("name", equalTo("shouldGetPosts Post2")),
                hasProperty("name", equalTo("shouldGetPosts Post1"))));
            return null;
        });
    }
    
    @Test
    public void shouldNotAcceptPostWithMissingRelationDescriptionForUserWithoutContributorRole() {
        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticate();
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("Department")
                    .setHandle("handle")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("handle")
                    .setPostCategories(ImmutableList.of("p1", "p2", "p3")));
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
    
            userTestService.authenticate();
            PostDTO postDTO = new PostDTO()
                .setName("Post")
                .setDescription("Description")
                .setOrganizationName("Organization")
                .setLocation(new LocationDTO().setName("location").setDomicile("PL")
                    .setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setPostCategories(new ArrayList<>())
                .setMemberCategories(new ArrayList<>())
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("cloudinaryId").setCloudinaryUrl("http://cloudinary.com"));
            ExceptionUtil.verifyApiException(() -> postApi.postPost(boardR.getId(), postDTO), ExceptionCode.MISSING_RELATION_DESCRIPTION, transactionStatus);
            return null;
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
        assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.WITHDRAW, Action.SUSPEND, Action.REJECT));
        
        Post post = postService.getPost(postR.getId());
        Assert.assertTrue(userRoleService.hasUserRole(post, user, Role.ADMINISTRATOR));
        
        Board board = boardService.getBoard(postR.getBoard().getId());
        Department department = departmentService.getDepartment(postR.getBoard().getDepartment().getId());
        assertThat(post.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(post, board, department));
    }
    
}
