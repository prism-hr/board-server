package hr.prism.board.exception;

import hr.prism.board.enums.Scope;

public class BoardNotFoundException extends BoardException {

    public BoardNotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode, "Requested resource does not exist");
    }

    public BoardNotFoundException(ExceptionCode exceptionCode, Scope scope, Long id) {
        super(exceptionCode, scope + " ID: " + id + " does not exist");
    }

}
