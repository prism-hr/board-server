package hr.prism.board.exception;

public class BoardException extends RuntimeException {

    private ExceptionCode exceptionCode;

    public BoardException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode.name() + ": " + message);
        this.exceptionCode = exceptionCode;
    }

    public BoardException(ExceptionCode exceptionCode, String message, Throwable t) {
        super(exceptionCode.name() + ": " + message, t);
        this.exceptionCode = exceptionCode;
    }

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

}
