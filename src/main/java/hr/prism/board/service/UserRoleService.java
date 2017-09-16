package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("JpaQlInspection")
public class UserRoleService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private ActivityService activityService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private ActionService actionService;

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

    public UserRolesRepresentation getUserRoles(Scope scope, Long resourceId, String searchTerm) {
        User user = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(user, scope, resourceId);
        actionService.executeAction(user, resource, Action.EDIT, () -> resource);

        List<UserRole> users;
        List<UserRole> members = Collections.emptyList();
        List<UserRole> memberRequests = Collections.emptyList();
        if (scope == Scope.DEPARTMENT) {
            users = getUserRoles(resource, Collections.singletonList(Role.ADMINISTRATOR), State.ACCEPTED, searchTerm);
            members = getUserRoles(resource, Collections.singletonList(Role.MEMBER), State.ACCEPTED, searchTerm);

            memberRequests = getUserRoles(resource, Collections.singletonList(Role.MEMBER), State.PENDING, searchTerm);
            if (!memberRequests.isEmpty()) {
                Map<Activity, UserRole> indexByActivities = memberRequests.stream().collect(Collectors.toMap(UserRole::getActivity, userRole -> userRole));
                for (hr.prism.board.domain.ActivityEvent activityEvent : activityService.findViews(indexByActivities.keySet(), user)) {
                    indexByActivities.get(activityEvent.getActivity()).setViewed(true);
                }
            }
        } else if (scope == Scope.BOARD) {
            users = getUserRoles(resource, Arrays.asList(Role.ADMINISTRATOR, Role.AUTHOR), State.ACCEPTED, searchTerm);
        } else {
            throw new IllegalStateException("Cannot request user roles for post");
        }

        return new UserRolesRepresentation()
            .setUsers(users.stream().map(userRoleMapper).collect(Collectors.toList()))
            .setMembers(members.stream().map(userRoleMapper).collect(Collectors.toList()))
            .setMemberRequests(memberRequests.stream().map(userRoleMapper).collect(Collectors.toList()));
    }

    public void createOrUpdateUserRole(Resource resource, User user, Role role) {
        createOrUpdateUserRole(user, resource, user, new UserRoleDTO().setRole(role));
    }

    public UserRoleRepresentation createResourceUser(Scope scope, Long resourceId, UserRoleDTO userRoleDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);

        UserDTO userDTO = userRoleDTO.getUser();
        User user = userService.getOrCreateUser(userDTO, (email) -> userCacheService.findByEmail(email));
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            createOrUpdateUserRole(currentUser, resource, user, userRoleDTO);
            return resource;
        });

        return getUserRole(resource, user, userRoleDTO.getRole());
    }

    public Long createResourceUsers(Scope scope, Long resourceId, List<UserRoleDTO> userRoleDTOs) {
        if (userRoleDTOs.stream().map(UserRoleDTO::getRole).anyMatch(role -> role != Role.MEMBER)) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_USER, "Only members can be bulk created");
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

    public void createOrUpdateResourceUser(User currentUser, Long resourceId, UserRoleDTO userRoleDTO, boolean invokedAsynchronously) {
        if (invokedAsynchronously && userService.getCurrentUser() != null) {
            // There should never be an authenticated user inside this method invocation
            throw new IllegalStateException("Bulk resource user creation should always be processed anonymously");
        }

        UserDTO userDTO = userRoleDTO.getUser();
        Resource resource = resourceService.findOne(resourceId);
        User user = userService.getOrCreateUser(userDTO, (email) -> userCacheService.findByEmail(resource, email, Role.MEMBER));
        UserRole userRole = createOrUpdateUserRole(currentUser, resource, user, userRoleDTO);
        userRole.setEmail(userDTO.getEmail());
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

    public UserRole findByUuid(String uuid) {
        return userRoleRepository.findByUuid(uuid);
    }

    private UserRole createOrUpdateUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {
        if (userRoleDTO.getRole() == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, userRoleDTO.getRole());
        if (userRole == null) {
            return userRoleCacheService.createUserRole(currentUser, resource, user, userRoleDTO, true);
        } else {
            userRole.setState(State.ACCEPTED);
            userRole.setExpiryDate(userRoleDTO.getExpiryDate());
            userRoleCacheService.deleteUserRoleCategories(resource, user);
            userRoleCacheService.createUserRoleCategories(userRole, userRoleDTO);
            userRoleCacheService.updateUserRolesSummary(resource);
            return userRole;
        }
    }

    private UserRoleRepresentation getUserRole(Resource resource, User user, Role role) {
        entityManager.flush();
        List<UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createQuery(
                "select distinct userRole " +
                    "from UserRole userRole " +
                    "where userRole.resource = :resource " +
                    "and userRole.user = :user " +
                    "and userRole.role = :role " +
                    "and userRole.state in (:states) ", UserRole.class)
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

    @SuppressWarnings("JpaQlInspection")
    private List<UserRole> getUserRoles(Resource resource, List<Role> roles, State state, String searchTerm) {
        List<Long> userIds = userService.findByResourceAndRoleAndStates(resource, roles, state);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        String search = UUID.randomUUID().toString();
        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            userService.createSearchResults(search, searchTerm, userIds);
            entityManager.flush();
        }

        List<UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status -> {
            String statement =
                "select distinct userRole " +
                    "from UserRole userRole " +
                    "inner join userRole.user user " +
                    "left join user.searches search on search.search = :search " +
                    "where userRole.resource = :resource " +
                    "and user.id in (:userIds) " +
                    "and userRole.role in(:roles) " +
                    "and userRole.state = :state ";
            if (searchTermApplied) {
                statement += "and search.id is not null ";
            }

            statement += "order by search.id, user.id desc";
            return entityManager.createQuery(statement, UserRole.class)
                .setParameter("search", search)
                .setParameter("resource", resource)
                .setParameter("userIds", userIds)
                .setParameter("roles", roles)
                .setParameter("state", state)
                .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("userRole.extended"))
                .getResultList();
        });

        if (searchTermApplied) {
            userService.deleteSearchResults(search);
        }

        return userRoles;
    }

}
