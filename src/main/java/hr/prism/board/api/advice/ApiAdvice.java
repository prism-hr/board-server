package hr.prism.board.api.advice;

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
import org.springframework.security.access.AccessDeniedException;
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
import java.util.Map;

import static hr.prism.board.exception.ExceptionCode.UNAUTHENTICATED_USER;
import static hr.prism.board.exception.ExceptionCode.UNKNOWN;
import static java.util.Collections.emptyMap;
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

    @ExceptionHandler(BoardForbiddenException.class)
    public ResponseEntity<Object> handleBoardForbiddenException(BoardForbiddenException exception, WebRequest request) {
        ExceptionCode exceptionCode = exception.getExceptionCode();
        HttpStatus responseStatus = exceptionCode == UNAUTHENTICATED_USER ? UNAUTHORIZED : FORBIDDEN;
        return handleExceptionLoggingInfo(exception, request, responseStatus);
    }

    @ExceptionHandler(BoardDuplicateException.class)
    public ResponseEntity<Object> handleBoardDuplicateException(BoardDuplicateException exception, WebRequest request) {
        return handleExceptionLoggingInfo(exception, request, CONFLICT);
    }

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<Object> handleBoardNotFoundException(BoardNotFoundException exception,
                                                               WebRequest request) {
        return handleExceptionLoggingInfo(exception, request, NOT_FOUND);
    }

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<Object> handleBoardException(BoardException exception, WebRequest request) {
        ExceptionCode exceptionCode = exception.getExceptionCode();
        return handleExceptionLoggingError(exception, exceptionCode, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        Map<String, Object> response =
            makeResponse(UNAUTHENTICATED_USER, emptyMap(), UNAUTHORIZED, (ServletWebRequest) request);
        return handleExceptionInternal(exception, response, new HttpHeaders(), UNAUTHORIZED, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleBaseException(Exception exception, WebRequest request) {
        return handleExceptionLoggingError(exception, UNKNOWN, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        LOGGER.error(getUserString() + ": 422 - " +
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
        LOGGER.error(getUserString() + ": 500 - " +
            ((ServletWebRequest) request).getRequest().getServletPath(), exception);
        return super.handleExceptionInternal(exception, exception.getMessage(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        LOGGER.error(getUserString() + ": 400 - " +
            ((ServletWebRequest) request).getRequest().getServletPath(), exception);
        return super.handleExceptionInternal(exception, exception.getMessage(), headers, status, request);
    }

    private ResponseEntity<Object> handleExceptionLoggingInfo(BoardException exception, WebRequest request,
                                                              HttpStatus responseStatus) {
        ExceptionCode exceptionCode = exception.getExceptionCode();
        String userName = getUserString();
        LOGGER.info(userName + ": " + responseStatus + " - " + exception.getMessage());

        Map<String, Object> response =
            makeResponse(exceptionCode, exception.getProperties(), responseStatus, (ServletWebRequest) request);
        return handleExceptionInternal(exception, response, new HttpHeaders(), responseStatus, request);
    }

    private ResponseEntity<Object> handleExceptionLoggingError(Exception exception, ExceptionCode exceptionCode,
                                                               WebRequest request) {
        HttpStatus responseStatus = INTERNAL_SERVER_ERROR;
        String userName = getUserString();
        LOGGER.error(userName + ": " + responseStatus + " - " + exception.getMessage(), exception);

        Map<String, Object> response =
            makeResponse(exceptionCode, emptyMap(), responseStatus, (ServletWebRequest) request);
        return handleExceptionInternal(exception, response, new HttpHeaders(), responseStatus, request);
    }

    private String getUserString() {
        User user = userService.getUser();
        return user == null ? "Anonymous" : user.toString();
    }

    private Map<String, Object> makeResponse(ExceptionCode exceptionCode, Map<String, Object> properties,
                                             HttpStatus responseStatus, ServletWebRequest request) {
        HttpServletRequest servletRequest = request.getRequest();
        String uri = Joiner.on("?").skipNulls().join(servletRequest.getRequestURI(), servletRequest.getQueryString());
        ImmutableMap.Builder<String, Object> responseBuilder = ImmutableMap.<String, Object>builder()
            .put("uri", uri)
            .put("status", responseStatus.value())
            .put("error", responseStatus.getReasonPhrase())
            .put("exceptionCode", exceptionCode);

        properties.forEach(responseBuilder::put);
        return responseBuilder.build();
    }

}
