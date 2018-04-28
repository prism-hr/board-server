package hr.prism.board.api;

import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.BoardNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static hr.prism.board.enums.Scope.UNIVERSITY;
import static hr.prism.board.exception.ExceptionCode.*;

@RestController
public class TestApi {

    @RequestMapping(value = "/test/throwable", method = RequestMethod.GET)
    public void throwThrowable() throws Throwable {
        throw new Throwable();
    }

    @RequestMapping(value = "/test/exception", method = RequestMethod.GET)
    public void throwException() throws Exception {
        throw new Exception();
    }

    @RequestMapping(value = "/test/runtimeException", method = RequestMethod.GET)
    public void throwRuntimeException() {
        throw new RuntimeException();
    }

    @RequestMapping(value = "/test/boardDuplicateException")
    public void throwBoardDuplicateException() {
        throw new BoardDuplicateException(DUPLICATE_DEPARTMENT, "Department already exists", 1L);
    }

    @RequestMapping(value = "/test/boardException")
    public void throwBoardException() {
        throw new BoardException(MISSING_COMMENT, "Comment must be provided");
    }

    @RequestMapping(value = "/test/boardForbiddenException")
    public void throwBoardForbiddenException() {
        throw new BoardForbiddenException(UNAUTHENTICATED_USER, "User cannot be authenticated");
    }

    @RequestMapping(value = "/test/boardNotFoundException")
    public void throwBoardNotFoundException() {
        throw new BoardNotFoundException(MISSING_RESOURCE, UNIVERSITY, 1L);
    }

}
