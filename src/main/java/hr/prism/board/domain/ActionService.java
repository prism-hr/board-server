package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.permission.ActionExecutionTemplate;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
public class ActionService {
    
    @Inject
    private ResourceService resourceService;
    
    public Resource executeAction(User user, Resource resource, Action action, ActionExecutionTemplate executionTemplate) {
        List<ActionRepresentation> actions = resource.getActions();
        for (ActionRepresentation actionRepresentation : actions) {
            if (actionRepresentation.getAction() == action) {
                resource = executionTemplate.executeWithAction();
                
                State state = resource.getState();
                State newState = actionRepresentation.getState();
                if (!(newState == null || state == newState)) {
                    if (newState == State.PREVIOUS) {
                        newState = resource.getPreviousState();
                    }
                    
                    resourceService.updateState(resource, newState);
                    return resourceService.getResource(user, resource.getScope(), resource.getId());
                }
                
                return resource;
            }
        }
        
        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }
    
}
