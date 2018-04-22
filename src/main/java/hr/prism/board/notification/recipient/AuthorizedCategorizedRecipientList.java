package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.service.UserService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class AuthorizedCategorizedRecipientList implements NotificationRecipientList {

    private final UserService userService;

    @Inject
    public AuthorizedCategorizedRecipientList(UserService userService) {
        this.userService = userService;
    }

    public List<UserNotification> list(Resource resource, Notification notification) {
        return userService.getByResourceAndEnclosingRoleCategorized(
            resource, notification.getScope(), notification.getRole());
    }

}
