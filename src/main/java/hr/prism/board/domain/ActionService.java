package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActionService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private ResourceService resourceService;
    
    public List<Action> getActions(Resource resource, User user) {
        Long resourceId = resource.getId();
        if (user.getResourceActions() == null) {
            // Lazily set the resource actions if not set somewhere else in the call chain
            user.setResourceActions(resourceService.getResourceActions(resourceId, user.getId()));
        }
    
        return user.getResourceActions().getActions(resourceId).stream().map(ResourceActions.ResourceAction::getAction).sorted().collect(Collectors.toList());
    }
    
    public List<Action> executeAction(Resource resource, User user, Action action) {
        Long resourceId = resource.getId();
        Collection<ResourceActions.ResourceAction> resourceActions = user.getResourceActions().getActions(resource.getId());
        for (ResourceActions.ResourceAction resourceAction : resourceActions) {
            if (resourceAction.getAction() == action) {
                State state = resource.getState();
                State newState = resourceAction.getState();
                if (!(newState == null || state == newState)) {
                    // Update resource actions when we change state
                    resourceService.updateState(resource, newState);
                    user.setResourceActions(resourceService.getResourceActions(resourceId, user.getId()));
                }
                
                return getActions(resource, user);
            }
        }
        
        // We need to throw here in case we forgot to attach the restriction processor to the controller
        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }
    
}
