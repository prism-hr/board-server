package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class ResetUuidProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationRequest notificationRequest) {
        return notificationRequest.getRecipient().getPasswordResetUuid();
    }

}
