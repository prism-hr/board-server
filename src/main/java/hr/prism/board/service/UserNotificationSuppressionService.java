package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserNotificationSuppression;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserNotificationSuppressionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class UserNotificationSuppressionService {

    @Inject
    private UserNotificationSuppressionRepository userNotificationSuppressionRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    public UserNotificationSuppression postSuppression(String uuid, Long resourceId) {
        User user = userService.getCurrentUser();
        if (user == null && uuid != null) {
            user = userService.findByUuid(uuid);
        }

        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        Resource resource = resourceService.findOne(resourceId);
        UserNotificationSuppression userNotificationSuppression = userNotificationSuppressionRepository.findByUserAndResource(user, resource);
        if (userNotificationSuppressionRepository.findByUserAndResource(user, resource) == null) {
            userNotificationSuppression = userNotificationSuppressionRepository.save(new UserNotificationSuppression().setUser(user).setResource(resource));
        }

        return userNotificationSuppression;
    }

    public List<UserNotificationSuppression> postSuppressions() {
        User user = userService.getCurrentUserSecured();
        userNotificationSuppressionRepository.insertByUserId(user.getId());
        entityManager.flush();
        return userNotificationSuppressionRepository.findByUser(user);
    }

    public void deleteSuppressions(User user) {
        userNotificationSuppressionRepository.deleteByUser(user);
    }

    public void deleteSuppression(User user, Resource resource) {
        userNotificationSuppressionRepository.deleteByUserAndResource(user, resource);
    }

}
