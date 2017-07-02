package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.workflow.Notification;

import java.util.List;

public interface NotificationRecipientList {

    List<User> list(Resource resource, Notification notification);

}
