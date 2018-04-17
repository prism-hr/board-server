package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.ResourceTask.MEMBER_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.NON_MEMBER_ROLES;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.emptyToNull;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@Service
@Transactional
public class UserRoleCacheService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActivityService activityService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private EventProducer eventProducer;

    public UserRole findByUuid(String uuid) {
        return userRoleRepository.findByUuid(uuid);
    }

    public UserRole createUserRole(User user, Resource resource, User userCreate,
                                   UserRoleDTO userRoleDTO, boolean notify) {
        if (Role.MEMBER == userRoleDTO.getRole()) {
            resourceTaskService.completeTasks(resource, MEMBER_TASKS);
        }

        return createUserRole(user, resource, userCreate, userRoleDTO, ACCEPTED, notify);
    }

    public UserRole createUserRole(User user, Resource resource, User userCreate, UserRoleDTO userRoleDTO,
                                   State state, boolean notify) {
        Role role = userRoleDTO.getRole();
        Scope scope = resource.getScope();

        if (notify && (Objects.equals(user, userCreate) ||
            !userRoleRepository.findByResourceAndUserAndRoles(resource, userCreate, NON_MEMBER_ROLES).isEmpty())) {
            notify = false;
        }

        UserRole userRole = userRoleRepository.save(
            new UserRole()
                .setUuid(randomUUID().toString())
                .setResource(resource)
                .setUser(userCreate)
                .setEmail(emptyToNull(userRoleDTO.getEmail()))
                .setRole(role)
                .setState(state)
                .setExpiryDate(userRoleDTO.getExpiryDate()));

        userRole.setCreatorId(resource.getCreatorId());
        updateMembershipData(userRole, userRoleDTO);

        if (notify) {
            String scopeName = scope.name();
            Long resourceId = resource.getId();

            eventProducer.produce(
                new ActivityEvent(this, resourceId, false,
                    singletonList(
                        new Activity()
                            .setUserId(userCreate.getId())
                            .setActivity(hr.prism.board.enums.Activity.valueOf("JOIN_" + scopeName + "_ACTIVITY")))),
                new NotificationEvent(this, resourceId,
                    singletonList(
                        new Notification()
                            .setInvitation(userRole.getUuid())
                            .setNotification(
                                hr.prism.board.enums.Notification.valueOf("JOIN_" + scopeName + "_NOTIFICATION")))));
        }

        return userRole;
    }

    public void deleteResourceUser(Resource resource, User user) {
        deleteUserRoles(resource, user);
        checkSafety(resource, IRREMOVABLE_USER);
    }

    public void updateUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {
        deleteUserRoles(resource, user);
        entityManager.flush();

        createUserRole(currentUser, resource, user, userRoleDTO, false);
        checkSafety(resource, IRREMOVABLE_USER_ROLE);
    }

    public void deleteUserRole(Resource resource, User user, Role role) {
        activityService.deleteActivities(resource, user, role);
        userRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
    }

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

    public void updateMembershipData(UserRole userRole, UserRoleDTO userRoleDTO) {
        boolean updated = false;
        boolean clearStudyData = false;
        MemberCategory oldMemberCategory = userRole.getMemberCategory();
        MemberCategory newMemberCategory = userRoleDTO.getMemberCategory();
        if (newMemberCategory != null) {
            Resource resource = userRole.getResource();
            Resource department = resourceService.findByResourceAndEnclosingScope(resource, DEPARTMENT);
            resourceService.validateCategories(department, MEMBER, singletonList(newMemberCategory.name()),
                MISSING_USER_ROLE_MEMBER_CATEGORIES,
                INVALID_USER_ROLE_MEMBER_CATEGORIES,
                CORRUPTED_USER_ROLE_MEMBER_CATEGORIES);

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
        if (resource.getScope() == DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, ADMINISTRATOR);
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
