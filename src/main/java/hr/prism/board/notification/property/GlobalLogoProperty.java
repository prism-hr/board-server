package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalLogoProperty implements NotificationProperty {

    @Value("${prism.logo.url}")
    private String prismLogoUrl;

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return prismLogoUrl;
    }

}
