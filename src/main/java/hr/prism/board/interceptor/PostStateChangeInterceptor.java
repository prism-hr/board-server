package hr.prism.board.interceptor;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.enums.State;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.State.*;

@Component
public class PostStateChangeInterceptor implements StateChangeInterceptor {

    private static final List<State> PUBLICATION_STATES = ImmutableList.of(PENDING, ACCEPTED, EXPIRED);

    @Override
    public State intercept(Resource resource, State state) {
        if (PUBLICATION_STATES.contains(state)) {
            Post post = (Post) resource;
            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime deadTimestamp = post.getDeadTimestamp();
            LocalDateTime liveTimestamp = post.getLiveTimestamp();
            if (deadTimestamp != null && baseline.isAfter(deadTimestamp)) {
                return EXPIRED;
            } else if (liveTimestamp != null && baseline.isBefore(liveTimestamp)) {
                return PENDING;
            }

            // If not currently accepted, send to pending to trigger notifications
            return resource.getState() == ACCEPTED ? ACCEPTED : PENDING;
        }

        return state;
    }

}
