package hr.prism.board.authentication;

import hr.prism.board.domain.ActionService;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.UserService;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.ArrayUtils;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
    public void processRestriction(JoinPoint joinPoint, Restriction restriction) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        User user = userService.getCurrentUserSecured();
        Annotation[][] parameterAnnotations = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterAnnotations();
        if (ArrayUtils.isEmpty(parameterAnnotations)) {
            parameterAnnotations = new Annotation[][]{};
        }
    
        Object[] arguments = joinPoint.getArgs();
        ResourceFilterDTO resourceFilterDTO = new ResourceFilterDTO().setUserId(user.getId());
        if (parameterAnnotations.length == arguments.length) {
            for (int i = 0; i < arguments.length; i++) {
                Annotation[] parameterAnnotationSet = parameterAnnotations[i];
                if (parameterAnnotationSet.length == 0) {
                    throw new IllegalStateException("Method declaration invalid");
                }
    
                Annotation parameterAnnotation = parameterAnnotationSet[0];
                Class<?> parameterAnnotationClass = parameterAnnotation.annotationType();
                if (parameterAnnotationClass == PathVariable.class) {
                    PropertyUtils.setProperty(resourceFilterDTO, verifyArgumentName(((PathVariable) parameterAnnotation).value()), arguments[i]);
                } else if (parameterAnnotationClass == RequestParam.class) {
                    PropertyUtils.setProperty(resourceFilterDTO, verifyArgumentName(((RequestParam) parameterAnnotation).value()), arguments[i]);
                }
            }
    
            List<Action> actions = Arrays.asList(restriction.actions());
            Map<Long, Resource> resources = resourceService.getResources(resourceFilterDTO);
            if (CollectionUtils.isEmpty(actions)) {
                user.setResources(resources);
                return;
            }
    
            Resource resource = resources.get(resourceFilterDTO.getId());
            if (resource == null) {
                throw new ApiForbiddenException("User " + user.toString() + " cannot perform any actions for: " + resource.toString());
            }
    
            List<Action> permittedActions = actionService.getActions(resource, user);
            if (!permittedActions.containsAll(actions)) {
                throw new ApiForbiddenException("User " + user.toString() + " cannot perform the action(s): " +
                    actions.stream().map(action -> action.name().toLowerCase()).sorted().collect(Collectors.joining(", ")) + " for: " + resource.toString());
            }
    
            user.setResources(resources);
            return;
        }
    
        throw new IllegalStateException("Method declaration invalid");
    }
    
    private String verifyArgumentName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalStateException("Argument declaration invalid");
        }
        
        return name;
    }
    
}
