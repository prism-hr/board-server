package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.representation.UserRolesRepresentation;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.cache.UserRoleCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings({"JpaQlInspection", "SpringAutowiredFieldsWarningInspection", "WeakerAccess"})
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
    private EntityManager entityManager;

    public UserRole fineOne(Long userRoleId) {
        return userRoleRepository.findOne(userRoleId);
    }

    public UserRolesRepresentation getUserRoles(Scope scope, Long resourceId, String searchTerm) {
        User user = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(user, scope, resourceId);
        actionService.executeAction(user, resource, Action.EDIT, () -> resource);

        List<UserRole> users;
        List<UserRole> members = Collections.emptyList();
        List<UserRole> memberRequests = Collections.emptyList();

        if (scope == Scope.DEPARTMENT) {
            users = getUserRoles(resource, ImmutableList.of(Role.ADMINISTRATOR, Role.AUTHOR), State.ACCEPTED, searchTerm);
            members = getUserRoles(resource, Collections.singletonList(Role.MEMBER), State.ACCEPTED, searchTerm);

            memberRequests = getUserRoles(resource, Collections.singletonList(Role.MEMBER), State.PENDING, searchTerm);
            if (!memberRequests.isEmpty()) {
                Map<Activity, UserRole> indexByActivities = memberRequests.stream()
                    .filter(userRole -> userRole.getActivity() != null).collect(Collectors.toMap(UserRole::getActivity, userRole -> userRole));
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
            .setMemberRequests(memberRequests.stream().map(userRoleMapper).collect(Collectors.toList()))
            .setMemberToBeUploadedCount(scope == Scope.DEPARTMENT ? ((Department) resource).getMemberToBeUploadedCount() : null);
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
            activityService.sendActivities(resource);
            return resource;
        });
    }

    public UserRoleRepresentation updateResourceUser(Scope scope, Long resourceId, Long userId, UserRoleDTO userRoleDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        User user = userCacheService.findOne(userId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> {
            userRoleCacheService.updateResourceUser(currentUser, resource, user, userRoleDTO);
            activityService.sendActivities(resource);
            return resource;
        });

        return getUserRole(resource, user, userRoleDTO.getRole());
    }

    public UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role) {
        return userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
    }

    public List<UserRole> findByResourceAndUser(Resource resource, User user) {
        return userRoleRepository.findByResourceAndUser(resource, user);
    }

    public UserRole findByResourceAndUserIdAndRole(Resource resource, Long userId, Role role) {
        return userRoleRepository.findByResourceAndUserIdAndRole(resource, userId, role);
    }

    public boolean hasAdministratorRole(User user) {
        return userRoleRepository.findIdsByUserAndRole(user, Role.ADMINISTRATOR, State.ACTIVE_USER_ROLE_STATES, LocalDate.now()).isEmpty();
    }

    private UserRole createOrUpdateUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {
        if (userRoleDTO.getRole() == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        Role role = userRoleDTO.getRole();
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, userRoleDTO.getRole());
        if (userRole == null) {
            return userRoleCacheService.createUserRole(currentUser, resource, user, userRoleDTO, Role.NOTIFIABLE.contains(role));
        } else {
            userRoleCacheService.updateUserRoleDemographicData(userRole, userRoleDTO);
            userRole.setState(State.ACCEPTED);
            userRole.setExpiryDate(userRoleDTO.getExpiryDate());
            userRoleCacheService.updateUserRolesSummary(resource);
            return userRole;
        }
    }

    private UserRoleRepresentation getUserRole(Resource resource, User user, Role role) {
        entityManager.flush();
        List<UserRole> userRoles = entityManager.createQuery(
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
            .getResultList();
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
        List<UserRole> userRoles = entityManager.createQuery(statement, UserRole.class)
            .setParameter("search", search)
            .setParameter("resource", resource)
            .setParameter("userIds", userIds)
            .setParameter("roles", roles)
            .setParameter("state", state)
            .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("userRole.extended"))
            .getResultList();

        if (searchTermApplied) {
            userService.deleteSearchResults(search);
        }

        return userRoles;
    }

}
