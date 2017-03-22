package hr.prism.board.api;

import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class PostApiIT {

    @Inject
    private PostApi postApi;

    @Inject
    private DepartmentBoardApi departmentBoardApi;

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

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    @Before
    public void setUp() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

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
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("scp")
                    .setPostCategories(new ArrayList<>()));
            return departmentBoardApi.postBoard(boardDTO);
        });

        PostRepresentation postR = transactionTemplate.execute(transactionStatus -> {
            PostDTO postDTO = new PostDTO()
                .setName("shouldCreatePost Post")
                .setDescription("Desc")
                .setOrganizationName("shouldCreatePost Organization")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldCreatePost GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(true)
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("shouldCreatePost CloudinaryId").setCloudinaryUrl("http://cloudinary.com"));

            PostRepresentation savedPostR = postApi.postPost(boardR.getId(), postDTO);
            savedPostR = postApi.getPost(savedPostR.getId());
            verifyPost(user, postDTO, savedPostR);
            return savedPostR;
        });

        transactionTemplate.execute(transactionStatus -> {
            Post post = postService.findOne(postR.getId());
            Board board = boardService.findOne(boardR.getId());
            Department department = departmentService.findOne(boardR.getDepartment().getId());

            // FIXME make sure
            Assert.assertThat(post.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(post, board));
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
                    .setMemberCategories(new ArrayList<>()))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("sup")
                    .setPostCategories(new ArrayList<>()));
            BoardRepresentation boardR = departmentBoardApi.postBoard(boardDTO);

            PostDTO postDTO = new PostDTO()
                .setName("shouldUpdatePost Post")
                .setDescription("Desc")
                .setOrganizationName("shouldUpdatePost Organization")
                .setLocation(new LocationDTO().setName("location1").setDomicile("PL")
                    .setGoogleId("shouldUpdatePost GoogleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))
                .setExistingRelation(true)
                .setApplyDocument(new DocumentDTO().setFileName("file1").setCloudinaryId("shouldUpdatePost CloudinaryId").setCloudinaryUrl("http://cloudinary.com"));

            PostRepresentation savedPostR = postApi.postPost(boardR.getId(), postDTO);
            postDTO.setName("shouldUpdatePost Board2").setDescription("Desc")
                .setOrganizationName("shouldUpdatePost Organization2")
                .setLocation(new LocationDTO().setName("location2").setDomicile("NG")
                    .setGoogleId("shouldUpdatePost GoogleId2").setLatitude(BigDecimal.TEN).setLongitude(BigDecimal.TEN))
                .setExistingRelation(false)
                .setApplyDocument(new DocumentDTO().setFileName("file2").setCloudinaryId("shouldUpdatePost CloudinaryId2").setCloudinaryUrl("http://cloudinary2.com"));
            postApi.updatePost(savedPostR.getId(), postDTO);
            PostRepresentation updatedPostR = postApi.getPost(savedPostR.getId());
            verifyPost(user, postDTO, updatedPostR);

            return savedPostR;
        });
    }

    private void verifyPost(User user, PostDTO postDTO, PostRepresentation postR) {
        Assert.assertEquals(postDTO.getName(), postR.getName());
        Assert.assertEquals(postDTO.getDescription(), postR.getDescription());
        Assert.assertEquals(postDTO.getOrganizationName(), postR.getOrganizationName());

        LocationRepresentation locationR = postR.getLocation();
        LocationDTO locationDTO = postDTO.getLocation();
        Assert.assertEquals(locationDTO.getName(), locationR.getName());
        Assert.assertEquals(locationDTO.getDomicile(), locationR.getDomicile());
        Assert.assertEquals(locationDTO.getGoogleId(), locationR.getGoogleId());
        Assert.assertThat(locationR.getLatitude(), Matchers.comparesEqualTo(locationDTO.getLatitude()));
        Assert.assertThat(locationR.getLongitude(), Matchers.comparesEqualTo(locationDTO.getLongitude()));

        Assert.assertEquals(postDTO.getExistingRelation(), postR.getExistingRelation());
        Assert.assertEquals(postDTO.getApplyWebsite(), postR.getApplyWebsite());

        DocumentRepresentation applyDocumentR = postR.getApplyDocument();
        DocumentDTO applyDocumentDTO = postDTO.getApplyDocument();
        Assert.assertEquals(applyDocumentDTO.getFileName(), applyDocumentR.getFileName());
        Assert.assertEquals(applyDocumentDTO.getCloudinaryId(), applyDocumentR.getCloudinaryId());
        Assert.assertEquals(applyDocumentDTO.getCloudinaryUrl(), applyDocumentR.getCloudinaryUrl());

        Assert.assertEquals(postDTO.getApplyEmail(), postR.getApplyEmail());

        Post post = postService.findOne(postR.getId());
        Assert.assertTrue(userRoleService.hasUserRole(post, user, Role.ADMINISTRATOR));

    }

}
