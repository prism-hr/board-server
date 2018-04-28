package hr.prism.board.exception;

import java.util.HashMap;
import java.util.Map;

public class BoardException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    final Map<String, Object> properties = new HashMap<>();

    public BoardException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode.name() + ": " + message);
        this.exceptionCode = exceptionCode;
    }

    public BoardException(ExceptionCode exceptionCode, String message, Throwable throwable) {
        super(exceptionCode.name() + ": " + message, throwable);
        this.exceptionCode = exceptionCode;
    }

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
