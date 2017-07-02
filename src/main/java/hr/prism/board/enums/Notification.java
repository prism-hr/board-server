package hr.prism.board.enums;

import hr.prism.board.notification.recipient.AuthorizedCategorizedRecipientList;
import hr.prism.board.notification.recipient.AuthorizedRecipientList;
import hr.prism.board.notification.recipient.DefinedRecipientList;
import hr.prism.board.notification.recipient.NotificationRecipientList;

public enum Notification {

    ACCEPT_BOARD(AuthorizedRecipientList.class),
    ACCEPT_POST(AuthorizedRecipientList.class),
    CORRECT_POST(AuthorizedRecipientList.class),
    JOIN_BOARD(DefinedRecipientList.class),
    JOIN_DEPARTMENT(DefinedRecipientList.class),
    NEW_BOARD(AuthorizedRecipientList.class),
    NEW_BOARD_PARENT(AuthorizedRecipientList.class),
    NEW_POST(DefinedRecipientList.class),
    NEW_POST_PARENT(AuthorizedRecipientList.class),
    PUBLISH_POST(AuthorizedRecipientList.class),
    PUBLISH_POST_MEMBER(AuthorizedCategorizedRecipientList.class),
    REJECT_BOARD(AuthorizedRecipientList.class),
    REJECT_POST(AuthorizedRecipientList.class),
    RESET_PASSWORD(DefinedRecipientList.class),
    RETIRE_POST(AuthorizedRecipientList.class),
    SUSPEND_POST(AuthorizedRecipientList.class);

    private Class<? extends NotificationRecipientList> recipients;

    Notification(Class<? extends NotificationRecipientList> recipients) {
        this.recipients = recipients;
    }

    public Class<? extends NotificationRecipientList> getRecipients() {
        return recipients;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
