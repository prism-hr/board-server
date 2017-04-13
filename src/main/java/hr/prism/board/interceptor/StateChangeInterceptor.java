package hr.prism.board.interceptor;

import hr.prism.board.domain.Resource;
import hr.prism.board.enums.State;

public interface StateChangeInterceptor {
    
    State intercept(Resource resource, State state);
    
}
