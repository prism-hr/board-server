package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService.NotificationRequest;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static hr.prism.board.enums.Scope.BOARD;

@Component
public class BoardProperty implements NotificationProperty {

    private final ResourceService resourceService;

    @Inject
    public BoardProperty(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public String getValue(NotificationRequest notificationRequest) {
        return resourceService.getByResourceAndEnclosingScope(notificationRequest.getResource(), BOARD).getName();
    }

}
