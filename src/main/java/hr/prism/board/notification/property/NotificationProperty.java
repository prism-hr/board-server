package hr.prism.board.notification.property;

import hr.prism.board.service.NotificationService;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.text.WordUtils.uncapitalize;

public interface NotificationProperty extends Comparable<NotificationProperty> {

    default String getKey() {
        return uncapitalize(getClass().getSimpleName().replace("Property", EMPTY));
    }

    String getValue(NotificationService.NotificationRequest notificationRequest);

    @Override
    default int compareTo(NotificationProperty other) {
        return getKey().compareTo(other.getKey());
    }

}
