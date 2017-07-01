package hr.prism.board.enums;

import hr.prism.board.notification.property.CommentProperty;
import hr.prism.board.notification.property.NotificationProperty;
import hr.prism.board.notification.property.PublicationScheduleProperty;
import hr.prism.board.notification.recipient.AuthorizedCategorizedRecipientList;
import hr.prism.board.notification.recipient.AuthorizedRecipientList;
import hr.prism.board.notification.recipient.NotificationRecipientList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public enum NotificationDefinition {

    ACCEPT_BOARD(Collections.singletonList(PublicationScheduleProperty.class)),
    ACCEPT_POST,
    CORRECT_POST,
    JOIN_BOARD,
    JOIN_DEPARTMENT,
    NEW_BOARD,
    NEW_BOARD_PARENT,
    NEW_POST,
    NEW_POST_PARENT,
    PUBLISH_POST(AuthorizedCategorizedRecipientList.class),
    PUBLISH_POST_MEMBER,
    REJECT_BOARD,
    REJECT_POST(Collections.singletonList(CommentProperty.class)),
    RESET_PASSWORD,
    RETIRE_POST,
    SUSPEND_POST(Collections.singletonList(CommentProperty.class));

    private Class<? extends NotificationRecipientList> recipients;

    private List<Class<? extends NotificationProperty>> properties = new ArrayList<>();

    NotificationDefinition() {
        this(AuthorizedRecipientList.class, null);
    }

    NotificationDefinition(Class<? extends NotificationRecipientList> recipients) {
        this(recipients, null);
    }

    NotificationDefinition(Collection<Class<? extends NotificationProperty>> properties) {
        this(AuthorizedRecipientList.class, properties);
    }

    NotificationDefinition(Class<? extends NotificationRecipientList> recipients, Collection<Class<? extends NotificationProperty>> properties) {
        // TODO: register the default properties
        this.recipients = recipients;
        if (this.properties != null) {
            this.properties.addAll(properties);
        }
    }

    public Class<? extends NotificationRecipientList> getRecipients() {
        return recipients;
    }

    public List<Class<? extends NotificationProperty>> getProperties() {
        return properties;
    }

}
