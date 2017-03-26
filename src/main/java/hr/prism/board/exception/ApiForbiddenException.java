package hr.prism.board.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ApiForbiddenException extends RuntimeException {
    
    public ApiForbiddenException(String message) {
        super(message);
    }
    
}