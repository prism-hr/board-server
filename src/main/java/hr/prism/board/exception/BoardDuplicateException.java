package hr.prism.board.exception;

public class BoardDuplicateException extends BoardException {

    public BoardDuplicateException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode, message);
    }

    public BoardDuplicateException(ExceptionCode exceptionCode, String message, Long id) {
        super(exceptionCode, message);
        properties.put("id", id);
    }

}
