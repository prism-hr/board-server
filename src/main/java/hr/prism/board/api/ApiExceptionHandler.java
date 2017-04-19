package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class ApiExceptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleException(Throwable throwable) throws Throwable {
        if (throwable instanceof ApiException || throwable instanceof ApiForbiddenException) {
            throw throwable;
        }
        
        LOGGER.error("Could not serve request", throwable);
        return ImmutableMap.of("exceptionCode", ExceptionCode.PROBLEM.name());
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
    @ExceptionHandler(ApiForbiddenException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public Map<String, String> handleException(ApiForbiddenException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
