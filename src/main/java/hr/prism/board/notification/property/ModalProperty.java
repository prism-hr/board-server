package hr.prism.board.notification.property;

import hr.prism.board.domain.User;
import hr.prism.board.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class ModalProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        User recipient = notificationRequest.getRecipient();
        return recipient.isRegistered() ? "Login" : "Register";
    }

}
