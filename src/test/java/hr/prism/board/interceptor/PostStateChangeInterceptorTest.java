package hr.prism.board.interceptor;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.State;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.State.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PostStateChangeInterceptorTest {

    private List<State> IGNORED_STATES = ImmutableList.of(DRAFT, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED);

    private List<State> INTERCEPTED_STATES = ImmutableList.of(PENDING, ACCEPTED, EXPIRED);

    private PostStateChangeInterceptor postStateChangeInterceptor = new PostStateChangeInterceptor();

    @Test
    public void intercept_successForIgnoredStates() {
        List<LocalDateTime> timestamps = asList(
            null, LocalDateTime.now().minusWeeks(1L), LocalDateTime.now().plusWeeks(1L));

        timestamps.forEach(liveTimestamp ->
            timestamps.forEach(deadTimestamp -> {
                Post post = new Post();
                post.setLiveTimestamp(liveTimestamp);
                post.setDeadTimestamp(deadTimestamp);

                IGNORED_STATES.forEach(state -> {
                    State newState = postStateChangeInterceptor.intercept(post, state);
                    assertEquals(state, newState);
                });
            }));
    }

    @Test
    public void intercept_successForInterceptedStates() {
        List<LocalDateTime> timestamps = asList(
            null, LocalDateTime.now().minusWeeks(1L), LocalDateTime.now().plusWeeks(1L));
        LocalDateTime baseline = LocalDateTime.now();

        timestamps.forEach(liveTimestamp ->
            timestamps.forEach(deadTimestamp -> {
                Post post = new Post();
                post.setLiveTimestamp(liveTimestamp);
                post.setDeadTimestamp(deadTimestamp);

                INTERCEPTED_STATES.forEach(state -> {
                    State newState = postStateChangeInterceptor.intercept(post, state);
                    if (deadTimestamp != null && deadTimestamp.isBefore(baseline)) {
                        assertEquals(EXPIRED, newState);
                    } else {
                        assertEquals(PENDING, newState);
                    }
                });
            }));
    }

}
