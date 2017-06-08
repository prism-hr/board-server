package hr.prism.board.event;

import hr.prism.board.domain.Role;
import hr.prism.board.workflow.Workflow;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class NotificationEvent extends ApplicationEvent {

    private Long creatorId;

    private String creator;

    private Long resourceId;

    private Role role;

    private List<Workflow.Notification> notifications;

    public NotificationEvent(Object source, Long creatorId, String creator, Long resourceId, Role role, List<Workflow.Notification> notifications) {
        super(source);
        this.creatorId = creatorId;
        this.creator = creator;
        this.resourceId = resourceId;
        this.role = role;
        this.notifications = notifications;
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

    public List<Workflow.Notification> getNotifications() {
        return notifications;
    }

}
