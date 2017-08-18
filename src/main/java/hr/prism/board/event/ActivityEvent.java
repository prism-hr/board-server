package hr.prism.board.event;

import hr.prism.board.workflow.Activity;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class ActivityEvent extends ApplicationEvent {

    private Long resourceId;

    private Long userRoleId;

    private Long resourceEventId;

    private List<Activity> activities;

    public ActivityEvent(Object source, Long resourceId, List<Activity> activities) {
        super(source);
        this.resourceId = resourceId;
        this.activities = activities;
    }

    public ActivityEvent(Object source, Long resourceId, Long userRoleId, Long resourceEventId, List<Activity> activities) {
        super(source);
        this.resourceId = resourceId;
        this.userRoleId = userRoleId;
        this.resourceEventId = resourceEventId;
        this.activities = activities;
    }

    public ActivityEvent(Object source, Long resourceId, Long userRoleId) {
        super(source);
        this.resourceId = resourceId;
        this.userRoleId = userRoleId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Long getUserRoleId() {
        return userRoleId;
    }

    public Long getResourceEventId() {
        return resourceEventId;
    }

    public List<Activity> getActivities() {
        return activities;
    }

}
