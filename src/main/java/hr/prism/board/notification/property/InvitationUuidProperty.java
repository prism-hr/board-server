package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class InvitationUuidProperty implements NotificationProperty {

    @Override
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        if (notificationRequest.getRecipient().isRegistered()) {
            return StringUtils.EMPTY;
        }

        return "&uuid=" + notificationRequest.getInvitation();
    }

}
