package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class CoveringNoteProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationRequest notificationRequest) {
        return ((Post) notificationRequest.getResource()).getResponse().getCoveringNote();
    }

}
