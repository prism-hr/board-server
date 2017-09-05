package hr.prism.board.service.event;

import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.event.UserRoleEvent;
import org.springframework.stereotype.Service;

@Service
public class TestUserRoleEventService extends UserRoleEventService {

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        super.createResourceUsers(new UserRoleEvent(source, creatorId, resourceId, resourceUsersDTO),false);
    }

}
