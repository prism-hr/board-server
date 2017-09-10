package hr.prism.board.exception;

public class BoardForbiddenException extends BoardException {

    public BoardForbiddenException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode, message);
    }

}
