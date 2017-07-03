package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;
import org.apache.commons.lang3.StringUtils;

public interface NotificationProperty extends Comparable<NotificationProperty> {

    default String getKey() {
        return getClass().getSimpleName().replace("Property", StringUtils.EMPTY).toLowerCase();
    }

    String getValue(NotificationService.NotificationRequest notificationRequest);

    @Override
    default int compareTo(NotificationProperty other) {
        return getKey().compareTo(other.getKey());
    }

}
