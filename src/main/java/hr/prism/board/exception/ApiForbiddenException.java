package hr.prism.board.exception;

public class ApiForbiddenException extends ApiException {
    
    public ApiForbiddenException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
    
}
