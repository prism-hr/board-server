package hr.prism.board.service.event;

import hr.prism.board.enums.Action;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestNotificationEventService extends NotificationEventService {

    @Override
    public void publishEvent(Object source, List<Notification> notifications) {
        super.sendNotifications(new NotificationEvent(source, notifications));
    }

    @Override
    public void publishEvent(Object source, Long resourceId, List<Notification> notifications) {
        super.sendNotifications(new NotificationEvent(source, resourceId, notifications));
    }

    @Override
    public void publishEvent(Object source, Long resourceId, Action action, List<Notification> notifications) {
        super.sendNotifications(new NotificationEvent(source, resourceId, action, notifications));
    }

    @Override
    public void publishEvent(Object source, Long resourceId, Long resourceEventId, List<Notification> notifications) {
        super.sendNotifications(new NotificationEvent(source, resourceId, resourceEventId, notifications));
    }

}
