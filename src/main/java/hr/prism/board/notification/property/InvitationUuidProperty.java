package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class InvitationUuidProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        String invitation = notificationRequest.getInvitation();
        if (invitation == null) {
            return StringUtils.EMPTY;
        }

        return "&uuid=" + invitation;
    }

}
