package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.enums.State;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.service.ServiceHelper.Scenarios;
import hr.prism.board.value.ResourceFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.State.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/boardService_setUp.sql")
@Sql(scripts = "classpath:data/boardService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class PostServiceIT {

    private static List<State> POST_STATES = ImmutableList.of(
        DRAFT, PENDING, ACCEPTED, EXPIRED, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED);

    @Inject
    private PostRepository postRepository;

    @Inject
    private PostService postService;

    @Inject
    private BoardService boardService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private ServiceHelper serviceHelper;

    @SpyBean
    private ActionService actionService;

    @SpyBean
    private ResourceService resourceService;

    private LocalDateTime baseline;

    private User administrator;

    private User otherAdministrator;

    private User author;

    private University university;

    private Department departmentAccepted;

    private Department departmentRejected;

    private List<Post> departmentAcceptedPosts;

    private List<Post> departmentRejectedPosts;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();

        administrator = serviceHelper.setUpUser();
        otherAdministrator = serviceHelper.setUpUser();
        author = serviceHelper.setUpUser();

        university = serviceHelper.setUpUniversity("university");

        departmentAccepted =
            serviceHelper.setUpDepartment(administrator, university, "department ACCEPTED", ACCEPTED);

        departmentRejected =
            serviceHelper.setUpDepartment(administrator, university, "department REJECTED", REJECTED);
        userRoleService.createUserRole(departmentRejected, otherAdministrator, ADMINISTRATOR);

        List<Board> departmentAcceptedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentAccepted.getId()));
        resourceService.updateState(departmentAcceptedBoards.get(1), REJECTED);

        List<Board> departmentRejectedBoards =
            boardService.getBoards(administrator, new ResourceFilter().setParentId(departmentRejected.getId()));
        resourceService.updateState(departmentRejectedBoards.get(1), REJECTED);

        Stream.of(departmentAcceptedBoards, departmentRejectedBoards).forEach(boards ->
            boards.forEach(board -> {
                POST_STATES.forEach(state -> {
                    serviceHelper.setUpPost(administrator, board, "post " + author.getId(), state);
                    serviceHelper.setUpPost(author, board, "post " + author.getId(), state);
                });
            }));

        departmentAcceptedPosts = postService.getPosts(administrator, departmentAccepted.getId());
        departmentRejectedPosts = postService.getPosts(administrator, departmentRejected.getId());
        reset(actionService, resourceService);
    }

    @After
    public void tearDown() {
        reset(actionService, resourceService);
    }

    @Test
    public void createPost_successWhenApplyWebsite() {

    }

    @Test
    public void createPost_successWhenApplyDocument() {

    }

    @Test
    public void createPost_successWhenApplyEmail() {

    }

    @Test
    public void getPosts_successWhenUnprivilegedUser() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentAccepted, AUTHOR))
            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentRejected, AUTHOR));
    }

}
