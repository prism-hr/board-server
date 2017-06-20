package hr.prism.board.service.event;

import hr.prism.board.enums.State;
import hr.prism.board.event.NotificationEvent;
import org.springframework.stereotype.Service;

@Service
public class TestNotificationEventService extends NotificationEventService {

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, String notification, State state) {
        super.sendNotifications(new NotificationEvent(source, creatorId, resourceId, notification, state));
    }

}
