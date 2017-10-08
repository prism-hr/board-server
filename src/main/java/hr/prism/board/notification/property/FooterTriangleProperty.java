package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FooterTriangleProperty implements NotificationProperty {
    
    @Value("${footer.logo.url}")
    private String footerLogoUrl;
    
    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return footerLogoUrl;
    }
    
}
