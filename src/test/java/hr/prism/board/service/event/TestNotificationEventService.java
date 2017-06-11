package hr.prism.board.service.event;

import hr.prism.board.event.NotificationEvent;
import org.springframework.stereotype.Service;

@Service
public class TestNotificationEventService extends NotificationEventService {

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, String notification) {
        super.sendNotifications(new NotificationEvent(source, creatorId, resourceId, notification));
    }

}
