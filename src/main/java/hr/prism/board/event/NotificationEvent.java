package hr.prism.board.event;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.workflow.Notification;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class NotificationEvent extends ApplicationEvent {

    private Long resourceId;

    private Action action;

    private State state;

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

    public NotificationEvent(Object source, Long resourceId, State state, List<Notification> notifications) {
        super(source);
        this.resourceId = resourceId;
        this.state = state;
        this.notifications = notifications;
    }

    public NotificationEvent(Object source, Long resourceId, Action action, State state, List<Notification> notifications) {
        super(source);
        this.resourceId = resourceId;
        this.action = action;
        this.state = state;
        this.notifications = notifications;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Action getAction() {
        return action;
    }

    public State getState() {
        return state;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

}
