package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ParentRedirectProperty implements NotificationProperty {

    @Value("${server.url}")
    private String serverUrl;

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return serverUrl + "/redirect?resource=" + notificationRequest.getResource().getParent().getId();
    }

}
