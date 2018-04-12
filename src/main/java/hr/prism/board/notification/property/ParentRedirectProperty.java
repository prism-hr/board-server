package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ParentRedirectProperty implements NotificationProperty {

    private final String serverUrl;

    @Inject
    public ParentRedirectProperty(@Value("${server.url}") String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public String getValue(NotificationRequest notificationRequest) {
        return serverUrl + "/redirect?resource=" + notificationRequest.getResource().getParent().getId();
    }

}
