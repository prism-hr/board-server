package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.apache.commons.lang3.StringUtils;

public interface NotificationProperty {

    default String getKey() {
        return getClass().getSimpleName().replace("Property", StringUtils.EMPTY).toLowerCase();
    }

    String getValue(NotificationService.NotificationInstance notificationInstance);

}
