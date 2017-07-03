package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class CommentProperty implements NotificationProperty {

    @Inject
    private ResourceService resourceService;

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        return resourceService.getLatestResourceOperation(notificationRequest.getResource(), notificationRequest.getAction()).getComment();
    }

}
