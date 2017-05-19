package hr.prism.board.api;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

@Controller
public class RedirectApi {
    
    @Inject
    private Environment environment;
    
    @RequestMapping(value = "/redirect", method = RequestMethod.GET)
    public String redirect(HttpServletRequest request) {
        String path = request.getParameter("path");
        if (path == null) {
            throw new IllegalArgumentException("redirect must specify a path");
        }
        
        String url = environment.getProperty("server.url");
        return "redirect:" + url + "/" + path;
    }
    
}
