package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.MemberDTO;
import hr.prism.board.dto.StaffDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.RoleType;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.DepartmentMemberEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.value.DemographicDataStatus;
import hr.prism.board.value.UserRoles;
import hr.prism.board.value.UserSearch;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Activity.JOIN_DEPARTMENT_ACTIVITY;
import static hr.prism.board.enums.Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY;
import static hr.prism.board.enums.Notification.JOIN_DEPARTMENT_NOTIFICATION;
import static hr.prism.board.enums.Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION;
import static hr.prism.board.enums.ResourceTask.MEMBER_TASKS;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.getAcademicYearStart;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
@Transactional
public class DepartmentUserService {

    private final UserService userService;

    private final ResourceService resourceService;

    private final ActionService actionService;

    private final UserRoleService userRoleService;

    private final ActivityService activityService;

    private final ResourceTaskService resourceTaskService;

    private final EventProducer eventProducer;

    private final EntityManager entityManager;

    @Inject
    public DepartmentUserService(UserService userService, ResourceService resourceService,
                                 ActionService actionService, UserRoleService userRoleService,
                                 ResourceTaskService resourceTaskService, ActivityService activityService,
                                 EventProducer eventProducer, EntityManager entityManager) {
        this.userService = userService;
        this.resourceService = resourceService;
        this.actionService = actionService;
        this.userRoleService = userRoleService;
        this.activityService = activityService;
        this.resourceTaskService = resourceTaskService;
        this.eventProducer = eventProducer;
        this.entityManager = entityManager;
    }

