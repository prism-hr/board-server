package hr.prism.board.service.event;

import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.event.UserRoleEvent;
import hr.prism.board.service.UserRoleService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.util.Set;

@Service
public class UserRoleEventService {

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        applicationEventPublisher.publishEvent(new UserRoleEvent(source, resourceId, resourceUsersDTO));
    }

    @Async
    @TransactionalEventListener
    public void createResourceUsersAsync(UserRoleEvent userRoleEvent) {
        createResourceUsers(userRoleEvent);
    }

    protected void createResourceUsers(UserRoleEvent userRoleEvent) {
        Long resourceId = userRoleEvent.getResourceId();
        ResourceUsersDTO resourceUsersDTO = userRoleEvent.getResourceUsersDTO();
        Set<UserRoleDTO> userRoleDTOs = resourceUsersDTO.getRoles();
        for (UserDTO userDTO : resourceUsersDTO.getUsers()) {
            userRoleService.createResourceUser(resourceId, userDTO, userRoleDTOs);
        }
    }

}
