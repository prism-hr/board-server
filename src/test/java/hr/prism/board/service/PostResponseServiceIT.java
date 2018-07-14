package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.dao.PostResponseDAO;
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
import java.util.List;

import static hr.prism.board.enums.Action.EDIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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

    @SpyBean
    private ResourceEventService resourceEventService;

    @SpyBean
    private PostResponseDAO postResponseDAO;

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
        verify(resourceEventService, times(1)).getById(1L);
    }

    @Test
    public void getPostResponse_successWhenNotResourceEventUser() {
        User user = userService.getById(2L);
        Post post = (Post) resourceService.getById(3L);

        ResourceEvent event = postResponseService.getPostResponse(user, 3L, 1L);
        assertFalse(event.isExposeResponseData());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));
        verify(resourceEventService, times(1)).getById(1L);
    }

    @Test
    public void getPostResponses_success() {
        User user = userService.getById(1L);
        Post post = (Post) resourceService.getById(3L);

        List<ResourceEvent> responses =
            postResponseService.getPostResponses(user, 3L, null);

        ResourceEvent expectedResponse = new ResourceEvent();
        expectedResponse.setId(1L);

        assertThat(responses).containsExactly(expectedResponse);
        assertFalse(responses.get(0).isViewed());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));

        verify(postResponseDAO, times(1))
            .getPostResponses(user, post, null);
    }

    @Test
    public void getPostResponses_successWhenActivityViewed() {
        User user = userService.getById(2L);
        Post post = (Post) resourceService.getById(3L);

        List<ResourceEvent> responses =
            postResponseService.getPostResponses(user, 3L, null);

        ResourceEvent expectedResponse = new ResourceEvent();
        expectedResponse.setId(1L);

        assertThat(responses).containsExactly(expectedResponse);
        assertTrue(responses.get(0).isViewed());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));

        verify(postResponseDAO, times(1))
            .getPostResponses(user, post, null);
    }

    @Test
    public void getPostResponses_successWhenAllTokensMatch() {
        User user = userService.getById(1L);
        Post post = (Post) resourceService.getById(3L);

        List<ResourceEvent> responses =
            postResponseService.getPostResponses(user, 3L, "undergraduate 19-24");

        ResourceEvent expectedResponse = new ResourceEvent();
        expectedResponse.setId(1L);

        assertThat(responses).containsExactly(expectedResponse);
        assertFalse(responses.get(0).isViewed());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));

        verify(postResponseDAO, times(1))
            .getPostResponses(user, post, "undergraduate 19-24");
    }

    @Test
    public void getPostResponses_successWhenSomeTokensMatch() {
        User user = userService.getById(1L);
        Post post = (Post) resourceService.getById(3L);

        List<ResourceEvent> responses =
            postResponseService.getPostResponses(user, 3L, "master 19-24");

        ResourceEvent expectedResponse = new ResourceEvent();
        expectedResponse.setId(1L);

        assertThat(responses).containsExactly(expectedResponse);
        assertFalse(responses.get(0).isViewed());

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));

        verify(postResponseDAO, times(1))
            .getPostResponses(user, post, "master 19-24");
    }

    @Test
    public void getPostResponses_failureWhenNoTokensMatch() {
        User user = userService.getById(1L);
        Post post = (Post) resourceService.getById(3L);

        List<ResourceEvent> responses =
            postResponseService.getPostResponses(user, 3L, "master 25-29");

        assertThat(responses).isEmpty();

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(EDIT), any(Execution.class));

        verify(postResponseDAO, times(1))
            .getPostResponses(user, post, "master 25-29");
    }

    @Test
    public void consumePostReferral_success() {
        String redirect = postResponseService.consumePostReferral("referral");
        assertEquals("", redirect);
    }

}
