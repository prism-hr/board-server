package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.DemographicDataStatusRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.representation.UserRolesRepresentation;
import hr.prism.board.value.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.utils.BoardUtils.getAcademicYearStart;

@Service
@Transactional
public class UserRoleService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCacheService userRoleCacheService;

//    @Inject
//    private ActivityService activityService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @Inject
    private UserCacheService userCacheService;

//    @Inject
//    private ActionService actionService;

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
        actionService.executeAction(user, resource, EDIT, () -> resource);

        List<UserRole> users;
        List<UserRole> members = Collections.emptyList();
        List<UserRole> memberRequests = Collections.emptyList();

        if (scope == Scope.DEPARTMENT) {
            users = getUserRoles(resource, ImmutableList.of(Role.ADMINISTRATOR, Role.AUTHOR), ACCEPTED, searchTerm);
            members = getUserRoles(resource, Collections.singletonList(MEMBER), ACCEPTED, searchTerm);

            memberRequests = getUserRoles(resource, Collections.singletonList(MEMBER), State.PENDING, searchTerm);
            if (!memberRequests.isEmpty()) {
                Map<Activity, UserRole> indexByActivities = memberRequests.stream()
                    .filter(userRole -> userRole.getActivity() != null).collect(Collectors.toMap(UserRole::getActivity, userRole -> userRole));
                for (hr.prism.board.domain.ActivityEvent activityEvent : activityService.findViews(indexByActivities.keySet(), user)) {
                    indexByActivities.get(activityEvent.getActivity()).setViewed(true);
                }
            }
        } else if (scope == Scope.BOARD) {
            users = getUserRoles(resource, Arrays.asList(Role.ADMINISTRATOR, Role.AUTHOR), ACCEPTED, searchTerm);
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

    public UserRole createUserRole(User user, Resource resource, UserRoleDTO userRoleDTO) {
        UserDTO userDTO = userRoleDTO.getUser();
        User userCreateUpdate = userService.getOrCreateUser(userDTO, (email) -> userCacheService.findByEmail(email));
        return createOrUpdateUserRole(user, resource, userCreateUpdate, userRoleDTO);
    }

    public void createOrUpdateUserRole(User user, Long resourceId, UserRoleDTO userRoleDTO) {
        UserDTO userDTO = userRoleDTO.getUser();
        Resource resource = resourceService.findOne(resourceId);
        User userCreate = userService.getOrCreateUser(userDTO,
            (email) -> userCacheService.findByEmail(resource, email, MEMBER));
        UserRole userRole = createOrUpdateUserRole(user, resource, userCreate, userRoleDTO);
        userRole.setEmail(userDTO.getEmail());
    }

    public void deleteUserRoles(Scope scope, Long resourceId, Long userId) {
        User currentUser = userService.getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, EDIT, () -> {
            User user = userCacheService.getUser(userId);
            userRoleCacheService.deleteResourceUser(resource, user);
            activityService.sendActivities(resource);
            return resource;
        });
    }

    public UserRole updateUserRole(Scope scope, Long resourceId, Long userUpdateId, UserRoleDTO userRoleDTO) {

        User userUpdate = userCacheService.getUser(userUpdateId);
        actionService.executeAction(currentUser, resource, EDIT, () -> {
            userRoleCacheService.updateUserRole(currentUser, resource, userUpdate, userRoleDTO);
            activityService.sendActivities(resource);
            return resource;
        });

        return getUserRole(resource, userUpdate, userRoleDTO.getRole());
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

    @SuppressWarnings("unchecked")
    public Statistics getMemberStatistics(Long departmentId) {
        return (Statistics) entityManager.createNamedQuery("memberStatistics")
            .setParameter("departmentId", departmentId)
            .getSingleResult();
    }

    public DemographicDataStatusRepresentation makeDemographicDataStatus(User user, Department department,
                                                                         boolean canPursue) {
        DemographicDataStatusRepresentation responseReadiness = new DemographicDataStatusRepresentation();
        if (Stream.of(user.getGender(), user.getAgeRange(), user.getLocationNationality()).anyMatch(Objects::isNull)) {
            // User data incomplete
            responseReadiness.setRequireUserDemographicData(true);
        }

        if (department.getMemberCategories().isEmpty()) {
            return responseReadiness;
        }

        UserRole userRole = findByResourceAndUserAndRole(department, user, MEMBER);
        if (userRole == null) {
            // Don't bug administrator for user role data
            responseReadiness.setRequireUserRoleDemographicData(!canPursue);
        } else {
            MemberCategory memberCategory = userRole.getMemberCategory();
            String memberProgram = userRole.getMemberProgram();
            Integer memberYear = userRole.getMemberYear();
            if (Stream.of(memberCategory, memberProgram, memberYear).anyMatch(Objects::isNull)) {
                // User role data incomplete
                responseReadiness.setRequireUserRoleDemographicData(true)
                    .setUserRole(
                        new UserRoleRepresentation()
                            .setMemberCategory(memberCategory)
                            .setMemberProgram(memberProgram)
                            .setMemberYear(memberYear));
            } else {
                LocalDate academicYearStart = getAcademicYearStart();
                if (academicYearStart.isAfter(userRole.getMemberDate())) {
                    // User role data out of date
                    responseReadiness.setRequireUserRoleDemographicData(true)
                        .setUserRole(
                            new UserRoleRepresentation()
                                .setMemberCategory(memberCategory)
                                .setMemberProgram(memberProgram)
                                .setMemberYear(memberYear));
                }
            }
        }

        return responseReadiness;
    }

    private UserRole createOrUpdateUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {
        if (userRoleDTO.getRole() == PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }

        Role role = userRoleDTO.getRole();
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
        if (userRole == null) {
            return userRoleCacheService.createUserRole(
                currentUser, resource, user, userRoleDTO, NON_MEMBER_ROLES.contains(role));
        } else {
            userRoleCacheService.updateMembershipData(userRole, userRoleDTO);
            userRole.setState(ACCEPTED);
            userRole.setExpiryDate(userRoleDTO.getExpiryDate());
            return userRole;
        }
    }

    // TODO: move to repository
    private UserRole getUserRole(Resource resource, User user, Role role) {
        entityManager.flush();
        return entityManager.createQuery(
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
            .setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("userRole.extended"))
            .getSingleResult();
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

        // TODO: move to repository
        List<UserRole> userRoles = entityManager.createQuery(statement, UserRole.class)
            .setParameter("search", search)
            .setParameter("resource", resource)
            .setParameter("userIds", userIds)
            .setParameter("roles", roles)
            .setParameter("state", state)
            .setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("userRole.extended"))
            .getResultList();

        if (searchTermApplied) {
            userService.deleteSearchResults(search);
        }

        return userRoles;
    }

}
