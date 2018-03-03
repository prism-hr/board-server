package hr.prism.board.enums;

import hr.prism.board.notification.recipient.*;

public enum Notification {

    ACCEPT_POST_NOTIFICATION(AuthorizedRecipientList.class),
    CORRECT_POST_NOTIFICATION(AuthorizedRecipientList.class),
    JOIN_DEPARTMENT_NOTIFICATION(DefinedRecipientList.class),
    JOIN_DEPARTMENT_REQUEST_NOTIFICATION(AuthorizedRecipientList.class),
    NEW_POST_NOTIFICATION(AuthorizedRecipientList.class),
    NEW_POST_PARENT_NOTIFICATION(AuthorizedRecipientList.class),
    PUBLISH_POST_NOTIFICATION(AuthorizedRecipientList.class),
    PUBLISH_POST_MEMBER_NOTIFICATION(AuthorizedCategorizedRecipientList.class),
    REJECT_POST_NOTIFICATION(AuthorizedRecipientList.class),
    RESET_PASSWORD_NOTIFICATION(DefinedRecipientList.class),
    RESTORE_POST_NOTIFICATION(AuthorizedRecipientList.class),
    RETIRE_POST_NOTIFICATION(AuthorizedRecipientList.class),
    SUSPEND_POST_NOTIFICATION(AuthorizedRecipientList.class),
    RESPOND_POST_NOTIFICATION(PostRecipientList.class),
    CREATE_TASK_NOTIFICATION(AuthorizedRecipientList.class),
    UPDATE_TASK_NOTIFICATION(AuthorizedRecipientList.class),
    SUBSCRIBE_DEPARTMENT_NOTIFICATION(AuthorizedRecipientList.class),
    SUSPEND_DEPARTMENT_NOTIFICATION(AuthorizedRecipientList.class);

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
