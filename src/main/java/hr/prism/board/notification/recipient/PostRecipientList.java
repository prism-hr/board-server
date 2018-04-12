package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

@Component
public class PostRecipientList implements NotificationRecipientList {

    public List<UserNotification> list(Resource resource, Notification notification) {
        if (resource instanceof Post) {
            String applyEmail = ((Post) resource).getApplyEmail();
            if (applyEmail != null) {
                User user = new User();
                user.setGivenName("Author");
                user.setSurname("Author");
                user.setEmail(applyEmail);
                return singletonList(new UserNotification(user));
            }
        }

        return Collections.emptyList();
    }

}
