package hr.prism.board.api;

import com.google.common.base.Joiner;
import hr.prism.board.service.ResourceService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class RedirectApi {

    @Inject
    private ResourceService resourceService;

    @Value("${app.url}")
    private String appUrl;

    @RequestMapping(value = "/api/redirect", method = RequestMethod.GET)
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = "home";
        String resource = request.getParameter("resource");
        if (resource != null) {
            contextPath = Joiner.on("/").skipNulls().join(resourceService.findOne(Long.parseLong(resource)).getHandle(), request.getParameter("view"));
        }

        List<String> parameters = Stream.of("uuid", "resetPasswordUuid", "unsubscribeUuid")
            .filter(param -> request.getParameter(param) != null)
            .map(param -> param + "=" + request.getParameter(param))
            .collect(Collectors.toList());

        String redirectUrl = appUrl + "/" + contextPath;
        redirectUrl += "?" + Joiner.on("&").join(parameters);

        if (request.getParameter("fragment") != null) {
            redirectUrl += "#" + request.getParameter("fragment");
        }

        response.sendRedirect(redirectUrl);
    }

}
