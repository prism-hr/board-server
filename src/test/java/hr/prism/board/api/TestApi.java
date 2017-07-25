package hr.prism.board.api;

import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApi {

    @RequestMapping(value = "/test/generalException", method = RequestMethod.GET)
    public void throwGeneralException() throws Throwable {
        throw new Throwable();
    }

    @RequestMapping(value = "/test/runtimeException", method = RequestMethod.GET)
    public void throwRuntimeException() {
        throw new RuntimeException();
    }

    @RequestMapping(value = "/test/apiException")
    public void throwApiException() {
        throw new BoardException(ExceptionCode.DUPLICATE_DEPARTMENT);
    }

    @RequestMapping(value = "/test/apiForbiddenException")
    public void throwApiForbiddenException() {
        throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
    }

}
