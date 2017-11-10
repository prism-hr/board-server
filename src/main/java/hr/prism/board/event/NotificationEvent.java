package hr.prism.board.event;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.workflow.Notification;
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

    public NotificationEvent(Object source, Long resourceId, List<ResourceTask> tasks, List<Notification> notifications) {
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

}
