package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Operation;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.interceptor.StateChangeInterceptor;
import hr.prism.board.permission.ActionExecutionTemplate;
import hr.prism.board.repository.OperationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.representation.ActionRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
public class ActionService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionService.class);
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private ResourceRepository resourceRepository;
    
    @Inject
    private OperationRepository operationRepository;
    
    @Inject
    private ObjectMapper objectMapper;
    
    public Resource executeAction(User user, Resource resource, Action action, ActionExecutionTemplate executionTemplate) {
        List<ActionRepresentation> actions = resource.getActions();
        for (ActionRepresentation actionRepresentation : actions) {
            if (actionRepresentation.getAction() == action) {
                resource = executionTemplate.executeWithAction();
                
                State state = resource.getState();
                State newState = actionRepresentation.getState();
                if (newState == null) {
                    newState = state;
                } else if (newState == state.PREVIOUS) {
                    newState = resource.getPreviousState();
                }
    
                Class<? extends StateChangeInterceptor> interceptorClass = resource.getScope().stateChangeInterceptorClass;
                if (interceptorClass != null) {
                    newState = BeanUtils.instantiate(interceptorClass).intercept(resource, newState);
                }
    
                if (state != newState) {
                    resourceService.updateState(resource, newState);
                    return resourceService.getResource(user, resource.getScope(), resource.getId());
                }
    
                recordOperation(resource, action, user);
                return resource;
            }
        }
        
        throw new ApiForbiddenException(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
    }
    
    // TODO: Generate comment and change list
    private void recordOperation(Resource resource, Action action, User user) {
        Operation operation = new Operation().setResource(resource).setAction(action).setUser(user).setComment(resource.getComment());
        
        List<String> changeList = resource.getChangeList();
        if (changeList != null) {
            try {
                operation.setChangeList(objectMapper.writeValueAsString(changeList));
            } catch (JsonProcessingException e) {
                LOGGER.info("Could not serialize change list", e);
            }
        }
        
        operation = operationRepository.save(operation);
        resource.getOperations().add(operation);
        resourceRepository.update(resource);
    }
    
}
