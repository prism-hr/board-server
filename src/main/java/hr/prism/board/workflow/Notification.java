package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public class Notification extends Update<Notification> {

    private hr.prism.board.enums.Notification notification;

    @JsonIgnore
    private Long userId;

    @JsonIgnore
    private Map<String, String> customProperties;

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

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public Notification setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
        return this;
    }

}
