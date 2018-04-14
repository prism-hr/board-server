package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.service.UserCacheService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static java.util.Collections.singletonList;

@Component
public class DefinedRecipientList implements NotificationRecipientList {

    private final UserCacheService userCacheService;

    @Inject
    public DefinedRecipientList(UserCacheService userCacheService) {
        this.userCacheService = userCacheService;
    }

    public List<UserNotification> list(Resource resource, Notification notification) {
        String uuid = notification.getInvitation();
        if (uuid == null) {
            return singletonList(new UserNotification(userCacheService.findOneFresh(notification.getUserId())));
        }

        return singletonList(new UserNotification(userCacheService.findByUserRoleUuid(uuid), uuid));
    }

}
