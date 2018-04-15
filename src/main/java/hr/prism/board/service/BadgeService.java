package hr.prism.board.service;

import freemarker.template.TemplateException;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.dto.WidgetOptionsDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.prism.board.enums.ResourceTask.BADGE_TASKS;

@Service
@Transactional
public class BadgeService {

    private final String appUrl;

    private final PostService postService;

    private final ResourceTaskService resourceTaskService;

    private final FreeMarkerConfig freemarkerConfig;

    public BadgeService(@Value("${app.url}") String appUrl, PostService postService,
                        ResourceTaskService resourceTaskService, FreeMarkerConfig freemarkerConfig) {
        this.appUrl = appUrl;
        this.postService = postService;
        this.resourceTaskService = resourceTaskService;
        this.freemarkerConfig = freemarkerConfig;
    }

    // TODO add test coverage
    public String getResourceBadge(Resource resource, WidgetOptionsDTO options) {
        Map<String, Object> model = createResourceBadgeModel(resource, options);

        List<Post> posts = postService.getPosts(resource.getId());
        posts = posts.subList(0, Math.min(posts.size(), options.getPostCount()));
        model.put("posts", posts);

        StringWriter stringWriter = new StringWriter();
        try {
            freemarkerConfig.getConfiguration().getTemplate("badge.ftl").process(model, stringWriter);
        } catch (IOException | TemplateException e) {
            throw new Error(e);
        }

        if (BooleanUtils.isFalse(options.getPreview())) {
            resourceTaskService.completeTasks(resource, BADGE_TASKS);
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
