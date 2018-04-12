package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Notification extends Update<Notification> {

    private hr.prism.board.enums.Notification notification;

    private List<Attachment> attachments = new ArrayList<>();

    @JsonIgnore
    private String invitation;

    public Notification() {
        setType(NOTIFICATION);
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

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Notification addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public String getInvitation() {
        return invitation;
    }

    public Notification setInvitation(String invitation) {
        this.invitation = invitation;
        return this;
    }

    public static class Attachment {

        private String name;

        private String url;

        private String label;

        public String getName() {
            return name;
        }

        public Attachment setName(String name) {
            this.name = name;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Attachment setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getLabel() {
            return label;
        }

        public Attachment setLabel(String label) {
            this.label = label;
            return this;
        }

    }

}
