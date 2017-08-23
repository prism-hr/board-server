package hr.prism.board.enums;

import hr.prism.board.dto.ResourcePatchDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;

public enum Action {

    VIEW(false),
    PURSUE(false),
    EDIT(false),
    EXTEND(false),
    ACCEPT(false), // Comment optional
    SUSPEND(true), // Comment required
    CORRECT(false), // Comment optional
    REJECT(true), // Comment required
    PUBLISH(false),
    RETIRE(false),
    RESTORE(false), // Comment optional for restore from reject, no comment for restore from withdrawn
    WITHDRAW(false);

    private boolean requireComment;

    Action(boolean requireComment) {
        this.requireComment = requireComment;
    }

    public boolean isRequireComment() {
        return requireComment;
    }

    public static Action exchangeAndValidate(String actionName, ResourcePatchDTO resourcePatchDTO) {
        Action action = valueOf(actionName.toUpperCase());
        if (action.isRequireComment() && resourcePatchDTO.getComment() == null) {
            throw new BoardException(ExceptionCode.MISSING_COMMENT);
        }

        return action;
    }

}
