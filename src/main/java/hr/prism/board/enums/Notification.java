package hr.prism.board.enums;

import hr.prism.board.notification.recipient.AuthorizedCategorizedRecipientList;
import hr.prism.board.notification.recipient.AuthorizedRecipientList;
import hr.prism.board.notification.recipient.DefinedRecipientList;
import hr.prism.board.notification.recipient.NotificationRecipientList;

public enum Notification {

    ACCEPT_BOARD_NOTIFICATION(AuthorizedRecipientList.class),
    ACCEPT_POST_NOTIFICATION(AuthorizedRecipientList.class),
    CORRECT_POST_NOTIFICATION(AuthorizedRecipientList.class),
    JOIN_BOARD_NOTIFICATION(DefinedRecipientList.class),
    JOIN_DEPARTMENT_NOTIFICATION(DefinedRecipientList.class),
    JOIN_DEPARTMENT_REQUEST_NOTIFICATION(AuthorizedRecipientList.class),
    NEW_BOARD_NOTIFICATION(AuthorizedRecipientList.class),
    NEW_BOARD_PARENT_NOTIFICATION(AuthorizedRecipientList.class),
    NEW_POST_NOTIFICATION(AuthorizedRecipientList.class),
    NEW_POST_PARENT_NOTIFICATION(AuthorizedRecipientList.class),
    PUBLISH_POST_NOTIFICATION(AuthorizedRecipientList.class),
    PUBLISH_POST_MEMBER_NOTIFICATION(AuthorizedCategorizedRecipientList.class),
    REJECT_BOARD_NOTIFICATION(AuthorizedRecipientList.class),
    REJECT_POST_NOTIFICATION(AuthorizedRecipientList.class),
    RESET_PASSWORD_NOTIFICATION(DefinedRecipientList.class),
    RESTORE_BOARD_NOTIFICATION(AuthorizedRecipientList.class),
    RESTORE_POST_NOTIFICATION(AuthorizedRecipientList.class),
    RETIRE_POST_NOTIFICATION(AuthorizedRecipientList.class),
    SUSPEND_POST_NOTIFICATION(AuthorizedRecipientList.class),
    RESPOND_POST_NOTIFICATION(AuthorizedRecipientList.class);

    private Class<? extends NotificationRecipientList> recipients;

    Notification(Class<? extends NotificationRecipientList> recipients) {
        this.recipients = recipients;
    }

    public Class<? extends NotificationRecipientList> getRecipients() {
        return recipients;
    }

    @Override
    public String toString() {
        return name().toLowerCase().replace("_notification", "");
    }

}
