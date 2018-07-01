package hr.prism.board.event;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.workflow.Notification;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class NotificationEvent extends ApplicationEvent {

    private Long resourceId;

    private Long resourceEventId;

    private Action action;

    private List<ResourceTask> tasks;

    private List<Notification> notifications;

    public NotificationEvent(Object source, List<Notification> notifications) {
        super(source);
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long resourceId, List<Notification> notifications) {
        super(source);
        this.resourceId = resourceId;
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long resourceId, Action action, List<Notification> notifications) {
        super(source);
        this.resourceId = resourceId;
        this.action = action;
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long resourceId, Long resourceEventId, List<Notification> notifications) {
        super(source);
        this.resourceId = resourceId;
        this.resourceEventId = resourceEventId;
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long resourceId, List<ResourceTask> tasks,
                             List<Notification> notifications) {
        super(source);
        this.resourceId = resourceId;
        this.tasks = tasks;
        this.notifications = notifications;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Long getResourceEventId() {
        return resourceEventId;
    }

    public Action getAction() {
        return action;
    }

    public List<ResourceTask> getTasks() {
        return tasks;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(resourceId)
            .append(resourceEventId)
            .append(action)
            .append(tasks)
            .append(notifications)
            .toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NotificationEvent that = (NotificationEvent) object;
        return new EqualsBuilder()
            .append(resourceId, that.resourceId)
            .append(resourceEventId, that.resourceEventId)
            .append(action, that.action)
            .append(tasks, that.tasks)
            .append(notifications, that.notifications)
            .isEquals();
    }

}
