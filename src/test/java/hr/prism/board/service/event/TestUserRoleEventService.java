package hr.prism.board.service.event;

import hr.prism.board.event.UserRoleEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestUserRoleEventService extends UserRoleEventService {

    @Override
    @Transactional
    public void createResourceUsers(UserRoleEvent userRoleEvent) {
        super.createResourceUsers(userRoleEvent);
    }

}
