package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.User;
import hr.prism.board.exception.*;
import hr.prism.board.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ApiAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAdvice.class);

    @Inject
    private UserService userService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> processException(Exception exception, WebRequest request) {
        Long id = null;
        ExceptionCode exceptionCode = ExceptionCode.PROBLEM;
        HttpStatus responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        Class<? extends Exception> exceptionClass = exception.getClass();
        if (BoardException.class.isAssignableFrom(exceptionClass)) {
            exceptionCode = ((BoardException) exception).getExceptionCode();
            if (exceptionClass == BoardForbiddenException.class) {
                responseStatus = exceptionCode == ExceptionCode.UNAUTHENTICATED_USER ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
            } else if (exceptionClass == BoardDuplicateException.class) {
                id = ((BoardDuplicateException) exception).getId();
                responseStatus = HttpStatus.CONFLICT;
            } else if (exceptionClass == BoardNotModifiedException.class) {
                responseStatus = HttpStatus.NOT_MODIFIED;
            } else if (exceptionClass == BoardNotFoundException.class) {
                responseStatus = HttpStatus.NOT_FOUND;
            }
        }

        User user = userService.getCurrentUser();
        String userPrefix = user == null ? "Anonymous" : user.toString();
        if (responseStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            LOGGER.error(userPrefix + ": " + responseStatus + " - " + exception.getMessage(), exception);
        } else {
            LOGGER.info(userPrefix + ": " + responseStatus + " - " + exception.getMessage());
        }

        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        ImmutableMap.Builder<String, Object> responseBuilder = ImmutableMap.<String, Object>builder()
            .put("timestamp", LocalDateTime.now())
            .put("uri", Joiner.on("?").skipNulls().join(servletRequest.getRequestURI(), servletRequest.getQueryString()))
            .put("status", responseStatus.value())
            .put("error", responseStatus.getReasonPhrase())
            .put("exceptionCode", exceptionCode);
        if (id != null) {
            responseBuilder.put("id", id);
        }

        return handleExceptionInternal(exception, responseBuilder.build(), new HttpHeaders(), responseStatus, request);
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

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getMessage(), headers, status, request);
    }

}
