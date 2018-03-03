package hr.prism.board.enums;

public enum Activity {

    ACCEPT_BOARD_ACTIVITY(false),
    ACCEPT_POST_ACTIVITY(false),
    CORRECT_POST_ACTIVITY(false),
    JOIN_BOARD_ACTIVITY(false),
    JOIN_DEPARTMENT_ACTIVITY(false),
    JOIN_DEPARTMENT_REQUEST_ACTIVITY(false),
    NEW_BOARD_PARENT_ACTIVITY(false),
    NEW_POST_PARENT_ACTIVITY(false),
    PUBLISH_POST_ACTIVITY(false),
    PUBLISH_POST_MEMBER_ACTIVITY(true),
    REJECT_POST_ACTIVITY(false),
    RESTORE_POST_ACTIVITY(false),
    RETIRE_POST_ACTIVITY(false),
    SUSPEND_POST_ACTIVITY(false),
    RESPOND_POST_ACTIVITY(false),
    CREATE_TASK_ACTIVITY(false),
    UPDATE_TASK_ACTIVITY(false),
    SUBSCRIBE_DEPARTMENT_ACTIVITY(false),
    SUSPEND_DEPARTMENT_ACTIVITY(false);

    private boolean filterByCategory;

    Activity(boolean filterByCategory) {
        this.filterByCategory = filterByCategory;
    }

    public boolean isFilterByCategory() {
        return filterByCategory;
    }

}
