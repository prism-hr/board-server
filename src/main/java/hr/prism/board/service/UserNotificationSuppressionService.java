package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserNotificationSuppression;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.mapper.UserNotificationSuppressionMapper;
import hr.prism.board.repository.UserNotificationSuppressionRepository;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_RESOURCE;
import static hr.prism.board.exception.ExceptionCode.UNSUPPRESSABLE_RESOURCE;

@Service
@Transactional
public class UserNotificationSuppressionService {

    private final UserNotificationSuppressionRepository userNotificationSuppressionRepository;

    private final ResourceService resourceService;

    private final NewUserRoleService userRoleService;

    private final NewUserService userService;

    private final UserNotificationSuppressionMapper userNotificationSuppressionMapper;

    private final EntityManager entityManager;

    @Inject
    public UserNotificationSuppressionService(
        UserNotificationSuppressionRepository userNotificationSuppressionRepository, ResourceService resourceService,
        NewUserRoleService userRoleService, NewUserService userService,
        UserNotificationSuppressionMapper userNotificationSuppressionMapper, EntityManager entityManager) {
        this.userNotificationSuppressionRepository = userNotificationSuppressionRepository;
        this.resourceService = resourceService;
        this.userRoleService = userRoleService;
        this.userService = userService;
        this.userNotificationSuppressionMapper = userNotificationSuppressionMapper;
        this.entityManager = entityManager;
    }

    public List<UserNotificationSuppressionRepresentation> getSuppressions() {
        User user = userService.requireAuthenticatedUser();
        return getSuppressions(user);
    }

    public List<UserNotificationSuppressionRepresentation> getSuppressions(User user) {
        Collection<Resource> resources = resourceService.getSuppressableResources(Scope.BOARD, user);
        List<Resource> suppressedResources =
            userNotificationSuppressionRepository.findByUser(user).stream().map(UserNotificationSuppression::getResource).collect(Collectors.toList());

        List<UserNotificationSuppressionRepresentation> representations = new ArrayList<>();
        for (Resource resource : resources) {
            representations.add(userNotificationSuppressionMapper.apply(resource, suppressedResources.contains(resource)));
        }

        return representations;
    }

    public UserNotificationSuppressionRepresentation postSuppression(String uuid, Long resourceId) {
        User user = userService.getAuthenticatedUser();
        if (user == null && uuid != null) {
            user = userService.getByUuid(uuid);
        }

        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNKNOWN_USER, "User cannot be found");
        }

        Resource resource = resourceService.findOne(resourceId);
        Scope scope = resource.getScope();
        if (scope != Scope.BOARD) {
            throw new BoardException(UNSUPPRESSABLE_RESOURCE, "Notifications cannot be suppressed for resource of scope: " + scope);
        }

        if (userRoleService.getByResourceAndUser(resource, user).isEmpty()) {
            throw new BoardForbiddenException(FORBIDDEN_RESOURCE, "User cannot access resource: " + scope + ": " + resourceId);
        }

        if (userNotificationSuppressionRepository.findByUserAndResource(user, resource) == null) {
            userNotificationSuppressionRepository.save(new UserNotificationSuppression().setUser(user).setResource(resource));
        }

        return userNotificationSuppressionMapper.apply(resource, true);
    }

    public List<UserNotificationSuppressionRepresentation> postSuppressions() {
        User user = userService.requireAuthenticatedUser();
        userNotificationSuppressionRepository.insertByUserId(user.getId(), LocalDateTime.now(), Scope.BOARD.name(), State.ACTIVE_USER_ROLE_STATE_STRINGS);
        entityManager.flush();
        return getSuppressions(user);
    }

    public void deleteSuppressions() {
        User user = userService.requireAuthenticatedUser();
        userNotificationSuppressionRepository.deleteByUser(user);
    }

    public void deleteSuppression(Long resourceId) {
        User user = userService.requireAuthenticatedUser();
        userNotificationSuppressionRepository.deleteByUserAndResourceId(user, resourceId);
    }

}
