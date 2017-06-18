package hr.prism.board.interceptor;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

public interface StateChangeInterceptor {

    State intercept(User user, Resource resource, State state, Action action);

}
