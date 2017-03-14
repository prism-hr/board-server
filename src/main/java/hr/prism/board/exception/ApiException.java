package hr.prism.board.exception;

public class ApiException extends RuntimeException {
    
    private ExceptionCode exceptionCode;
    
    public ApiException(ExceptionCode exceptionCode) {
        super(exceptionCode.name());
    }
    
    public ApiException(ExceptionCode exceptionCode, Throwable t) {
        super(exceptionCode.name(), t);
    }
    
}
