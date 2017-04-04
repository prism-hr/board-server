package hr.prism.board.domain;

import com.google.common.collect.HashMultimap;
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
        Long resourceId = resource.getId();
        HashMultimap<Long, ResourceAction> resourceActions = user.getResources();
        if (resourceActions == null || !resourceActions.containsKey(resourceId)) {
            // Lazily set the resource actions if not set somewhere else in the call chain
            user.setResources(resourceService.getResources(resourceId, user.getId()));
        }
    
        return user.getResources().get(resourceId).stream().map(ResourceAction::getAction).sorted().collect(Collectors.toList());
    }
    
    public List<Action> executeAction(Resource resource, User user, Action action) {
        Long resourceId = resource.getId();
        Collection<ResourceAction> resourceActions = user.getResources().get(resource.getId());
        for (ResourceAction resourceAction : resourceActions) {
            if (resourceAction.getAction() == action) {
                State state = resource.getState();
                State newState = resourceAction.getState();
                if (!(newState == null || state == newState)) {
                    // Update resource actions when we change state
                    resourceService.updateState(resource, newState);
                    user.setResources(resourceService.getResources(resourceId, user.getId()));
                }
                
                return getActions(resource, user);
            }
        }
        
        // We need to throw here in case we forgot to attach the restriction processor to the controller
        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }
    
}
