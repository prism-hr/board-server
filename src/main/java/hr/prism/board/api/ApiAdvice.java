package hr.prism.board.api;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.User;
import hr.prism.board.exception.*;
import hr.prism.board.service.UserService;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
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
import java.util.ArrayList;
import java.util.List;

import static hr.prism.board.exception.ExceptionCode.PROBLEM;
import static hr.prism.board.exception.ExceptionCode.UNAUTHENTICATED_USER;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ApiAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = getLogger(ApiAdvice.class);

    private final UserService userService;

    @Inject
    public ApiAdvice(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> processException(Exception exception, WebRequest request) {
        Long id = null;
        ExceptionCode exceptionCode = PROBLEM;
        HttpStatus responseStatus = INTERNAL_SERVER_ERROR;

        Class<? extends Exception> exceptionClass = exception.getClass();
        if (BoardException.class.isAssignableFrom(exceptionClass)) {
            exceptionCode = ((BoardException) exception).getExceptionCode();
            if (exceptionClass == BoardForbiddenException.class) {
                responseStatus = exceptionCode == UNAUTHENTICATED_USER ? UNAUTHORIZED : FORBIDDEN;
            } else if (exceptionClass == BoardDuplicateException.class) {
                id = ((BoardDuplicateException) exception).getId();
                responseStatus = CONFLICT;
            } else if (exceptionClass == BoardNotModifiedException.class) {
                responseStatus = NOT_MODIFIED;
            } else if (exceptionClass == BoardNotFoundException.class) {
                responseStatus = NOT_FOUND;
            }
        }

        String userPrefix = getCurrentUsername();
        if (responseStatus == INTERNAL_SERVER_ERROR) {
            LOGGER.error(userPrefix + ": " + responseStatus + " - " + exception.getMessage(), exception);
        } else {
            LOGGER.info(userPrefix + ": " + responseStatus + " - " + exception.getMessage());
        }

        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        String uri = Joiner.on("?").skipNulls().join(servletRequest.getRequestURI(), servletRequest.getQueryString());
        ImmutableMap.Builder<String, Object> responseBuilder = ImmutableMap.<String, Object>builder()
            .put("uri", uri)
            .put("status", responseStatus.value())
            .put("error", responseStatus.getReasonPhrase())
            .put("exceptionCode", exceptionCode);
        if (id != null) {
            responseBuilder.put("id", id);
        }

        return handleExceptionInternal(exception, responseBuilder.build(), new HttpHeaders(), responseStatus, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        LOGGER.error(getCurrentUsername() + ": 422 - " +
            ((ServletWebRequest) request).getRequest().getServletPath(), exception);
        List<String> errors = new ArrayList<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        for (ObjectError error : exception.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        return handleExceptionInternal(exception, errors, headers, UNPROCESSABLE_ENTITY, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException exception,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        LOGGER.error(getCurrentUsername() + ": 500 - " +
            ((ServletWebRequest) request).getRequest().getServletPath(), exception);
        return super.handleExceptionInternal(exception, exception.getMessage(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        LOGGER.error(getCurrentUsername() + ": 400 - " +
            ((ServletWebRequest) request).getRequest().getServletPath(), exception);
        return super.handleExceptionInternal(exception, exception.getMessage(), headers, status, request);
    }

    private String getCurrentUsername() {
        User user = userService.getCurrentUser();
        return user == null ? "Anonymous" : user.toString();
    }

}
