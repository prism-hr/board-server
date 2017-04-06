package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.permission.ActionExecutionTemplate;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActionService {
    
    @Inject
    private ResourceService resourceService;
    
    public List<Action> getActions(Resource resource) {
        Set<ResourceAction> resourceActions = resource.getResourceActions();
        if (resourceActions == null) {
            return Collections.emptyList();
        }
        
        return resource.getResourceActions().stream().map(ResourceAction::getAction).collect(Collectors.toList());
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
