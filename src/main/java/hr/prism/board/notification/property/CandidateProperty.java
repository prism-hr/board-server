package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class CandidateProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return ((Post) notificationRequest.getResource()).getResponse().getUser().getFullName();
    }

}
