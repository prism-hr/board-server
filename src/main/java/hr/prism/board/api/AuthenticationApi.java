package hr.prism.board.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationApi {
    
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @RequestMapping(value = {"/register", "/login", "/forgot", "/logout"}, method = RequestMethod.GET)
    public void suppressStormpathMvcViews() {
    }
    
    @RequestMapping(value = "/postLogout", method = RequestMethod.POST)
    public void postLogout() {
    }
    
}
