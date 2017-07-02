package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class RecipientProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationService.NotificationInstance notificationInstance) {
        return notificationInstance.getRecipient().getGivenName();
    }

}
