package hr.prism.board.notification.property;

import hr.prism.board.enums.Scope;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BoardProperty implements NotificationProperty {

    @Inject
    private ResourceService resourceService;

    public String getValue(NotificationService.NotificationInstance notificationInstance) {
        return resourceService.findByResourceAndEnclosingScope(notificationInstance.getResource(), Scope.BOARD).getName();
    }

}
