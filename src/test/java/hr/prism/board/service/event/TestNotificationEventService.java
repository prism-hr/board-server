package hr.prism.board.service.event;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
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
    public void publishEvent(Object source, Long resourceId, List<Notification> notifications, State state) {
        super.publishEvent(source, resourceId, notifications, state);
    }

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications) {
        super.sendNotifications(new NotificationEvent(source, creatorId, resourceId, notifications));
    }

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, Action action, List<Notification> notifications, State state) {
        super.sendNotifications(new NotificationEvent(source, creatorId, resourceId, action, notifications, state));
    }

}
