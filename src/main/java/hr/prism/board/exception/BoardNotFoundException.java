package hr.prism.board.exception;

public class BoardNotFoundException extends BoardException {
    
    public BoardNotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode, "Requested resource does not exist");
    }
    
}
