package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class ProfileProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        String profile = ((Post) notificationRequest.getResource()).getResponse().getWebsiteResume();
        return profile == null ? "Not specified" : profile;
    }

}
