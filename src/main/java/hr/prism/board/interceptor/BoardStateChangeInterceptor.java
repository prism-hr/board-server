package hr.prism.board.interceptor;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.service.UserRoleService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BoardStateChangeInterceptor implements StateChangeInterceptor {

    @Inject
    private UserRoleService userRoleService;

    @Override
    public State intercept(User user, Resource resource, State state, Action action) {
        if (action == Action.EXTEND) {
            Board board = (Board) resource;
            Department department = (Department) board.getParent();
            if (userRoleService.findbyResourceAndUserAndRole(department, user, Role.ADMINISTRATOR) == null) {
                return State.DRAFT;
            }

            return State.ACCEPTED;
        }

        return state;
    }

}
