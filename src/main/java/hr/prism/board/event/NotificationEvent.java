package hr.prism.board.event;

import hr.prism.board.enums.State;
import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {

    private Long creatorId;

    private Long resourceId;

    private String notification;

    private State state;

    public NotificationEvent(Object source, Long resourceId, String notification, State state) {
        super(source);
        this.resourceId = resourceId;
        this.notification = notification;
        this.state = state;
    }

    public NotificationEvent(Object source, Long creatorId, Long resourceId, String notification, State state) {
        super(source);
        this.creatorId = creatorId;
        this.resourceId = resourceId;
        this.notification = notification;
        this.state = state;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getNotification() {
        return notification;
    }

    public State getState() {
        return state;
    }

}
