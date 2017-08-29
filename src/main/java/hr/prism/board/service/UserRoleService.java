package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.UserRoleEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("JpaQlInspection")
public class UserRoleService {

    private static final String RESOURCE_USER_ROLE =
        "select distinct userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.state in (:userRoleStates) ";

    private static final String RESOURCE_USER_USER_ROLE =
        "select distinct userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.state in (:userRoleStates) ";

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
    private UserRoleMapper userRoleMapper;

    @Inject
    private UserRoleEventService userRoleEventService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public UserRole fineOne(Long userRoleId) {
        return userRoleRepository.findOne(userRoleId);
    }

    public List<UserRole> findByResourceAndUser(Resource resource, User user) {
        return userRoleRepository.findByResourceAndUser(resource, user);
    }

    public List<ResourceUserRepresentation> getResourceUsers(Scope scope, Long resourceId) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.VIEW, () -> resource);

        List<UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createQuery(RESOURCE_USER_ROLE)
                .setParameter("resource", resource)
                .setParameter("userRoleStates", State.ACTIVE_USER_ROLE_STATES)
                .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("userRole.extended"))
                .getResultList());

        Map<User, ResourceUserRepresentation> resourceUsersMap = new TreeMap<>();
        for (UserRole userRole : userRoles) {
            User user = userRole.getUser();
            if (!resourceUsersMap.containsKey(user)) {
                resourceUsersMap.put(user, new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(new HashSet<>()));
            }

            ResourceUserRepresentation representation = resourceUsersMap.get(user);
            representation.getRoles().add(userRoleMapper.apply(userRole));
        }

        return new ArrayList<>(resourceUsersMap.values());
    }

    public void createOrUpdateUserRole(Resource resource, User user, Role role) {
        createOrUpdateUserRole(user, resource, user, new UserRoleDTO().setRole(role));
    }

    public ResourceUserRepresentation createResourceUser(Scope scope, Long resourceId, ResourceUserDTO resourceUserDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userService.getOrCreateUser(resourceUserDTO.getUser());
        Set<UserRoleDTO> roles = resourceUserDTO.getRoles();
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            for (UserRoleDTO roleDTO : roles) {
                createOrUpdateUserRole(currentUser, resource, user, roleDTO);
            }

            return resource;
        });

        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(getUserRoles(resource, user));
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
            createOrUpdateUserRole(currentUser, resource, user, userRoleDTO);
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
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            userRoleCacheService.updateResourceUser(currentUser, resource, user, resourceUserDTO);
            return resource;
        });

        return new ResourceUserRepresentation().setUser(userMapper.apply(user)).setRoles(getUserRoles(resource, user));
    }

    public List<UserRole> findByResourceAndState(Resource resource, State state) {
        return userRoleRepository.findByResourceAndState(resource, state);
    }

    public UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role) {
        return userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
    }

    public UserRole findByResourceAndUserIdAndRole(Resource resource, Long userId, Role role) {
        return userRoleRepository.findByResourceAndUserIdAndRole(resource, userId, role);
    }

    public List<UserRole> findByResourceAndRole(Resource resource, Role role) {
        return userRoleRepository.findByResourceAndRole(resource, role);
    }

    private void createOrUpdateUserRole(User currentUser, Resource resource, User user, UserRoleDTO roleDTO) {
        if (roleDTO.getRole() == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, roleDTO.getRole());
        if (userRole == null) {
            userRoleCacheService.createUserRole(currentUser, resource, user, roleDTO, true);
        } else if (userRole.getState() == State.REJECTED) {
            userRole.setState(State.ACCEPTED);
            userRoleCacheService.updateUserRolesSummary(resource);
        }
    }

    private Set<UserRoleRepresentation> getUserRoles(Resource resource, User user) {
        entityManager.flush();
        List<UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createQuery(RESOURCE_USER_USER_ROLE)
                .setParameter("resource", resource)
                .setParameter("user", user)
                .setParameter("userRoleStates", State.ACTIVE_USER_ROLE_STATES)
                .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("userRole.extended"))
                .getResultList());
        return userRoles.stream().map(userRoleMapper).collect(Collectors.toSet());
    }

}
