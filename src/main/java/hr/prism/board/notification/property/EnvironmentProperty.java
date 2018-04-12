package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class EnvironmentProperty implements NotificationProperty {

    private final String environment;

    @Inject
    public EnvironmentProperty(@Value("${environment}") String environment) {
        this.environment = environment;
    }

    @Override
    public String getValue(NotificationRequest notificationRequest) {
        return environment;
    }

}
