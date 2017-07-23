package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ApiAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> processException(Exception exception, WebRequest request) {
        Long id = null;
        ExceptionCode exceptionCode = ExceptionCode.PROBLEM;
        HttpStatus responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (exception instanceof BoardForbiddenException) {
            exceptionCode = ((BoardForbiddenException) exception).getExceptionCode();
            responseStatus = exceptionCode == ExceptionCode.UNAUTHENTICATED_USER ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        } else if (exception instanceof BoardDuplicateException) {
            BoardDuplicateException duplicateException = (BoardDuplicateException) exception;
            id = duplicateException.getId();
            exceptionCode = duplicateException.getExceptionCode();
            responseStatus = HttpStatus.CONFLICT;
        } else if (exception instanceof BoardNotModifiedException) {

        } else if (exception instanceof BoardException) {
            exceptionCode = ((BoardException) exception).getExceptionCode();
            responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        }

        LOGGER.error("Could not serve request", exception);
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        ImmutableMap.Builder<String, Object> bodyBuilder = ImmutableMap.<String, Object>builder()
            .put("timestamp", LocalDateTime.now())
            .put("uri", Joiner.on("?").skipNulls().join(servletRequest.getRequestURI(), servletRequest.getQueryString()))
            .put("status", responseStatus.value())
            .put("error", responseStatus.getReasonPhrase())
            .put("exceptionCode", exceptionCode);
        if (id != null) {
            bodyBuilder.put("id", id);
        }

        return handleExceptionInternal(exception, bodyBuilder.build(), new HttpHeaders(), responseStatus, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        return handleExceptionInternal(ex, errors, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

}
