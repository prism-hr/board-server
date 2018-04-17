package hr.prism.board.event.consumer;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.event.UserRoleEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.service.DepartmentUserService;
import hr.prism.board.service.UserCacheService;
import hr.prism.board.service.UserRoleService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;

import static hr.prism.board.exception.ExceptionCode.UNPROCESSABLE_RESOURCE_USER;

@Component
public class UserRoleEventConsumer {

    @Inject
    private final UserCacheService userCacheService;

    @Inject
    private final UserRoleService userRoleService;

    @Inject
    private final DepartmentUserService departmentUserService;

    @Inject
    public UserRoleEventConsumer(UserCacheService userCacheService, UserRoleService userRoleService,
                                 DepartmentUserService departmentUserService) {
        this.userCacheService = userCacheService;
        this.userRoleService = userRoleService;
        this.departmentUserService = departmentUserService;
    }

    @Async
    @TransactionalEventListener
    public void consume(UserRoleEvent userRoleEvent) {
        Long resourceId = userRoleEvent.getResourceId();
        User user = userCacheService.getUser(userRoleEvent.getCreatorId());
        for (UserRoleDTO userRoleDTO : userRoleEvent.getUserRoles()) {
            try {
                userRoleService.createOrUpdateUserRole(user, resourceId, userRoleDTO);
            } catch (Throwable t) {
                throw new BoardException(
                    UNPROCESSABLE_RESOURCE_USER, "Unable to add member: " + userRoleDTO.getUser().toString(), t);
            } finally {
                departmentUserService.decrementMemberCountPending(resourceId);
            }
        }
    }

}
