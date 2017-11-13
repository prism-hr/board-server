package hr.prism.board.exception;

public class BoardNotificationException extends BoardException {

    public BoardNotificationException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode, message);
    }

}
