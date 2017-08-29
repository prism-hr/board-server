package hr.prism.board.exception;

public class BoardException extends RuntimeException {

    private ExceptionCode exceptionCode;

    public BoardException(ExceptionCode exceptionCode) {
        super(exceptionCode.name());
        this.exceptionCode = exceptionCode;
    }

    public BoardException(ExceptionCode exceptionCode, Throwable t) {
        super(exceptionCode.name(), t);
        this.exceptionCode = exceptionCode;
    }

    public BoardException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode.name() + ": " + message);
        this.exceptionCode = exceptionCode;
    }

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

}
