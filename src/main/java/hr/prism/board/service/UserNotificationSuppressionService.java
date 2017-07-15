package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserNotificationSuppressionRepository;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserNotificationSuppressionService {

    @Inject
    private UserNotificationSuppressionRepository userNotificationSuppressionRepository;

    @Inject
    private ActionService actionService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<UserNotificationSuppressionRepresentation> getSuppressions() {
        User user = userService.getCurrentUserSecured();
        return getSuppressions(user);
    }

    public List<UserNotificationSuppressionRepresentation> getSuppressions(User user) {
        Collection<Resource> resources = resourceService.getSuppressableResources(user, Scope.BOARD);
        List<Resource> suppressedResources =
            userNotificationSuppressionRepository.findByUser(user).stream().map(UserNotificationSuppression::getResource).collect(Collectors.toList());

        List<UserNotificationSuppressionRepresentation> representations = new ArrayList<>();
        for (Resource resource : resources) {
            representations.add(map((Board) resource, suppressedResources.contains(resource)));
        }

        return representations;
    }

    public UserNotificationSuppressionRepresentation postSuppression(String uuid, Long resourceId) {
        User user = userService.getCurrentUser();
        if (user == null && uuid != null) {
            user = userService.findByUuid(uuid);
        }

        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        Resource resource = resourceService.findOne(resourceId);
        if (resource.getScope() != Scope.BOARD) {
            throw new BoardException(ExceptionCode.UNSUPPRESSABLE_RESOURCE);
        }

        if (userRoleService.findByResourceAndUser(resource, user).isEmpty()) {
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_RESOURCE);
        }

        if (userNotificationSuppressionRepository.findByUserAndResource(user, resource) == null) {
            userNotificationSuppressionRepository.save(new UserNotificationSuppression().setUser(user).setResource(resource));
        }

        return map((Board) resource, true);
    }

    public List<UserNotificationSuppressionRepresentation> postSuppressions() {
        User user = userService.getCurrentUserSecured();
        userNotificationSuppressionRepository.insertByUserId(user.getId(), Scope.BOARD.name());
        entityManager.flush();
        return getSuppressions(user);
    }

    public void deleteSuppressions() {
        User user = userService.getCurrentUserSecured();
        userNotificationSuppressionRepository.deleteByUser(user);
    }

    public void deleteSuppression(Long resourceId) {
        User user = userService.getCurrentUserSecured();
        userNotificationSuppressionRepository.deleteByUserAndResourceId(user, resourceId);
    }

    private UserNotificationSuppressionRepresentation map(Board board, Boolean suppressed) {
        Document documentLogo = board.getDocumentLogo();
        DocumentRepresentation documentLogoRepresentation = null;
        if (documentLogo != null) {
            documentLogoRepresentation =
                new DocumentRepresentation()
                    .setCloudinaryId(documentLogo.getCloudinaryId())
                    .setCloudinaryUrl(documentLogo.getCloudinaryUrl())
                    .setFileName(documentLogo.getFileName());
        }

        Resource parent = board.getParent();
        BoardRepresentation representation =
            ((BoardRepresentation) new BoardRepresentation()
                .setId(board.getId())
                .setName(board.getName()))
                .setDocumentLogo(documentLogoRepresentation)
                .setDepartment(
                    (DepartmentRepresentation) new DepartmentRepresentation()
                        .setId(parent.getId())
                        .setName(parent.getName()));

        return new UserNotificationSuppressionRepresentation()
            .setResource(representation)
            .setSuppressed(suppressed);
    }

}
