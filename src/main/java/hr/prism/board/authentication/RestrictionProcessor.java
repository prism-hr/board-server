package hr.prism.board.authentication;

import com.google.common.base.Joiner;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRoleService;
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
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class RestrictionProcessor {
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private UserService userService;
    
    @Before("execution(@hr.prism.board.authentication.Restriction * *(..)) && @annotation(restriction)")
    public void processRestriction(JoinPoint joinPoint, Restriction restriction) {
        User user = userService.getCurrentUserSecured();
        Annotation[][] parameterAnnotations = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterAnnotations();
        if (ArrayUtils.isEmpty(parameterAnnotations)) {
            Scope scope = restriction.scope();
            if (userRoleService.hasUserRole(scope, user)) {
                return;
            }
    
            throw new ApiForbiddenException("User has no " + scope.name().toLowerCase() + " roles");
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
                
                if (userRoleService.hasUserRole(resource, user, restriction.roles())) {
                    return;
                }
    
                throw new ApiForbiddenException("User " + user.toString() + " does not have role(s): " +
                    Joiner.on(", ").join(restriction.roles()).toLowerCase() + " for: " + resource.toString());
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
