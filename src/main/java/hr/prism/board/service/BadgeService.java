package hr.prism.board.service;

import freemarker.template.TemplateException;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.dto.WidgetOptionsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BadgeService {

    @Inject
    private PostService postService;

    @Inject
    private FreeMarkerConfig freemarkerConfig;

    @Value("${app.url}")
    private String appUrl;

    public String getResourceBadge(Resource resource, WidgetOptionsDTO options) {
        Map<String, Object> model = createResourceBadgeModel(resource, options);

        // FIXME get posts also for department
        List<Post> posts = postService.getPosts(resource.getId());
        posts = posts.subList(0, Math.min(posts.size(), options.getPostCount()));
        model.put("posts", posts);

        StringWriter stringWriter = new StringWriter();
        try {
            freemarkerConfig.getConfiguration().getTemplate("badge.ftl").process(model, stringWriter);
        } catch (IOException | TemplateException e) {
            throw new Error(e);
        }

        return stringWriter.toString();
    }

    private Map<String, Object> createResourceBadgeModel(Resource resource, WidgetOptionsDTO options) {
        Map<String, Object> model = new HashMap<>();
        model.put("options", options);
        model.put("resource", resource);
        model.put("applicationUrl", appUrl);
        return model;
    }

}
