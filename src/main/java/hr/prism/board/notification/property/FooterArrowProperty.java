package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FooterArrowProperty implements NotificationProperty {
    
    @Value("${footer.triangle.url}")
    private String footerTriangleUrl;
    
    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return footerTriangleUrl;
    }
    
}
