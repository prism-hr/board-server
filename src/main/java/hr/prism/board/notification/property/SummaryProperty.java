package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class SummaryProperty implements NotificationProperty {

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return notificationRequest.getResource().getSummary();
    }

}
