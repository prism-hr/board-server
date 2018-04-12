package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService.NotificationRequest;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class CommentProperty implements NotificationProperty {

    private final ResourceService resourceService;

    @Inject
    public CommentProperty(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public String getValue(NotificationRequest notificationRequest) {
        return resourceService.getLatestResourceOperation(
            notificationRequest.getResource(), notificationRequest.getAction()).getComment();
    }

}
