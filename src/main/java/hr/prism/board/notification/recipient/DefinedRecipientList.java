package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.service.UserService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static java.util.Collections.singletonList;

@Component
public class DefinedRecipientList implements NotificationRecipientList {

    private final UserService userService;

    @Inject
    public DefinedRecipientList(UserService userService) {
        this.userService = userService;
    }

    public List<UserNotification> list(Resource resource, Notification notification) {
        String uuid = notification.getInvitation();
        if (uuid == null) {
            return singletonList(new UserNotification(userService.getById(notification.getUserId())));
        }

        return singletonList(new UserNotification(userService.getByUserRoleUuid(uuid), uuid));
    }

}
