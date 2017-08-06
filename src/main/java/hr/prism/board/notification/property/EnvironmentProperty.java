package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentProperty implements NotificationProperty {

    @Value("${environment}")
    private String environment;

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return environment;
    }

}
