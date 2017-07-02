package hr.prism.board.service.cache;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.domain.UserRoleCategory;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleCategoryRepository;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.workflow.Notification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@Transactional
public class UserRoleCacheService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCategoryRepository userRoleCategoryRepository;

    @Inject
    private ResourceService resourceService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @PersistenceContext
    private EntityManager entityManager;

    @CacheEvict(key = "#user.id", value = "users")
    public void createUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {
        Role role = userRoleDTO.getRole();
        Scope scope = resource.getScope();

        boolean notify = true;
        if (role == Role.MEMBER || scope == Scope.POST || Objects.equals(currentUser, user)
            || !userRoleRepository.findByResourceAndUserAndNotRole(resource, user, Role.MEMBER).isEmpty()) {
            notify = false;
        }

        UserRole userRole = userRoleRepository.save(
            new UserRole().setResource(resource).setUser(user).setRole(role).setExpiryDate(userRoleDTO.getExpiryDate()));

        List<MemberCategory> newCategories = userRoleDTO.getCategories();
        if (userRoleDTO.getRole() == Role.MEMBER) {
            Resource department = resourceService.findByResourceAndEnclosingScope(resource, Scope.DEPARTMENT);
            resourceService.validateCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(newCategories),
                ExceptionCode.MISSING_USER_ROLE_MEMBER_CATEGORIES,
                ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES,
                ExceptionCode.CORRUPTED_USER_ROLE_MEMBER_CATEGORIES);

            IntStream.range(0, newCategories.size())
                .forEach(index -> {
                    MemberCategory newCategory = newCategories.get(index);
                    UserRoleCategory userRoleCategory = new UserRoleCategory();
                    userRoleCategory.setUserRole(userRole);
                    userRoleCategory.setName(newCategory);
                    userRoleCategory.setOrdinal(index);
                    userRoleCategoryRepository.save(userRoleCategory);
                });
        }

        if (notify) {
            Notification notification = new Notification().setUserId(user.getId()).setExcludingCreator(true).setNotification("join_" + scope.name().toLowerCase());
            notificationEventService.publishEvent(this, currentUser.getId(), resource.getId(), Collections.singletonList(notification));
        }
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void deleteResourceUser(Resource resource, User user) {
        userRoleCategoryRepository.deleteByResourceAndUser(resource, user);
        userRoleRepository.deleteByResourceAndUser(resource, user);
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void updateResourceUser(User currentUser, Resource resource, User user, ResourceUserDTO resourceUserDTO) {
        if (resourceUserDTO.getRoles().isEmpty()) {
            throw new BoardException(ExceptionCode.IRREMOVABLE_USER_ROLE);
        }

        userRoleCategoryRepository.deleteByResourceAndUser(resource, user);
        userRoleRepository.deleteByResourceAndUser(resource, user);
        entityManager.flush();

        for (UserRoleDTO userRoleDTO : resourceUserDTO.getRoles()) {
            createUserRole(currentUser, resource, user, userRoleDTO);
        }

        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER_ROLE);
    }

    private void checkSafety(Resource resource, ExceptionCode exceptionCode) {
        if (resource.getScope() == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new BoardException(exceptionCode);
            }
        }
    }

}
