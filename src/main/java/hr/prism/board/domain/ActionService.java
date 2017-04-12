package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.permission.ActionExecutionTemplate;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service
@Transactional
public class ActionService {

    @Inject
    private ResourceService resourceService;

    public List<ResourceAction> getActions(Resource resource) {
        Set<ResourceAction> resourceActions = resource.getResourceActions();
        if (resourceActions == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(resource.getResourceActions());
    }

    public Resource executeAction(User user, Resource resource, Action action, ActionExecutionTemplate executionTemplate) {
        Collection<ResourceAction> resourceActions = resource.getResourceActions();
        for (ResourceAction resourceAction : resourceActions) {
            if (resourceAction.getAction() == action) {
                resource = executionTemplate.executeWithAction();

                State state = resource.getState();
                State newState = resourceAction.getState();
                if (!(newState == null || state == newState)) {
                    resourceService.updateState(resource, newState);
                    return resourceService.getResource(user, resource.getScope(), resource.getId());
                }

                return resource;
            }
        }

        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }

}
