package hr.prism.board.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;

@Service
@Transactional
public class UserRoleService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserMapper userMapper;

    public List<ResourceUserRepresentation> getResourceUsers(Scope scope, Long resourceId) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.VIEW, () -> resource);

        List<UserRole> userRoles = userRoleRepository.findByResource(resource);
        Map<User, ResourceUserRepresentation> resourceUsersMap = new TreeMap<>();
        for (UserRole userRole : userRoles) {
            User user = userRole.getUser();
            if (!resourceUsersMap.containsKey(user)) {
                resourceUsersMap.put(user, new ResourceUserRepresentation().setUser(userMapper.apply(user))
                    .setRoles(new TreeSet<>()));
            }

            ResourceUserRepresentation representation = resourceUsersMap.get(user);
            representation.getRoles().add(userRole.getRole());
        }

        return new ArrayList<>(resourceUsersMap.values());
    }

    public void createUserRole(Resource resource, User user, Role role) {
        if (role == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
        if (userRole == null) {
            userRoleCacheService.createUserRole(resource, user, role);
        }
    }

    public ResourceUserRepresentation createResourceUser(Scope scope, Long resourceId, ResourceUserDTO resourceUserDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userService.getOrCreateUser(resourceUserDTO.getUser());
        Set<Role> roles = resourceUserDTO.getRoles();
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            for (Role role : roles) {
                createUserRole(resource, user, role);
            }

            return resource;
        });

        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(roles);
    }

    // FIXME: make this asynchronous, likely to be slow for more than 20 or so users
    public void createResourceUsers(Scope scope, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            for (UserDTO userDTO : resourceUsersDTO.getUsers()) {
                User user = userService.getOrCreateUser(userDTO);
                for (Role role : resourceUsersDTO.getRoles()) {
                    createUserRole(resource, user, role);
                }
            }

            return resource;
        });
    }

    public void deleteResourceUser(Scope scope, Long resourceId, Long userId) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            User user = userCacheService.findOne(userId);
            userRoleCacheService.deleteResourceUser(resource, user);
            return resource;
        });
    }

    public void createUserRole(Scope scope, Long resourceId, Long userId, Role role) {
        User user = userCacheService.findOne(userId);
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            createUserRole(resource, user, role);
            return resource;
        });
    }

    public void deleteUserRole(Scope scope, Long resourceId, Long userId, Role role) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            User user = userCacheService.findOne(userId);
            userRoleCacheService.deleteUserRole(resource, user, role);
            return resource;
        });
    }

}
