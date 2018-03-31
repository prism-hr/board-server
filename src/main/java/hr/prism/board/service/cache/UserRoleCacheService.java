package hr.prism.board.service.cache;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.ResourceTaskService;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UserRoleCacheService {

    private static final List<hr.prism.board.enums.ResourceTask> MEMBER_TASKS = ImmutableList.of(
        hr.prism.board.enums.ResourceTask.CREATE_MEMBER, hr.prism.board.enums.ResourceTask.UPDATE_MEMBER);

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActivityService activityService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private EntityManager entityManager;

    public UserRole findByUuid(String uuid) {
        return userRoleRepository.findByUuid(uuid);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public UserRole createUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO, boolean notify) {
        if (Role.MEMBER.equals(userRoleDTO.getRole())) {
            resourceTaskService.completeTasks(resource, MEMBER_TASKS);
        }

        return createUserRole(currentUser, resource, user, userRoleDTO, State.ACCEPTED, notify);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public UserRole createUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO, State state, boolean notify) {
        Role role = userRoleDTO.getRole();
        Scope scope = resource.getScope();

        if (notify && (Objects.equals(currentUser, user) || !userRoleRepository.findByResourceAndUserAndRoles(resource, user, Role.NOTIFIABLE).isEmpty())) {
            notify = false;
        }

        UserRole userRole = userRoleRepository.save(new UserRole().setUuid(UUID.randomUUID().toString()).setResource(resource)
            .setUser(user).setEmail(BoardUtils.emptyToNull(userRoleDTO.getEmail())).setRole(role).setState(state).setExpiryDate(userRoleDTO.getExpiryDate()));
        userRole.setCreatorId(resource.getCreatorId());

        updateUserRoleDemographicData(userRole, userRoleDTO);
        updateStatistics(resource);

        if (notify) {
            String scopeName = scope.name();
            Long resourceId = resource.getId();

            Activity activity = new Activity().setUserId(user.getId())
                .setActivity(hr.prism.board.enums.Activity.valueOf("JOIN_" + scopeName + "_ACTIVITY"));
            activityEventService.publishEvent(this, resourceId, false, Collections.singletonList(activity));

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
        updateStatistics(resource);
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

    public void updateStatistics(Resource resource) {
        entityManager.flush();
        LocalDate baseline = LocalDate.now();
        if (resource instanceof Department) {
            Department department = (Department) resource;
            department.setAuthorCount(userRoleRepository.findSummaryByResourceAndRole(resource, Role.AUTHOR, State.ACTIVE_USER_ROLE_STATES, baseline).getCount());
            department.setMemberCount(userRoleRepository.findSummaryByResourceAndRole(resource, Role.MEMBER, State.ACTIVE_USER_ROLE_STATES, baseline).getCount());
        }
    }

    public void updateUserRoleDemographicData(UserRole userRole, UserRoleDTO userRoleDTO) {
        boolean updated = false;
        boolean clearStudyData = false;
        MemberCategory oldMemberCategory = userRole.getMemberCategory();
        MemberCategory newMemberCategory = userRoleDTO.getMemberCategory();
        if (newMemberCategory != null) {
            Resource department = resourceService.findByResourceAndEnclosingScope(userRole.getResource(), Scope.DEPARTMENT);
            resourceService.validateCategories(department, CategoryType.MEMBER, Collections.singletonList(newMemberCategory.name()),
                ExceptionCode.MISSING_USER_ROLE_MEMBER_CATEGORIES, ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, ExceptionCode.CORRUPTED_USER_ROLE_MEMBER_CATEGORIES);

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
