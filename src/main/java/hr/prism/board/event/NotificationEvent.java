package hr.prism.board.event;

import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {

    private Long creatorId;

    private Long resourceId;

    private String notification;

    public NotificationEvent(Object source, Long resourceId, String notification) {
        super(source);
        this.resourceId = resourceId;
        this.notification = notification;
    }

    public NotificationEvent(Object source, Long creatorId, Long resourceId, String notification) {
        super(source);
        this.creatorId = creatorId;
        this.resourceId = resourceId;
        this.notification = notification;
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

}
