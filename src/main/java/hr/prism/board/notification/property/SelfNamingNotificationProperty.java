package hr.prism.board.notification.property;

import org.apache.commons.lang3.StringUtils;

public abstract class SelfNamingNotificationProperty implements NotificationProperty {

    @Override
    public String getKey() {
        return getClass().getSimpleName().replace("Property", StringUtils.EMPTY).toLowerCase();
    }

}
