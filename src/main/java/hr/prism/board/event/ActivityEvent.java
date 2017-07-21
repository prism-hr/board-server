package hr.prism.board.event;

import hr.prism.board.enums.Activity;
import org.springframework.context.ApplicationEvent;

public class ActivityEvent extends ApplicationEvent {

    private Long resourceId;

    private Long userRoleId;

    private Activity type;

    public ActivityEvent(Object source, Long resourceId, Activity type) {
        super(source);
        this.resourceId = resourceId;
        this.type = type;
    }

    public ActivityEvent(Object source, Long resourceId, Long userRoleId, Activity type) {
        super(source);
        this.resourceId = resourceId;
        this.userRoleId = userRoleId;
        this.type = type;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Long getUserRoleId() {
        return userRoleId;
    }

    public Activity getType() {
        return type;
    }

}
