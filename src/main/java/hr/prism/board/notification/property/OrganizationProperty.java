package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class OrganizationProperty implements NotificationProperty {

    public String getValue(NotificationRequest notificationRequest) {
        return ((Post) notificationRequest.getResource()).getOrganization().getName();
    }

}
