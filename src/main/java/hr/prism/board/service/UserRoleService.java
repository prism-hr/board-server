package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.representation.UserRolesRepresentation;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("JpaQlInspection")
public class UserRoleService {

    private static final String RESOURCE_USER_ROLE =
        "select distinct userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.role <> :role " +
            "and userRole.state in (:states) ";

    private static final String RESOURCE_USER_USER_ROLE =
        "select distinct userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role = :role " +
            "and userRole.state in (:states) ";

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
    private DepartmentService departmentService;

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

    public UserRolesRepresentation getResourceUsers(Scope scope, Long resourceId) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> resource);

        List<UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createQuery(RESOURCE_USER_ROLE)
                .setParameter("resource", resource)
                .setParameter("role", Role.MEMBER)
                .setParameter("states", State.ACTIVE_USER_ROLE_STATES)
                .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("userRole.extended"))
                .getResultList());

        Map<User, UserRoleRepresentation> resourceUsersMap = new TreeMap<>();
        for (UserRole userRole : userRoles) {
            User user = userRole.getUser();
            if (!resourceUsersMap.containsKey(user)) {
                resourceUsersMap.put(user, userRoleMapper.apply(userRole));
            }
        }

        UserRolesRepresentation response =
            new UserRolesRepresentation()
                .setUsers(new ArrayList<>(resourceUsersMap.values()));
        if (scope == Scope.DEPARTMENT) {
            response.setMembers(userRoleRepository.findByResourceAndRoleAndState(
                resource, Role.MEMBER, State.ACCEPTED).stream().map(userRoleMapper).collect(Collectors.toList()));
            response.setMemberRequests(departmentService.getMembershipRequests(
                (Department) resource, null, false).stream().map(userRoleMapper).collect(Collectors.toList()));
        }

        return response;
    }

    public void createOrUpdateUserRole(Resource resource, User user, Role role) {
        createOrUpdateUserRole(user, resource, user, new UserRoleDTO().setRole(role));
    }

    public UserRoleRepresentation createResourceUser(Scope scope, Long resourceId, UserRoleDTO userRoleDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userService.getOrCreateUser(userRoleDTO.getUser());
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            createOrUpdateUserRole(currentUser, resource, user, userRoleDTO);
            return resource;
        });

        return getUserRole(resource, user, userRoleDTO.getRole());
    }

    public Long createResourceUsers(Scope scope, Long resourceId, List<UserRoleDTO> userRoleDTOs) {
        if (userRoleDTOs.stream().map(UserRoleDTO::getRole).anyMatch(role -> role != Role.MEMBER)) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_USER);
        }

        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            userRoleEventService.publishEvent(this, currentUser.getId(), resourceId, userRoleDTOs);
            return resource;
        });

        List<String> emails = userRoleDTOs.stream().map(UserRoleDTO::getUser).map(UserDTO::getEmail).collect(Collectors.toList());
        Long memberCountProvisional = userService.findUserCount(resource, Role.MEMBER, emails) + emails.size();
        ((Department) resource).setMemberCountProvisional(memberCountProvisional);
        return memberCountProvisional;
    }

    public void createResourceUser(User currentUser, Long resourceId, UserRoleDTO userRoleDTO, boolean invokedAsynchronously) {
        if (invokedAsynchronously && userService.getCurrentUser() != null) {
            // There should never be an authenticated user inside this method authentication
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_RESOURCE_USER);
        }

        User user = userService.getOrCreateUser(userRoleDTO.getUser());
        Resource resource = resourceService.findOne(resourceId);
        createOrUpdateUserRole(currentUser, resource, user, userRoleDTO);
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

    public UserRoleRepresentation updateResourceUser(Scope scope, Long resourceId, Long userId, UserRoleDTO userRoleDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userCacheService.findOne(userId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            userRoleCacheService.updateResourceUser(currentUser, resource, user, userRoleDTO);
            return resource;
        });

        return getUserRole(resource, user, userRoleDTO.getRole());
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

    private UserRoleRepresentation getUserRole(Resource resource, User user, Role role) {
        entityManager.flush();
        List<UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createQuery(RESOURCE_USER_USER_ROLE, UserRole.class)
                .setParameter("resource", resource)
                .setParameter("user", user)
                .setParameter("role", role)
                .setParameter("states", State.ACTIVE_USER_ROLE_STATES)
                .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("userRole.extended"))
                .getResultList());
        if (userRoles.isEmpty()) {
            return null;
        }

        return userRoleMapper.apply(userRoles.get(0));
    }

}
