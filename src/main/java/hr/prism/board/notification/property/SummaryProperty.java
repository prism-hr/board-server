package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class SummaryProperty implements NotificationProperty {

    public String getValue(NotificationRequest notificationRequest) {
        return notificationRequest.getResource().getSummary();
    }

}
