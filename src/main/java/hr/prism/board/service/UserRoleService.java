package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ResourceUserRepresentation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service
@Transactional
public class UserRoleService {
    
    @Inject
    private UserRoleRepository userRoleRepository;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserService userService;
    
    @Inject
    private ActionService actionService;
    
    @Inject
    private ApplicationContext applicationContext;
    
    public void createUserRole(Long resourceId, Long userId, Role role) {
        Resource resource = resourceService.findOne(resourceId);
        User user = userService.findOne(userId);
        createUserRole(resource, user, role);
    }
    
    public void createUserRole(Resource resource, User user, Role role) {
        if (role == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }
        
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
        if (userRole == null) {
            userRole = new UserRole().setResource(resource).setUser(user).setRole(role);
            userRoleRepository.save(userRole);
    
            resource.getUserRoles().add(userRole);
            user.getUserRoles().add(userRole);
        }
    }
    
    public boolean hasUserRole(Resource resource, User user, Role... roles) {
        if (ArrayUtils.isEmpty(roles)) {
            throw new IllegalStateException("No roles specified");
        }
        
        return userRoleRepository.findByResourceAndUserAndRoles(resource, user, roles).size() > 0;
    }
    
    public List<ResourceUserRepresentation> getResourceUsers(Scope scope, Long resourceId) {
        UserMapper userMapper = applicationContext.getBean(UserMapper.class);
        User currentUser = userService.getCurrentUser();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.VIEW, () -> resource);
        
        List<UserRole> userRoles = userRoleRepository.findByResource(resource);
        Map<Long, ResourceUserRepresentation> resourceUsersMap = new TreeMap<>();
        for (UserRole userRole : userRoles) {
            User user = userRole.getUser();
            if (!resourceUsersMap.containsKey(user.getId())) {
                resourceUsersMap.put(user.getId(), new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(new HashSet<>()));
            }
            ResourceUserRepresentation representation = resourceUsersMap.get(user.getId());
            representation.getRoles().add(userRole.getRole());
        }
        return new ArrayList<>(resourceUsersMap.values());
    }
    
    public ResourceUserRepresentation addResourceUser(Scope scope, Long resourceId, ResourceUserDTO resourceUserDTO) {
        UserMapper userMapper = applicationContext.getBean(UserMapper.class);
        User currentUser = userService.getCurrentUser();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> resource);
        
        User user = userService.getOrCreateUser(resourceUserDTO.getUser());
        ResourceUserRepresentation userRepresentation = new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(new HashSet<>());
        
        for (Role role : resourceUserDTO.getRoles()) {
            UserRole userRole = new UserRole().setRole(role).setUser(user).setResource(resource);
            userRoleRepository.save(userRole);
            userRepresentation.getRoles().add(role);
        }
        return userRepresentation;
    }
    
    public void removeResourceUser(Scope scope, Long resourceId, Long userId) {
        User currentUser = userService.getCurrentUser();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> resource);
        
        User user = userService.findOne(userId);
        userRoleRepository.deleteByResourceAndUser(resource, user);
        
        if (scope == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new ApiException(ExceptionCode.IRREMOVABLE_USER);
            }
        }
    }
    
    public void addUserRole(Scope scope, Long resourceId, Long userId, Role role) {
        User currentUser = userService.getCurrentUser();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> resource);
        
        User user = userService.findOne(userId);
        UserRole userRole = new UserRole().setRole(role).setUser(user).setResource(resource);
        userRoleRepository.save(userRole);
    }
    
    public void removeUserRole(Scope scope, Long resourceId, Long userId, Role role) {
        User currentUser = userService.getCurrentUser();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> resource);
        
        User user = userService.findOne(userId);
        List<UserRole> userRoles = userRoleRepository.findByResourceAndUser(resource, user);
        if (userRoles.size() <= 1) {
            throw new ApiException(ExceptionCode.IRREMOVABLE_USER_ROLE);
        }
        
        Long deletedCount = userRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
        if (deletedCount < 1) {
            throw new ApiException(ExceptionCode.NON_EXISTING_USER_ROLE);
        }
        
        if (scope == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new ApiException(ExceptionCode.IRREMOVABLE_USER_ROLE);
            }
        }
    }
}
