package hr.prism.board.enums;

public enum Action {

    VIEW,
    EDIT,
    PURSUE,
    CORRECT, // Comment optional
    EXTEND,
    ACCEPT, // Comment optional
    SUSPEND, // Comment required
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
