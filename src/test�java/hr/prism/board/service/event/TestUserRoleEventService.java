package hr.prism.board.service.event;

import hr.prism.board.event.UserRoleEvent;
import org.springframework.stereotype.Service;

@Service
public class TestUserRoleEventService extends UserRoleEventService {

    @Override
    public void createResourceUsersAsync(UserRoleEvent userRoleEvent) {
        super.createResourceUsers(userRoleEvent);
    }

}
