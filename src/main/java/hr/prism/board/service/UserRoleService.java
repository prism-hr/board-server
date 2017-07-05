package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.domain.UserRoleCategory;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.UserRoleEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

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

    @Inject
    private UserRoleEventService userRoleEventService;

    public List<ResourceUserRepresentation> getResourceUsers(Scope scope, Long resourceId) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.VIEW, () -> resource);

        List<UserRole> userRoles = userRoleRepository.findByResource(resource);
        Map<User, ResourceUserRepresentation> resourceUsersMap = new TreeMap<>();
        for (UserRole userRole : userRoles) {
            User user = userRole.getUser();
            if (!resourceUsersMap.containsKey(user)) {
                resourceUsersMap.put(user, new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(new HashSet<>()));
            }

            ResourceUserRepresentation representation = resourceUsersMap.get(user);
            UserRoleRepresentation userRoleRepresentation = new UserRoleRepresentation().setRole(userRole.getRole()).setExpiryDate(userRole.getExpiryDate())
                .setCategories(userRole.getCategories().stream().map(UserRoleCategory::getName).collect(Collectors.toList()));
            representation.getRoles().add(userRoleRepresentation);
        }

        return new ArrayList<>(resourceUsersMap.values());
    }

    public void createUserRole(Resource resource, User user, Role role) {
        createUserRole(user, resource, user, new UserRoleDTO().setRole(role));
    }

    public ResourceUserRepresentation createResourceUser(Scope scope, Long resourceId, ResourceUserDTO resourceUserDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userService.getOrCreateUser(resourceUserDTO.getUser());
        Set<UserRoleDTO> roles = resourceUserDTO.getRoles();
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            for (UserRoleDTO roleDTO : roles) {
                createUserRole(currentUser, resource, user, roleDTO);
            }

            return resource;
        });

        Set<UserRoleRepresentation> rolesRepresentation = roles.stream().map(r ->
            new UserRoleRepresentation().setRole(r.getRole()).setExpiryDate(r.getExpiryDate())
                .setCategories(Optional.ofNullable(r.getCategories()).orElse(Collections.emptyList()))).collect(Collectors.toSet());
        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(rolesRepresentation);
    }

    public void createResourceUsers(Scope scope, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            userRoleEventService.publishEvent(this, currentUser.getId(), resourceId, resourceUsersDTO);
            return resource;
        });
    }

    // Method is used by UserRoleEventService#createResourceUsers to process user creation in a series of small transactions
    // Don't use it for anything else - there are no security checks applied to it, security checks are applied when we publish the producing event
    public void createResourceUser(User currentUser, Long resourceId, UserDTO userDTO, Set<UserRoleDTO> userRoleDTOs) {
        User user = userService.getOrCreateUser(userDTO);
        Resource resource = resourceService.findOne(resourceId);
        for (UserRoleDTO userRoleDTO : userRoleDTOs) {
            createUserRole(currentUser, resource, user, userRoleDTO);
        }
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

    public ResourceUserRepresentation updateResourceUser(Scope scope, Long resourceId, Long userId, ResourceUserDTO resourceUserDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userCacheService.findOne(userId);
        Set<UserRoleDTO> roles = resourceUserDTO.getRoles();
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            userRoleCacheService.updateResourceUser(currentUser, resource, user, resourceUserDTO);
            return resource;
        });

        Set<UserRoleRepresentation> rolesRepresentation = roles.stream().map(r ->
            new UserRoleRepresentation().setRole(r.getRole()).setExpiryDate(r.getExpiryDate())
                .setCategories(Optional.ofNullable(r.getCategories()).orElse(Collections.emptyList())))
            .collect(Collectors.toSet());
        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(rolesRepresentation);
    }

    public UserRole findbyResourceAndUserAndRole(Resource resource, User user, Role role) {
        return userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
    }

    private void createUserRole(User currentUser, Resource resource, User user, UserRoleDTO roleDTO) {
        if (roleDTO.getRole() == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, roleDTO.getRole());
        if (userRole == null) {
            userRoleCacheService.createUserRole(currentUser, resource, user, roleDTO, true);
        }
    }

}
