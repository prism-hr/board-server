package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserNotificationSuppression;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserNotificationSuppressionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.State.ACTIVE_USER_ROLE_STATE_STRINGS;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_RESOURCE;
import static hr.prism.board.exception.ExceptionCode.UNSUPPRESSABLE_RESOURCE;

@Service
@Transactional
public class UserNotificationSuppressionService {

    private final UserNotificationSuppressionRepository userNotificationSuppressionRepository;

    private final ResourceService resourceService;

    private final UserRoleService userRoleService;

    private final UserService userService;

    private final EntityManager entityManager;

    @Inject
    public UserNotificationSuppressionService(
        UserNotificationSuppressionRepository userNotificationSuppressionRepository, ResourceService resourceService,
        UserRoleService userRoleService, UserService userService, EntityManager entityManager) {
        this.userNotificationSuppressionRepository = userNotificationSuppressionRepository;
        this.resourceService = resourceService;
        this.userRoleService = userRoleService;
        this.userService = userService;
        this.entityManager = entityManager;
    }

    public List<Resource> getSuppressions() {
        User user = userService.getUserSecured();
        return getSuppressions(user);
    }

    public Resource createSuppression(String uuid, Long resourceId) {
        User user = userService.getUser();
        if (user == null && uuid != null) {
            user = userService.getByUuid(uuid);
        }

        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNKNOWN_USER, "User cannot be found");
        }

        Resource resource = resourceService.getById(resourceId);
        Scope scope = resource.getScope();
        if (scope != BOARD) {
            throw new BoardException(UNSUPPRESSABLE_RESOURCE,
                "Notifications cannot be suppressed for resource of scope: " + scope);
        }

        if (userRoleService.getByResourceAndUser(resource, user).isEmpty()) {
            throw new BoardForbiddenException(FORBIDDEN_RESOURCE,
                "User cannot suppress notifications for resource: " + scope + " ID: " + resourceId);
        }

        if (userNotificationSuppressionRepository.findByUserAndResource(user, resource) == null) {
            userNotificationSuppressionRepository.save(
                new UserNotificationSuppression()
                    .setUser(user)
                    .setResource(resource));
        }

        resource.setNotificationSuppressedForUser(true);
        return resource;
    }

    public List<Resource> createSuppressionsForAllResources() {
        User user = userService.getUserSecured();
        userNotificationSuppressionRepository.insertByUserId(
            user.getId(), LocalDateTime.now(), BOARD.name(), ACTIVE_USER_ROLE_STATE_STRINGS);
        entityManager.flush();
        return getSuppressions(user);
    }

    public void deleteSuppressions() {
        User user = userService.getUserSecured();
        userNotificationSuppressionRepository.deleteByUser(user);
    }

    public void deleteSuppression(Long resourceId) {
        User user = userService.getUserSecured();
        userNotificationSuppressionRepository.deleteByUserAndResourceId(user, resourceId);
    }

    private List<Resource> getSuppressions(User user) {
        List<Resource> resources = resourceService.getSuppressableResources(BOARD, user);
        List<Resource> suppressedResources = userNotificationSuppressionRepository.findByUser(user);
        resources.forEach(resource ->
            resource.setNotificationSuppressedForUser(suppressedResources.contains(resource)));
        return resources;
    }

}
