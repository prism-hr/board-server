package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.State.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/boardService_setUp.sql")
@Sql(scripts = "classpath:data/boardService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class PostServiceIT {

    private static List<State> POST_STATES = ImmutableList.of(
        DRAFT, PENDING, ACCEPTED, EXPIRED, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED);

    @Inject
    private UserRepository userRepository;

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

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
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

}
