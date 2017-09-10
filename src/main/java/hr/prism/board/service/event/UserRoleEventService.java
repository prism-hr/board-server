package hr.prism.board.service.event;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.event.UserRoleEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.UserRoleService;
import hr.prism.board.service.cache.UserCacheService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.util.List;

@Service
public class UserRoleEventService {

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long creatorId, Long resourceId, List<UserRoleDTO> userRoleDTOs) {
        applicationEventPublisher.publishEvent(new UserRoleEvent(source, creatorId, resourceId, userRoleDTOs));
    }

    @Async
    @TransactionalEventListener
    public void createResourceUsersAsync(UserRoleEvent userRoleEvent) {
        createResourceUsers(userRoleEvent, true);
    }

    protected void createResourceUsers(UserRoleEvent userRoleEvent, boolean invokedAsynchronously) {
        Long resourceId = userRoleEvent.getResourceId();
        try {
            User currentUser = userCacheService.findOne(userRoleEvent.getCreatorId());
            for (UserRoleDTO userRoleDTO : userRoleEvent.getUserRoles()) {
                userRoleService.createOrUpdateResourceUser(currentUser, resourceId, userRoleDTO, invokedAsynchronously);
            }
        } catch (Throwable t) {
            throw new BoardException(ExceptionCode.UNPROCESSABLE_RESOURCE_USER, "Unable to bulk create user roles", t);
        } finally {
            departmentService.unsetMemberCountProvisional(resourceId);
        }
    }

}
