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
    public State intercept(User user, Resource resource, Action action, State state) {
        if (Arrays.asList(State.PENDING, State.EXPIRED, State.ACCEPTED).contains(state)) {
            Post post = (Post) resource;
            if (state == State.ACCEPTED && post.getParent().getParent().getParent().getState() == State.REJECTED) {
                // If department has failed or cancelled subscription, send to pending
                return State.PENDING;
            }

            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime deadTimestamp = post.getDeadTimestamp();
            LocalDateTime liveTimestamp = post.getLiveTimestamp();
            if (liveTimestamp != null && baseline.isBefore(liveTimestamp)) {
                return State.PENDING;
            } else if (deadTimestamp != null && baseline.isAfter(deadTimestamp)) {
                return State.EXPIRED;
            }

            // If not currently accepted, send to pending to trigger notifications
            return resource.getState() == State.ACCEPTED ? State.ACCEPTED : State.PENDING;
        }

        return state;
    }

}
