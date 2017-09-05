package hr.prism.board.service.event;

import hr.prism.board.domain.User;
import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.dto.UserDTO;
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
import java.util.Set;

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

    public void publishEvent(Object source, Long creatorId, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        applicationEventPublisher.publishEvent(new UserRoleEvent(source, creatorId, resourceId, resourceUsersDTO));
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
            ResourceUsersDTO resourceUsersDTO = userRoleEvent.getResourceUsersDTO();
            Set<UserRoleDTO> userRoleDTOs = resourceUsersDTO.getRoles();
            for (UserDTO userDTO : resourceUsersDTO.getUsers()) {
                userRoleService.createResourceUser(currentUser, resourceId, userDTO, userRoleDTOs, invokedAsynchronously);
            }
        } catch (Throwable t) {
            throw new BoardException(ExceptionCode.UNPROCESSABLE_RESOURCE_USER, t);
        } finally {
            departmentService.unsetMemberCountProvisional(resourceId);
        }
    }

}
