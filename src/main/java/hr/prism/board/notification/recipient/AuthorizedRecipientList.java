package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.service.UserService;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class AuthorizedRecipientList implements NotificationRecipientList {

    @Inject
    private UserService userService;

    public List<User> list(Resource resource, Notification notification) {
        return userService.findByResourceAndEnclosingScopeAndRole(resource, notification.getScope(), notification.getRole());
    }

}
