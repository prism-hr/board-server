package hr.prism.board.interceptor;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.enums.State;

import java.time.LocalDateTime;
import java.util.Arrays;

public class PostStateChangeInterceptor implements StateChangeInterceptor {
    
    @Override
    public State intercept(Resource resource, State state) {
        if (Arrays.asList(State.PENDING, State.EXPIRED, State.ACCEPTED).contains(state)) {
            Post post = (Post) resource;
            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime deadTimestamp = post.getDeadTimestamp();
            if (baseline.isBefore(post.getLiveTimestamp())) {
                return State.PENDING;
            } else if (deadTimestamp != null && baseline.isAfter(deadTimestamp)) {
                return State.EXPIRED;
            }
            
            return State.ACCEPTED;
        }
        
        return state;
    }
    
}
