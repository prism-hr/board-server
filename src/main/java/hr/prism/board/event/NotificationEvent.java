package hr.prism.board.event;

import hr.prism.board.enums.State;
import hr.prism.board.workflow.Notification;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class NotificationEvent extends ApplicationEvent {

    private Long creatorId;

    private Long resourceId;

    private List<Notification> notifications;

    private State state;

    public NotificationEvent(Object source, List<Notification> notifications) {
        super(source);
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long resourceId, List<Notification> notifications, State state) {
        super(source);
        this.resourceId = resourceId;
        this.notifications = notifications;
        this.state = state;
    }

    public NotificationEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications) {
        super(source);
        this.creatorId = creatorId;
        this.resourceId = resourceId;
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications, State state) {
        super(source);
        this.creatorId = creatorId;
        this.resourceId = resourceId;
        this.notifications = notifications;
        this.state = state;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public State getState() {
        return state;
    }

}
