package hr.prism.board.authentication;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.ActionService;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceAction;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.ArrayUtils;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class RestrictionProcessor {
    
    @Inject
    private ActionService actionService;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserService userService;
    
    @Before("execution(@hr.prism.board.authentication.Restriction * *(..)) && @annotation(restriction)")
    public void processRestriction(JoinPoint joinPoint, Restriction restriction) {
        User user = userService.getCurrentUserSecured();
        Annotation[][] parameterAnnotations = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterAnnotations();
        if (ArrayUtils.isEmpty(parameterAnnotations)) {
            // TODO: will need to support filtering, ordering, paging
            user.setResourceActions(resourceService.getResourceActions(restriction.scope(), user.getId()));
            return;
        }
        
        Object[] arguments = joinPoint.getArgs();
        Map<String, Object> parameters = new HashMap<>();
        if (parameterAnnotations.length == arguments.length) {
            for (int i = 0; i < arguments.length; i++) {
                Annotation[] parameterAnnotationSet = parameterAnnotations[i];
                if (parameterAnnotationSet.length == 0) {
                    throw new IllegalStateException("Method declaration invalid");
                }
    
                Annotation parameterAnnotation = parameterAnnotationSet[0];
                Class<?> parameterAnnotationClass = parameterAnnotation.annotationType();
                if (parameterAnnotationClass == PathVariable.class) {
                    parameters.put(verifyArgumentName(((PathVariable) parameterAnnotation).value()), arguments[i]);
                } else if (parameterAnnotationClass == RequestParam.class) {
                    parameters.put(verifyArgumentName(((RequestParam) parameterAnnotation).value()), arguments[i]);
                }
            }
            
            Resource resource = null;
            boolean validRequest = false;
            if (parameters.containsKey("id")) {
                resource = resourceService.findOne((Long) parameters.get("id"));
                validRequest = true;
            } else if (parameters.containsKey("handle")) {
                resource = resourceService.findByHandle((String) parameters.get("handle"));
                validRequest = true;
            }
            
            if (validRequest) {
                if (resource == null) {
                    throw new IllegalStateException("Could not find " + restriction.scope().name().toLowerCase());
                }
    
                Long resourceId = resource.getId();
                List<Action> actions = Arrays.asList(restriction.actions());
                HashMultimap<Long, ResourceAction> resourceActions = resourceService.getResourceActions(resourceId, user.getId());
                if (resourceActions.isEmpty()) {
                    throw new ApiForbiddenException("User " + user.toString() + " cannot perform any actions for: " + resource.toString());
                }
    
                if (actions.isEmpty()) {
                    user.setResourceActions(resourceActions);
                    return;
                }
    
                List<Action> permittedActions = actionService.getActions(resource, user);
                if (!permittedActions.containsAll(actions)) {
                    throw new ApiForbiddenException("User " + user.toString() + " cannot perform the action(s): " +
                        actions.stream().map(action -> action.name().toLowerCase()).sorted().collect(Collectors.joining(", ")) + " for: " + resource.toString());
                }
    
                user.setResourceActions(resourceActions);
                return;
            }
        }
    }
    
    private String verifyArgumentName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalStateException("Argument declaration invalid");
        }
        
        return name;
    }
    
}
