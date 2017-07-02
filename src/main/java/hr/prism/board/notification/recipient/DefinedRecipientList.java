package hr.prism.board.notification.recipient;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component
public class DefinedRecipientList implements NotificationRecipientList {

    @Inject
    private UserCacheService userCacheService;

    public List<User> list(Resource resource, Notification notification) {
        return Collections.singletonList(userCacheService.findOne(notification.getUserId()));
    }

}