    public List<UserSearch> findUsers(User user, Long id, String searchTerm) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);
        return userService.findUsers(searchTerm);
    }

    @Transactional(isolation = SERIALIZABLE)
    public Department createMembers(User user, Long id, List<MemberDTO> memberDTOs) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        return (Department) actionService.executeAction(user, department, EDIT, () -> {
            department.increaseMemberTobeUploadedCount((long) memberDTOs.size());
            eventProducer.produce(new DepartmentMemberEvent(this, id, memberDTOs));
            department.setLastMemberTimestamp(now());
            return department;
        });
    }

    public User createMembershipRequest(User user, Long id, MemberDTO memberDTO) {
        Department department = (Department) resourceService.getById(id);
        checkExistingMemberRequest(department, user);
        checkValidMemberCategory(department, memberDTO.getMemberCategory());

        UserDTO userDTO = memberDTO.getUser();
        User userCreateUpdate = userService.createOrUpdateUser(
            userDTO, (email) -> userService.getByEmail(department, email, Role.MEMBER));
        UserRole userRole = userRoleService.createOrUpdateUserRole(department, userCreateUpdate, memberDTO, PENDING);
        checkValidDemographicData(user, department);

        eventProducer.produce(
            new ActivityEvent(this, id, UserRole.class, userRole.getId(),
                singletonList(
                    new hr.prism.board.workflow.Activity()
                        .setScope(DEPARTMENT)
                        .setRole(ADMINISTRATOR)
                        .setActivity(JOIN_DEPARTMENT_REQUEST_ACTIVITY))),
            new NotificationEvent(this, id,
                singletonList(
                    new hr.prism.board.workflow.Notification()
                        .setScope(DEPARTMENT)
                        .setRole(ADMINISTRATOR)
                        .setNotification(JOIN_DEPARTMENT_REQUEST_NOTIFICATION))));

        resourceTaskService.completeTasks(department, MEMBER_TASKS);
        return user;
    }

    public UserRole viewMembershipRequest(User user, Long id, Long userId) {
        Resource department = resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);
        UserRole userRole = userRoleService.getByResourceAndUserIdAndRole(department, userId, MEMBER);
        activityService.viewActivity(userRole.getActivity(), user);
        return userRole.setViewed(true);
    }

    public void reviewMembershipRequest(User user, Long id, Long userId, State state) {
        Resource department = resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> {
            UserRole userRole = userRoleService.getByResourceAndUserIdAndRole(department, userId, MEMBER);
            if (userRole.getState() == PENDING) {
                userRole.setState(state);

                eventProducer.produce(
                    new ActivityEvent(this, id, UserRole.class, userRole.getId()));
            }

            return department;
        });
    }

    public User updateMembership(User user, Long id, MemberDTO memberDTO) {
        Department department = (Department) resourceService.getById(id);

        UserRole userRole = userRoleService.getByResourceUserAndRole(department, user, MEMBER);
        if (userRole == null || userRole.getState() == REJECTED) {
            throw new BoardForbiddenException(FORBIDDEN_PERMISSION, "User is not a member");
        }

        UserDTO userDTO = memberDTO.getUser();
        userService.updateUserMembership(user, userDTO);
        userRoleService.updateMembership(userRole, memberDTO);
        checkValidDemographicData(user, department);
        return user;
    }

    public UserRoles getUserRoles(User user, Long id, String searchTerm) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        List<UserRole> staff = userRoleService.getUserRoles(department, STAFF_ROLES, ACCEPTED, searchTerm);
        List<UserRole> members = userRoleService.getUserRoles(department, MEMBER_ROLES, ACCEPTED, searchTerm);
        List<UserRole> memberRequests = userRoleService.getUserRoles(department, MEMBER_ROLES, PENDING, searchTerm);

        if (!memberRequests.isEmpty()) {
            Map<hr.prism.board.domain.Activity, UserRole> indexByActivities =
                memberRequests.stream()
                    .filter(userRole -> userRole.getActivity() != null)
                    .collect(toMap(UserRole::getActivity, Function.identity()));

            List<hr.prism.board.domain.ActivityEvent> views =
                activityService.findViews(indexByActivities.keySet(), user);
            for (hr.prism.board.domain.ActivityEvent activityEvent : views) {
                indexByActivities.get(activityEvent.getActivity()).setViewed(true);
            }
        }

        return new UserRoles()
            .setStaff(staff)
            .setMembers(members)
            .setMemberRequests(memberRequests)
            .setMemberToBeUploadedCount(department.getMemberToBeUploadedCount());
    }

    public List<UserRole> createUserRoles(User user, Long id, UserRoleDTO userRoleDTO) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        UserDTO userDTO = userRoleDTO.getUser();
        RoleType type = userRoleDTO.getType();

        User userCreateUpdate;
        switch (type) {
            case STAFF:
                userCreateUpdate = userService.createOrUpdateUser(userDTO, userService::getByEmail);
                break;
            case MEMBER:
                userCreateUpdate = userService.createOrUpdateUser(
                    userDTO, (email) -> userService.getByEmail(department, email, Role.MEMBER));
                break;
            default:
                throw new IllegalStateException("Unexpected user role type: " + type);
        }

        return createOrUpdateUserRoles(user, id, userCreateUpdate, userRoleDTO);
    }

    public List<UserRole> updateUserRoles(User user, Long id, Long userCreateUpdateId, UserRoleDTO userRoleDTO) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        User userCreateUpdate = userService.getById(userCreateUpdateId);
        return createOrUpdateUserRoles(user, id, userCreateUpdate, userRoleDTO);
    }

    public void deleteUserRoles(User user, Long id, Long userDeleteId, RoleType roleType) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        User userDelete = userService.getById(userDeleteId);
        userRoleService.deleteUserRoles(department, userDelete, roleType);
        eventProducer.produce(
            new ActivityEvent(this, id));
    }

    public void createOrUpdateUserRole(Long id, MemberDTO memberDTO) {
        Department department = (Department) resourceService.getById(id);
        UserDTO userDTO = memberDTO.getUser();
        User userCreateUpdate = userService.createOrUpdateUser(
            userDTO, (email) -> userService.getByEmail(department, email, Role.MEMBER));
        userRoleService.createOrUpdateUserRole(department, userCreateUpdate, memberDTO, ACCEPTED);
        resourceTaskService.completeTasks(department, MEMBER_TASKS);
    }

    @Transactional(isolation = SERIALIZABLE)
    public void decrementMemberCountPending(Long id) {
        ((Department) resourceService.getById(id)).decrementMemberToBeUploadedCount();
    }

    DemographicDataStatus makeDemographicDataStatus(User user, Department department) {
        DemographicDataStatus responseReadiness = new DemographicDataStatus();
        if (Stream.of(user.getGender(), user.getAgeRange(), user.getLocationNationality()).anyMatch(Objects::isNull)) {
            // User data incomplete
            responseReadiness.setRequireUserData(true);
        }

        if (department.getMemberCategories().isEmpty()) {
            // No member data required
            return responseReadiness;
        }

        UserRole userRole = userRoleService.getByResourceUserAndRole(department, user, MEMBER);
        if (userRole == null) {
            // Must be administrator - no member data required
            return responseReadiness;
        }

        MemberCategory memberCategory = userRole.getMemberCategory();
        String memberProgram = userRole.getMemberProgram();
        Integer memberYear = userRole.getMemberYear();
        LocalDate expiryDate = userRole.getExpiryDate();

        responseReadiness
            .setMemberCategory(memberCategory)
            .setMemberProgram(memberProgram)
            .setMemberYear(memberYear)
            .setExpiryDate(expiryDate);

        if (Stream.of(memberCategory, memberProgram, memberYear, expiryDate).anyMatch(Objects::isNull)) {
            // Member data incomplete
            return responseReadiness.setRequireMemberData(true);
        }

        LocalDate academicYearStart = getAcademicYearStart();
        if (academicYearStart.isAfter(userRole.getMemberDate())) {
            // Member data out of date
            return responseReadiness.setRequireMemberData(true);
        }

        // Member data complete and up to date
        return responseReadiness;
    }

    private List<UserRole> createOrUpdateUserRoles(User user, Long id, User userCreateUpdate, UserRoleDTO userRoleDTO) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        RoleType type = userRoleDTO.getType();
        switch (type) {
            case STAFF:
                List<Role> newRoles = ((StaffDTO) userRoleDTO).getRoles();
                List<Role> oldRoles = userRoleService.getByResourceAndUser(department, userCreateUpdate);

                List<UserRole> createdUserRoles =
                    newRoles
                        .stream()
                        .filter(role -> !oldRoles.contains(role))
                        .map(role -> userRoleService.createUserRole(department, userCreateUpdate, role))
                        .collect(toList());

                oldRoles
                    .stream()
                    .filter(role -> !newRoles.contains(role))
                    .forEach(role -> {
                        activityService.deleteActivities(department, userCreateUpdate, role);
                        userRoleService.deleteUserRole(department, userCreateUpdate, role);
                    });

                eventProducer.produce(
                    new ActivityEvent(this, id));
                return notifyStaffUserIfNew(user, id, createdUserRoles);
            case MEMBER:
                UserRole userRole = userRoleService.createOrUpdateUserRole(
                    department, userCreateUpdate, (MemberDTO) userRoleDTO, ACCEPTED);
                resourceTaskService.completeTasks(department, MEMBER_TASKS);
                eventProducer.produce(
                    new ActivityEvent(this, id));
                return singletonList(userRole);
            default:
                throw new IllegalStateException("Unexpected user role type: " + type);
        }
    }

    private void checkExistingMemberRequest(Department department, User user) {
        UserRole userRole = userRoleService.getByResourceUserAndRole(department, user, MEMBER);
        if (userRole == null) {
            return;
        }

        if (userRole.getState() == REJECTED) {
            throw new BoardForbiddenException(FORBIDDEN_PERMISSION, "Member request already rejected");
        }

        throw new BoardDuplicateException(DUPLICATE_PERMISSION, "Member request already submitted", userRole.getId());
    }

    private void checkValidMemberCategory(Department department, MemberCategory memberCategory) {
        List<String> memberCategories = department.getMemberCategoryStrings();
        if (memberCategories.isEmpty()) {
            if (memberCategory == null) {
                return;
            }

            throw new BoardException(FORBIDDEN_MEMBER_CATEGORIES, "Categories must not be specified");
        }

        if (memberCategory == null) {
            throw new BoardException(MISSING_MEMBER_CATEGORIES, "Categories must be specified");
        }

        if (memberCategories
            .stream()
            .noneMatch(name -> name.equals(memberCategory.name()))) {
            throw new BoardException(
                INVALID_MEMBER_CATEGORIES, "Valid categories must be specified - check parent categories");
        }
    }

    private void checkValidDemographicData(User user, Department department) {
        entityManager.flush();
        DemographicDataStatus dataStatus = makeDemographicDataStatus(user, department);
        if (dataStatus.isRequireUserData()) {
            throw new BoardException(INVALID_MEMBERSHIP, "User data not valid / complete");
        }

        if (dataStatus.isRequireMemberData()) {
            throw new BoardException(INVALID_MEMBERSHIP, "Member data not valid / complete");
        }
    }

    private List<UserRole> notifyStaffUserIfNew(User user, Long id, List<UserRole> userRoles) {
        Optional<UserRole> userRoleNotifyOptional =
            userRoles
                .stream()
                .filter(UserRole::isCreated)
                .findFirst();

        if (userRoleNotifyOptional.isPresent()) {
            UserRole userRoleNotify = userRoleNotifyOptional.get();
            User userNotify = userRoleNotify.getUser();

            if (Objects.equals(user, userNotify)) {
                return userRoles;
            }

            eventProducer.produce(
                new ActivityEvent(this, id,
                    singletonList(
                        new Activity()
                            .setUserId(userNotify.getId())
                            .setActivity(JOIN_DEPARTMENT_ACTIVITY))),
                new NotificationEvent(this, id,
                    singletonList(
                        new Notification()
                            .setInvitation(userRoleNotify.getUuid())
                            .setNotification(JOIN_DEPARTMENT_NOTIFICATION))));
        }

        return userRoles;
    }

}
