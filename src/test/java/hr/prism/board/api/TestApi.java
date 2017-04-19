package hr.prism.board.api;

import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
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
    
    @RequestMapping(value = "/test/apiException")
    public void throwApiException() {
        throw new ApiException(ExceptionCode.DUPLICATE_DEPARTMENT);
    }
    
    @RequestMapping
    public void throwApiForbiddenException() {
        throw new ApiForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
    }
    
}
