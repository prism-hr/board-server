package hr.prism.board.service.cache;

import hr.prism.board.domain.*;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleCategoryRepository;
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
import java.util.stream.IntStream;

import hr.prism.board.workflow.Activity;

@Service
@Transactional
public class UserRoleCacheService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCategoryRepository userRoleCategoryRepository;

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
        createUserRoleCategories(userRole, userRoleDTO);
        updateUserRolesSummary(resource);

        if (notify) {
            String scopeName = scope.name();
            Activity activity = new Activity()
                .setActivity(hr.prism.board.enums.Activity.valueOf("JOIN_" + scopeName + "_ACTIVITY"));

            Notification notification = new Notification().setInvitation(userRole.getUuid())
                .setNotification(hr.prism.board.enums.Notification.valueOf("JOIN_" + scopeName + "_NOTIFICATION"));
            notificationEventService.publishEvent(this, resource.getId(), Collections.singletonList(notification));
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
        userRoleCategoryRepository.deleteByResourceAndUserAndRole(resource, user, role);
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

        for (Map.Entry<Pair<Resource, Role>, UserRole> oldUserRoleEntry : oldUserRoles.entrySet()) {
            if (newUserRoles.containsKey(oldUserRoleEntry.getKey())) {
                UserRole oldUserRole = oldUserRoleEntry.getValue();
                deleteUserRole(oldUserRole.getResource(), oldUserRole.getUser(), oldUserRole.getRole());
            }
        }

        userRoleRepository.updateByUser(newUser, oldUser);
    }

    public void createUserRoleCategories(UserRole userRole, UserRoleDTO userRoleDTO) {
        if (userRoleDTO.getRole() == Role.MEMBER) {
            Resource resource = userRole.getResource();
            List<MemberCategory> categories = userRoleDTO.getCategories();

            Resource department = resourceService.findByResourceAndEnclosingScope(resource, Scope.DEPARTMENT);
            resourceService.validateCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(categories),
                ExceptionCode.MISSING_USER_ROLE_MEMBER_CATEGORIES, ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, ExceptionCode.CORRUPTED_USER_ROLE_MEMBER_CATEGORIES);

            if (categories != null) {
                IntStream.range(0, categories.size())
                    .forEach(index -> {
                        MemberCategory newCategory = categories.get(index);
                        UserRoleCategory userRoleCategory = new UserRoleCategory();
                        userRoleCategory.setUserRole(userRole);
                        userRoleCategory.setName(newCategory);
                        userRoleCategory.setOrdinal(index);
                        userRole.getCategories().add(userRoleCategory);
                        userRoleCategoryRepository.save(userRoleCategory);
                    });
            }
        }
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

    public void deleteUserRoleCategories(Resource resource, User user) {
        userRoleCategoryRepository.deleteByResourceAndUser(resource, user);
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
        userRoleCategoryRepository.deleteByResourceAndUser(resource, user);
        userRoleRepository.deleteByResourceAndUser(resource, user);
    }

}
