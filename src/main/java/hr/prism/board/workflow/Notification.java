package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class Notification extends Update<Notification> {

    private hr.prism.board.enums.Notification notification;

    @JsonIgnore
    private String invitation;

    private List<Attachment> attachments = new ArrayList<>();

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

    public String getInvitation() {
        return invitation;
    }

    public Notification setInvitation(String invitation) {
        this.invitation = invitation;
        return this;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Notification addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(notification)
            .append(invitation)
            .append(attachments)
            .toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Notification that = (Notification) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(notification, that.notification)
            .append(invitation, that.invitation)
            .append(attachments, that.attachments)
            .isEquals();
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

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(url)
                .toHashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            Attachment that = (Attachment) object;
            return new EqualsBuilder()
                .append(name, that.name)
                .append(url, that.url)
                .append(label, that.label)
                .isEquals();
        }

    }

}
