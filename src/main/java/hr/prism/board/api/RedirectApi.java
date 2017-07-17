package hr.prism.board.api;

import com.google.common.base.Joiner;
import hr.prism.board.service.ResourceService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RedirectApi {

    @Inject
    private Environment environment;

    @Inject
    private ResourceService resourceService;

    @RequestMapping(value = "/api/redirect", method = RequestMethod.GET)
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String modal = request.getParameter("modal");
        if (modal == null) {
            throw new IllegalArgumentException("redirect must specify a modal action");
        }

        String contextPath = StringUtils.EMPTY;
        String resource = request.getParameter("resource");
        if (resource != null) {
            contextPath = Joiner.on("/").skipNulls().join(resourceService.findOne(Long.parseLong(resource)).getHandle(), request.getParameter("view"));
        }

        List<String> parameters = new ArrayList<>();
        String filter = request.getParameter("filter");
        if (filter != null) {
            parameters.add("filter=" + filter);
        }

        String uuid = request.getParameter("uuid");
        if (uuid != null) {
            parameters.add("uuid=" + uuid);
        }

        parameters.add("show" + modal + "=true");
        response.sendRedirect(environment.getProperty("app.url") + "/" + contextPath + "?" + Joiner.on("&").join(parameters));
    }

}
