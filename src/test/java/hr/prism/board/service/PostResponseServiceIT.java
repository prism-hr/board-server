package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.workflow.Execution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.Action.EDIT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/postResponseService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class PostResponseServiceIT {

    @Inject
    private PostResponseService postResponseService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @SpyBean
    private ActionService actionService;

    public void tearDown() {
        reset(actionService);
    }

    @Test
    public void getPostResponse_successWhenResourceEventUser() {
        User user = userService.getById(1L);
        Post post = (Post) resourceService.getById(3L);

        ResourceEvent event = postResponseService.getPostResponse(user, 3L, 1L);
        assertTrue(event.isExposeResponseData());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));
    }

    @Test
    public void getPostResponse_successWhenNotResourceEventUser() {
        User user = userService.getById(2L);
        Post post = (Post) resourceService.getById(3L);

        ResourceEvent event = postResponseService.getPostResponse(user, 3L, 1L);
        assertFalse(event.isExposeResponseData());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));
    }

}
