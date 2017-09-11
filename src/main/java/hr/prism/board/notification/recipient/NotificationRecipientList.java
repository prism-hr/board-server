package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;

import java.util.List;

public interface NotificationRecipientList {

    List<UserNotification> list(Resource resource, Notification notification);

}
