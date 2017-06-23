package hr.prism.board.api;

import hr.prism.board.service.ResourceService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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

    @Inject
    private ResourceService resourceService;

    @RequestMapping(value = "/api/redirect", method = RequestMethod.GET)
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if (action == null) {
            throw new IllegalArgumentException("redirect must specify an action");
        }

        String handle = StringUtils.EMPTY;
        String resource = request.getParameter("resource");
        if (resource != null) {
            handle = resourceService.findOne(Long.parseLong(resource)).getHandle();
        }

        String appUrl = environment.getProperty("app.url");
        response.sendRedirect(appUrl + "/" + handle + "?show" + WordUtils.capitalize(action) + "=true");
    }

}
