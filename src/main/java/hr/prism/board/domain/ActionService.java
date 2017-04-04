package hr.prism.board.domain;

import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActionService {
    
    @Inject
    private ResourceService resourceService;
    
    public List<Action> getActions(Resource resource, User user) {
        return user.getResources().get(resource.getId()).getResourceActions().stream().map(ResourceAction::getAction).collect(Collectors.toList());
    }
    
    public List<Action> executeAction(Resource resource, User user, Action action) {
        Collection<ResourceAction> resourceActions = resource.getResourceActions();
        for (ResourceAction resourceAction : resourceActions) {
            if (resourceAction.getAction() == action) {
                State state = resource.getState();
                State newState = resourceAction.getState();
                if (!(newState == null || state == newState)) {
                    // Update resource actions when we change state
                    resourceService.updateState(resource, newState);
                    user.setResources(resourceService.getResources(new ResourceFilterDTO().setScope(resource.getScope()).setId(resource.getId()).setUserId(user.getId())));
                }
                
                return getActions(resource, user);
            }
        }
        
        // We need to throw here in case we forgot to attach the restriction processor to the controller
        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }
    
}
