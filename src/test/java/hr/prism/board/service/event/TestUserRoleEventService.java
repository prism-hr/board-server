package hr.prism.board.service.event;

import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.event.UserRoleEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestUserRoleEventService extends UserRoleEventService {

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, List<UserRoleDTO> userRoleDTOs) {
        super.createResourceUsers(new UserRoleEvent(source, creatorId, resourceId, userRoleDTOs), false);
    }

}
