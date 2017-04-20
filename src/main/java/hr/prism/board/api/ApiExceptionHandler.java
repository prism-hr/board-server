package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@EnableWebMvc
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> processException(Exception exception, WebRequest request) {
        ExceptionCode exceptionCode = ExceptionCode.PROBLEM;
        HttpStatus responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (exception instanceof ApiForbiddenException) {
            exceptionCode = ((ApiForbiddenException) exception).getExceptionCode();
            responseStatus = HttpStatus.FORBIDDEN;
        } else if (exception instanceof ApiException) {
            exceptionCode = ((ApiException) exception).getExceptionCode();
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        
        LOGGER.error("Could not serve request", exception);
        return handleExceptionInternal(exception, ImmutableMap.of("exceptionCode", exceptionCode), new HttpHeaders(), responseStatus, request);
    }
    
}
