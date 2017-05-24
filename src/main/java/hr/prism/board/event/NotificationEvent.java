package hr.prism.board.event;

import hr.prism.board.domain.Role;
import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {
    
    private Long creatorId;
    
    private String creator;
    
    private Long resourceId;
    
    private Role role;
    
    private String notification;
    
    public NotificationEvent(Object source, Long creatorId, String creator, Long resourceId, Role role, String notification) {
        super(source);
        this.creatorId = creatorId;
        this.creator = creator;
        this.resourceId = resourceId;
        this.role = role;
        this.notification = notification;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public String getCreator() {
        return creator;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getNotification() {
        return notification;
    }
    
}
