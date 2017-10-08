package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeaderLogoProperty implements NotificationProperty {
    
    @Value("${header.logo.url}")
    private String headerLogoUrl;
    
    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return headerLogoUrl;
    }

}
