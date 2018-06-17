package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.OrganizationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.service.PostService;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceFilter.ResourceFilterList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static hr.prism.board.enums.State.ACCEPTED;
import static java.math.BigDecimal.ONE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/api_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class PostApiIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ApiHelper apiHelper;

    @MockBean
    private PostService postService;

    @MockBean
    private PostMapper postMapper;

    private User user;

    private PostDTO postDTO;

    private Post post;

    private ResourceFilter filter;

    @Before
    public void setUp() {
        user = new User();
        user.setId(1L);

        postDTO =
            new PostDTO()
                .setName("post")
                .setOrganization(
                    new OrganizationDTO()
                        .setName("organization"))
                .setLocation(
                    new LocationDTO()
                        .setName("london")
                        .setDomicile("uk")
                        .setGoogleId("google")
                        .setLatitude(ONE)
                        .setLongitude(ONE));

        post = new Post();
        post.setId(4L);

        filter =
            new ResourceFilter()
                .setSearchTerm("search")
                .setState(ResourceFilterList.of(ACCEPTED));

        when(postService.createPost(user, 3L, postDTO)).thenReturn(post);
        when(postService.getPosts(user, filter)).thenReturn(singletonList(post));
        when(postService.getPosts(null, new ResourceFilter())).thenReturn(emptyList());
        when(postService.getById(any(User.class), eq(4L), any(String.class), eq(true))).thenReturn(post);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(postService, postMapper);
    }

    @Test
    public void createPost_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            post("/api/boards/3/posts")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization)
                .content(objectMapper.writeValueAsString(postDTO)))
            .andExpect(status().isOk());

        verify(postService, times(1)).createPost(user, 3L, postDTO);
        verify(postMapper, times(1)).apply(post);
    }

    @Test
    public void createPost_failureWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            post("/api/boards/3/posts")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(postDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPosts_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/posts?searchTerm=search&state=ACCEPTED")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(postService, times(1)).getPosts(user, filter);
        verify(postMapper, times(1)).apply(post);
    }

    @Test
    public void getPosts_successWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/posts")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(postService, times(1)).getPosts(null, new ResourceFilter());
    }

    @Test
    public void getPost_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/posts/4")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(postService, times(1))
            .getById(eq(user), eq(4L), any(String.class), eq(true));
        verify(postMapper, times(1)).apply(post);
    }

    @Test
    public void getPost_successWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/posts/4")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(postService, times(1))
            .getById(eq(null), eq(4L), any(String.class), eq(true));
        verify(postMapper, times(1)).apply(post);
    }

}
