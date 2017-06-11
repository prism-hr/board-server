package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.event.UserRoleEvent;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;
import org.springframework.context.ApplicationEventPublisher;
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
    private ApplicationEventPublisher applicationEventPublisher;

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
        this.createUserRole(resource, user, new UserRoleDTO().setRole(role));
    }

    public ResourceUserRepresentation createResourceUser(Scope scope, Long resourceId, ResourceUserDTO resourceUserDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userService.getOrCreateUser(resourceUserDTO.getUser());
        Set<UserRoleDTO> roles = resourceUserDTO.getRoles();
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            for (UserRoleDTO roleDTO : roles) {
                createUserRole(resource, user, roleDTO);
            }

            return resource;
        });

        Set<UserRoleRepresentation> rolesRepresentation = roles.stream().map(r ->
            new UserRoleRepresentation().setRole(r.getRole()).setExpiryDate(r.getExpiryDate()).setCategories(r.getCategories())).collect(Collectors.toSet());
        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(rolesRepresentation);
    }

    public void createResourceUsers(Scope scope, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            applicationEventPublisher.publishEvent(new UserRoleEvent(this, resourceId, resourceUsersDTO));
            return resource;
        });
    }

    // Method is used by UserRoleEventService#createResourceUsers to process user creation in a series of small transactions
    // Don't use it for anything else - there are no security checks applied to it, security checks are applied when we publish the producing event
    public void createResourceUser(Long resourceId, UserDTO userDTO, Set<UserRoleDTO> userRoleDTOs) {
        User user = userService.getOrCreateUser(userDTO);
        Resource resource = resourceService.findOne(resourceId);
        for (UserRoleDTO userRoleDTO : userRoleDTOs) {
            createUserRole(resource, user, userRoleDTO);
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
            userRoleCacheService.updateResourceUser(resource, user, resourceUserDTO);
            return resource;
        });

        Set<UserRoleRepresentation> rolesRepresentation = roles.stream().map(r ->
            new UserRoleRepresentation().setRole(r.getRole()).setExpiryDate(r.getExpiryDate()).setCategories(r.getCategories())).collect(Collectors.toSet());
        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(rolesRepresentation);
    }

    private void createUserRole(Resource resource, User user, UserRoleDTO roleDTO) {
        if (roleDTO.getRole() == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, roleDTO.getRole());
        if (userRole == null) {
            userRoleCacheService.createUserRole(resource, user, roleDTO);
        }
    }

}
