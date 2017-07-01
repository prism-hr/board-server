package hr.prism.board.notification.property;

import hr.prism.board.domain.Resource;
import hr.prism.board.enums.Action;

public interface NotificationProperty {

    String getKey();

    String getValue(Resource resource, Action action);

}
