package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ResourceRedirectProperty implements NotificationProperty {

    @Inject
    private Environment environment;

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return environment.getProperty("server.url") + "/redirect?resource=" + notificationRequest.getResource().getId();
    }

}
