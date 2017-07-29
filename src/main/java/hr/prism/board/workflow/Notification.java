package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Notification extends Update<Notification> {

    private hr.prism.board.enums.Notification notification;

    @JsonIgnore
    private Long userId;

    public Notification() {
        setType(Update.NOTIFICATION);
    }

    public hr.prism.board.enums.Notification getNotification() {
        return notification;
    }

    public Notification setNotification(hr.prism.board.enums.Notification notification) {
        this.notification = notification;
        return this;
    }

    public Workflow with(hr.prism.board.enums.Notification notification) {
        this.notification = notification;
        return getWorkflow();
    }

    public Long getUserId() {
        return userId;
    }

    public Notification setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

}
