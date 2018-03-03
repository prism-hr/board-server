package hr.prism.board.enums;

public enum Action {

    VIEW,
    PURSUE,
    EDIT,
    SUSPEND, // Comment required
    CORRECT, // Comment optional
    EXTEND,
    ACCEPT, // Comment optional
    REJECT, // Comment required
    PUBLISH,
    RETIRE,
    RESTORE, // Comment optional for restore from reject, no comment for restore from withdrawn
    CONVERT,
    SUBSCRIBE,
    UNSUBSCRIBE,
    WITHDRAW,
    ARCHIVE

}
