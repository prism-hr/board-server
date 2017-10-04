package hr.prism.board.service.cache;

import hr.prism.board.domain.*;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserRoleCacheService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActivityService activityService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @PersistenceContext
    private EntityManager entityManager;

    public UserRole findByUuid(String uuid) {
        return userRoleRepository.findByUuid(uuid);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public UserRole createUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO, boolean notify) {
        return createUserRole(currentUser, resource, user, userRoleDTO, State.ACCEPTED, notify);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public UserRole createUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO, State state, boolean notify) {
        Role role = userRoleDTO.getRole();
        Scope scope = resource.getScope();

        if (notify && (role == Role.MEMBER || Objects.equals(currentUser, user)
            || !userRoleRepository.findByResourceAndUserAndNotRole(resource, user, Role.MEMBER).isEmpty())) {
            notify = false;
        }

        UserRole userRole = userRoleRepository.save(
            new UserRole().setUuid(UUID.randomUUID().toString()).setResource(resource).setUser(user).setRole(role).setState(state).setExpiryDate(userRoleDTO.getExpiryDate()));
        updateUserRoleMemberData(userRole, userRoleDTO);
        updateUserRolesSummary(resource);

        if (notify) {
            String scopeName = scope.name();
            Long resourceId = resource.getId();

            Activity activity = new Activity().setUserId(user.getId())
                .setActivity(hr.prism.board.enums.Activity.valueOf("JOIN_" + scopeName + "_ACTIVITY"));
            activityEventService.publishEvent(this, resourceId, Collections.singletonList(activity));

            Notification notification = new Notification().setInvitation(userRole.getUuid())
                .setNotification(hr.prism.board.enums.Notification.valueOf("JOIN_" + scopeName + "_NOTIFICATION"));
            notificationEventService.publishEvent(this, resourceId, Collections.singletonList(notification));
        }

        return userRole;
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void deleteResourceUser(Resource resource, User user) {
        deleteUserRoles(resource, user);
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER);
        updateUserRolesSummary(resource);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void updateResourceUser(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {
        deleteUserRoles(resource, user);
        entityManager.flush();

        createUserRole(currentUser, resource, user, userRoleDTO, false);
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER_ROLE);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void deleteUserRole(Resource resource, User user, Role role) {
        activityService.deleteActivities(resource, user, role);
        userRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
    }

    @CacheEvict(key = "#newUser.id", value = "users")
    public void mergeUserRoles(User newUser, User oldUser) {
        Map<Pair<Resource, Role>, UserRole> newUserRoles = new HashMap<>();
        Map<Pair<Resource, Role>, UserRole> oldUserRoles = new HashMap<>();
        userRoleRepository.findByUsersOrderByUser(Arrays.asList(newUser, oldUser)).forEach(userRole -> {
            if (userRole.getUser().equals(newUser)) {
                newUserRoles.put(Pair.of(userRole.getResource(), userRole.getRole()), userRole);
            } else {
                oldUserRoles.put(Pair.of(userRole.getResource(), userRole.getRole()), userRole);
            }
        });

        List<UserRole> deletes = new ArrayList<>();
        for (Map.Entry<Pair<Resource, Role>, UserRole> oldUserRoleEntry : oldUserRoles.entrySet()) {
            if (newUserRoles.containsKey(oldUserRoleEntry.getKey())) {
                deletes.add(oldUserRoleEntry.getValue());
            }
        }

        if (!deletes.isEmpty()) {
            activityService.deleteActivities(deletes);
            userRoleRepository.deleteByIds(deletes.stream().map(UserRole::getId).collect(Collectors.toList()));
        }

        userRoleRepository.updateByUser(newUser, oldUser);
    }

    public void updateUserRolesSummary(Resource resource) {
        entityManager.flush();
        LocalDate baseline = LocalDate.now();
        if (resource instanceof Department) {
            ((Department) resource).setMemberCount(userRoleRepository.findSummaryByResourceAndRole(resource, Role.MEMBER, State.ACTIVE_USER_ROLE_STATES, baseline).getCount());
        } else if (resource instanceof Board) {
            ((Board) resource).setAuthorCount(userRoleRepository.findSummaryByResourceAndRole(resource, Role.AUTHOR, State.ACTIVE_USER_ROLE_STATES, baseline).getCount());
        }
    }

    public void updateUserRoleMemberData(UserRole userRole, UserRoleDTO userRoleDTO) {
        boolean updated = false;
        boolean clearStudyData = false;
        MemberCategory oldMemberCategory = userRole.getMemberCategory();
        MemberCategory newMemberCategory = userRoleDTO.getMemberCategory();
        if (newMemberCategory != null) {
            userRole.setMemberCategory(newMemberCategory);
            clearStudyData = newMemberCategory != oldMemberCategory;
            updated = true;
        }

        String memberProgram = userRoleDTO.getMemberProgram();
        if (memberProgram != null || clearStudyData) {
            userRole.setMemberProgram(memberProgram);
            updated = true;
        }

        Integer memberYear = userRoleDTO.getMemberYear();
        if (memberYear != null || clearStudyData) {
            userRole.setMemberYear(memberYear);
            updated = true;
        }

        if (updated) {
            userRole.setMemberDate(LocalDate.now());
        }
    }

    private void checkSafety(Resource resource, ExceptionCode exceptionCode) {
        if (resource.getScope() == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new BoardException(exceptionCode, "Cannot remove last remaining administrator");
            }
        }
    }

    private void deleteUserRoles(Resource resource, User user) {
        activityService.deleteActivities(resource, user);
        userRoleRepository.deleteByResourceAndUser(resource, user);
    }

}
