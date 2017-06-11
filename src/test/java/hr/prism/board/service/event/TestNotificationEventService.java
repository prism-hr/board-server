package hr.prism.board.service.event;

import hr.prism.board.event.NotificationEvent;
import org.springframework.stereotype.Service;

@Service
public class TestNotificationEventService extends NotificationEventService {

    @Override
    public void sendNotificationsAsync(NotificationEvent notificationEvent) {
        super.sendNotifications(notificationEvent);
    }

}
