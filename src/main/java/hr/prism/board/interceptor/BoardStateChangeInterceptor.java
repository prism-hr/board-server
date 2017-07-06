package hr.prism.board.interceptor;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import hr.prism.board.service.UserRoleService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BoardStateChangeInterceptor implements StateChangeInterceptor {

    @Inject
    private UserRoleService userRoleService;

    @Override
    public State intercept(User user, Resource resource, Action action, State state) {
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
