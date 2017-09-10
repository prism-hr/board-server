package hr.prism.board.exception;

public class BoardNotModifiedException extends BoardException {

    public BoardNotModifiedException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode, message);
    }

}
