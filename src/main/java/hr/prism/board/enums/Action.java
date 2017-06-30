package hr.prism.board.enums;

import hr.prism.board.dto.ResourcePatchDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;

public enum Action {

    VIEW(false),
    AUDIT(false),
    EDIT(true),
    EXTEND(true),
    ACCEPT(true), // Comment optional
    SUSPEND(true, true), // Comment required
    CORRECT(true), // Comment optional
    REJECT(true, true), // Comment required
    PUBLISH(true),
    RETIRE(true),
    RESTORE(true), // Comment optional for restore from reject, no comment for restore from withdrawn
    WITHDRAW(true);

    private boolean resourceOperation;

    private boolean requireComment;

    Action(boolean resourceOperation) {
        this.resourceOperation = resourceOperation;
    }

    Action(boolean resourceOperation, boolean requireComment) {
        this.resourceOperation = resourceOperation;
        this.requireComment = requireComment;
    }

    public boolean isResourceOperation() {
        return resourceOperation;
    }

    public boolean isRequireComment() {
        return requireComment;
    }

    public static Action exchangeAndValidate(String actionName, ResourcePatchDTO resourcePatchDTO) {
        Action action = valueOf(actionName);
        if (action.isRequireComment() && resourcePatchDTO.getComment() == null) {
            throw new BoardException(ExceptionCode.MISSING_COMMENT);
        }

        return action;
    }

}
