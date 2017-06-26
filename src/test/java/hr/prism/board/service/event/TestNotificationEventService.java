package hr.prism.board.service.event;

import hr.prism.board.enums.State;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestNotificationEventService extends NotificationEventService {

    @Override
    public void publishEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications, State state) {
        super.sendNotifications(new NotificationEvent(source, creatorId, resourceId, notifications, state));
    }

}
