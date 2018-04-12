package hr.prism.board.interceptor;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

import static hr.prism.board.enums.State.*;

@Component
public class PostStateChangeInterceptor implements StateChangeInterceptor {

    @Override
    public State intercept(User user, Resource resource, Action action, State state) {
        if (Arrays.asList(PENDING, EXPIRED, ACCEPTED).contains(state)) {
            Post post = (Post) resource;
            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime deadTimestamp = post.getDeadTimestamp();
            LocalDateTime liveTimestamp = post.getLiveTimestamp();
            if (liveTimestamp != null && baseline.isBefore(liveTimestamp)) {
                return PENDING;
            } else if (deadTimestamp != null && baseline.isAfter(deadTimestamp)) {
                return EXPIRED;
            }

            // If not currently accepted, send to pending to trigger notifications
            return resource.getState() == ACCEPTED ? ACCEPTED : PENDING;
        }

        return state;
    }

}
