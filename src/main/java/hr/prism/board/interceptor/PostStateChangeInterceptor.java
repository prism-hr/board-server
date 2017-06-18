package hr.prism.board.interceptor;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class PostStateChangeInterceptor implements StateChangeInterceptor {

    @Override
    public State intercept(User user, Resource resource, State state, Action action) {
        if (Arrays.asList(State.PENDING, State.EXPIRED, State.ACCEPTED).contains(state)) {
            Post post = (Post) resource;
            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime deadTimestamp = post.getDeadTimestamp();
            LocalDateTime liveTimestamp = post.getLiveTimestamp();
            if (liveTimestamp != null && baseline.isBefore(liveTimestamp)) {
                return State.PENDING;
            } else if (deadTimestamp != null && baseline.isAfter(deadTimestamp)) {
                return State.EXPIRED;
            }

            return State.ACCEPTED;
        }

        return state;
    }

}
