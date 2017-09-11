package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component
public class DefinedRecipientList implements NotificationRecipientList {

    @Inject
    private UserCacheService userCacheService;

    public List<UserNotification> list(Resource resource, Notification notification) {
        String uuid = notification.getInvitation();
        if (uuid == null) {
            return Collections.singletonList(new UserNotification(userCacheService.findOneFresh(notification.getUserId())));
        }

        return Collections.singletonList(new UserNotification(userCacheService.findByUuid(uuid), uuid));
    }

}
