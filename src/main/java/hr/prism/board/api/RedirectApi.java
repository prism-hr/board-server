package hr.prism.board.api;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class RedirectApi {

    @Inject
    private Environment environment;

    @RequestMapping(value = "/api/redirect", method = RequestMethod.GET)
    public String redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getParameter("path");
        if (path == null) {
            throw new IllegalArgumentException("redirect must specify a path");
        }

        String appUrl = environment.getProperty("app.url");
        response.sendRedirect(appUrl + "/" + path);
        return null;
    }

}
