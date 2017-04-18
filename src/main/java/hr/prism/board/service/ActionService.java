package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.interceptor.StateChangeInterceptor;
import hr.prism.board.permission.ActionExecutionTemplate;
import hr.prism.board.representation.ActionRepresentation;
import org.springframework.beans.BeanUtils;
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
                if (newState == null) {
                    newState = state;
                } else if (newState == State.PREVIOUS) {
                    newState = resource.getPreviousState();
                }
    
                Class<? extends StateChangeInterceptor> interceptorClass = resource.getScope().stateChangeInterceptorClass;
                if (interceptorClass != null) {
                    newState = BeanUtils.instantiate(interceptorClass).intercept(resource, newState);
                }
    
                if (state != newState) {
                    resourceService.updateState(resource, newState);
                    resource = resourceService.getResource(user, resource.getScope(), resource.getId());
                }
    
                ResourceOperation resourceOperation = resourceService.createResourceOperation(resource, action, user);
                resourceService.updateResource(resource, resourceOperation.getUpdatedTimestamp());
                return resource;
            }
        }
        
        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }
    
}
