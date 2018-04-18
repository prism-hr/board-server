package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.MemberDTO;
import hr.prism.board.dto.StaffDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.State;
import hr.prism.board.enums.UserRoleType;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.DepartmentMemberEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.DemographicDataStatusRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Activity.JOIN_DEPARTMENT_ACTIVITY;
import static hr.prism.board.enums.Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY;
import static hr.prism.board.enums.Notification.JOIN_DEPARTMENT_NOTIFICATION;
import static hr.prism.board.enums.Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.*;
import static java.util.Collections.singletonList;

@Service
@Transactional
public class DepartmentUserService {

    private final NewUserService userService;

    private final ResourceService resourceService;

    private final ActionService actionService;

    private final NewUserRoleService userRoleService;

    private final ActivityService activityService;

    private final ResourceTaskService resourceTaskService;

    private final EventProducer eventProducer;

    @Inject
    public DepartmentUserService(NewUserService userService, ResourceService resourceService,
                                 ActionService actionService, NewUserRoleService userRoleService,
                                 ResourceTaskService resourceTaskService, ActivityService activityService,
                                 EventProducer eventProducer) {
        this.userService = userService;
        this.resourceService = resourceService;
        this.actionService = actionService;
        this.userRoleService = userRoleService;
        this.activityService = activityService;
        this.resourceTaskService = resourceTaskService;
        this.eventProducer = eventProducer;
    }

    public List<UserRepresentation> findUsers(Long id, String searchTerm) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);
        return userService.findUsers(searchTerm);
    }

    public Department createMembers(Long id, List<MemberDTO> memberDTOs) {
        User currentUser = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(currentUser, DEPARTMENT, id);
        return (Department) actionService.executeAction(currentUser, department, EDIT, () -> {
            department.increaseMemberTobeUploadedCount((long) memberDTOs.size());
            eventProducer.produce(new DepartmentMemberEvent(this, id, memberDTOs));
            department.setLastMemberTimestamp(LocalDateTime.now());
            return department;
        });
    }

    public User createMembershipRequest(Long id, MemberDTO memberDTO) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.findOne(id);
        checkExistingMemberRequest(department, user);
        checkValidMemberCategory(department, memberDTO.getMemberCategory());

        UserRole userRole = userRoleService.createOrUpdateUserRole(department, memberDTO, PENDING);
        validateMembership(user, department, BoardException.class, INVALID_MEMBERSHIP);

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

        return user;
    }

    public UserRole viewMembershipRequest(Long id, Long userId) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);
        UserRole userRole = userRoleService.getByResourceAndUserIdAndRole(department, userId, MEMBER);
        activityService.viewActivity(userRole.getActivity(), user);
        return userRole.setViewed(true);
    }

    public void reviewMembershipRequest(Long id, Long userId, State state) {
        User user = userService.getCurrentUserSecured();
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

    public User updateMembership(Long id, MemberDTO memberDTO) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.findOne(id);

        UserRole userRole = userRoleService.getByResourceUserAndRole(department, user, MEMBER);
        if (userRole == null || userRole.getState() == REJECTED) {
            throw new BoardForbiddenException(FORBIDDEN_PERMISSION, "User is not a member");
        }

        UserDTO userDTO = memberDTO.getUser();
        userService.updateMembership(user, userDTO);
        userRoleService.updateMembership(userRole, memberDTO);
        validateMembership(user, department, BoardException.class, INVALID_MEMBERSHIP);
        return user;
    }

    public List<UserRole> createOrUpdateUserRoles(Long id, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        UserRoleType type = userRoleDTO.getType();
        switch (type) {
            case STAFF:
                // TODO: work out how to delete selectively
                List<UserRole> userRoles = userRoleService.createOrUpdateUserRoles(department, (StaffDTO) userRoleDTO);
                return notifyStaffUserIfNew(user, id, userRoles);
            case MEMBER:
                return singletonList(
                    userRoleService.createOrUpdateUserRole(department, (MemberDTO) userRoleDTO, ACCEPTED));
            default:
                throw new IllegalStateException("Unexpected user role type: " + type);
        }
    }

    public void createOrUpdateUserRole(Long id, MemberDTO memberDTO) {
        Resource resource = resourceService.findOne(id);
        userRoleService.createOrUpdateUserRole(resource, memberDTO, ACCEPTED);
    }


    public List<UserRole> updateUserRoles(Long id, Long userUpdateId, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        UserRoleType type = userRoleDTO.getType();
        switch (type) {
            case STAFF:
                List<UserRole> userRoles = userRoleService.createOrUpdateUserRoles(department, (StaffDTO) userRoleDTO);
                return notifyStaffUserIfNew(user, id, userRoles);
            case MEMBER:
                UserRole userRole =
                    userRoleService.createOrUpdateUserRole(department, (MemberDTO) userRoleDTO, ACCEPTED);
                activityService.sendActivities(department);
                return singletonList(userRole);
            default:
                throw new IllegalStateException("Unexpected user role type: " + type);
        }
    }

    public void validateMembership(User user, Department department, Class<? extends BoardException> exceptionClass,
                                   ExceptionCode exceptionCode) {
        DemographicDataStatusRepresentation dataStatus =
            userRoleService.makeDemographicDataStatus(user, department, true);
        if (!dataStatus.isReady()) {
            if (dataStatus.isRequireUserDemographicData()) {
                throwFor(exceptionClass, exceptionCode, "User demographic data not valid");
            }

            throwFor(exceptionClass, exceptionCode, "User role demographic data not valid");
        }
    }

    public void decrementMemberCountPending(Long id) {
        ((Department) resourceService.findOne(id)).decrementMemberToBeUploadedCount();
    }

    private void checkExistingMemberRequest(Department department, User user) {
        UserRole userRole = userRoleService.getByResourceUserAndRole(department, user, MEMBER);
        if (userRole == null) {
            return;
        }

        if (userRole.getState() == REJECTED) {
            throw new BoardForbiddenException(FORBIDDEN_PERMISSION, "Member request already rejected");
        }

        throw new BoardException(DUPLICATE_PERMISSION, "Member request already submitted");
    }

    private void checkValidMemberCategory(Department department, MemberCategory memberCategory) {
        List<ResourceCategory> referenceCategories = department.getCategories(CategoryType.MEMBER);
        if (referenceCategories.isEmpty()) {
            if (memberCategory == null) {
                return;
            }

            throw new BoardException(CORRUPTED_USER_ROLE_MEMBER_CATEGORIES, "Categories must not be specified");
        }

        if (memberCategory == null) {
            throw new BoardException(MISSING_USER_ROLE_MEMBER_CATEGORIES, "Categories must be specified");
        }

        if (referenceCategories
            .stream()
            .map(ResourceCategory::getName)
            .noneMatch(name -> name.equals(memberCategory.name()))) {
            throw new BoardException(
                INVALID_USER_ROLE_MEMBER_CATEGORIES, "Valid categories must be specified - check parent categories");
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
                new ActivityEvent(this, id, false,
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
