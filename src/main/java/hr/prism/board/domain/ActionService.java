package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ActionService {

    @Inject
    private UserRoleService userRoleService;

    public List<Action> getActions(Resource resource, User user) {
        List<Action> actions = new LinkedList<>();
        HashSet<Role> parentRoles = new HashSet<>(userRoleService.findParentRolesByResourceAndUser(resource, user));
        HashSet<Role> roles = new HashSet<>(userRoleService.findByResourceAndUser(resource, user));
        HashSet<Role> allRoles = Stream.concat(parentRoles.stream(), roles.stream()).collect(Collectors.toCollection(HashSet::new));

        if (allRoles.contains(Role.ADMINISTRATOR)) {
            actions.add(Action.EDIT);
            actions.add(Action.CORRECT);
        }
        if (parentRoles.contains(Role.ADMINISTRATOR)) {
            actions.add(Action.APPROVE);
            actions.add(Action.REJECT);
            actions.add(Action.REQUEST_CORRECTION);
        }
        List<Action> availableActions = getAvailableActions(resource);
        return actions.stream().filter(availableActions::contains).collect(Collectors.toList());
    }

    public void executeAction(Resource resource, User user, Action action) {
        List<Action> actions = getActions(resource, user);
        if (!actions.contains(action)) {
            throw new ApiForbiddenException("Cannot perform action");
        }

        switch (action) {
            case APPROVE:
                resource.setState(State.ACCEPTED);
                break;
            case REJECT:
                resource.setState(State.REJECTED);
                break;
            default:
        }
    }

    private List<Action> getAvailableActions(Resource resource) {
        List<Action> actions = new LinkedList<>();
        actions.add(Action.EDIT);
        if (resource.getScope() == Scope.POST) {
            if (resource.getState() == State.CORRECTION) {
                actions.add(Action.CORRECT);
            } else if (resource.getState() == State.DRAFT) {
                actions.add(Action.APPROVE);
                actions.add(Action.REJECT);
                actions.add(Action.REQUEST_CORRECTION);
            }
        }
        return actions;
    }
}
