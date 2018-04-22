package hr.prism.board.service;

import freemarker.template.TemplateException;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.dto.WidgetOptionsDTO;
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
import static java.lang.Math.min;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Service
@Transactional
public class DepartmentBadgeService {

    private final String appUrl;

    private final PostService postService;

    private final ResourceService resourceService;

    private final ResourceTaskService resourceTaskService;

    private final FreeMarkerConfig freemarkerConfig;

    public DepartmentBadgeService(@Value("${app.url}") String appUrl, PostService postService,
                                  ResourceService resourceService, ResourceTaskService resourceTaskService,
                                  FreeMarkerConfig freemarkerConfig) {
        this.appUrl = appUrl;
        this.postService = postService;
        this.resourceService = resourceService;
        this.resourceTaskService = resourceTaskService;
        this.freemarkerConfig = freemarkerConfig;
    }

    // TODO test coverage
    public String getBadge(Long id, WidgetOptionsDTO options) {
        Resource resource = resourceService.getById(id);
        Map<String, Object> model = makeBadgeModel(resource, options);

        List<Post> posts = postService.getPosts(resource.getId());
        posts = posts.subList(0, min(posts.size(), options.getPostCount()));
        model.put("posts", posts);

        StringWriter stringWriter = new StringWriter();
        try {
            freemarkerConfig.getConfiguration().getTemplate("badge.ftl").process(model, stringWriter);
        } catch (IOException | TemplateException e) {
            throw new Error(e);
        }

        if (isFalse(options.getPreview())) {
            resourceTaskService.completeTasks(resource, BADGE_TASKS);
        }

        return stringWriter.toString();
    }

    private Map<String, Object> makeBadgeModel(Resource resource, WidgetOptionsDTO options) {
        Map<String, Object> model = new HashMap<>();
        model.put("options", options);
        model.put("resource", resource);
        model.put("applicationUrl", appUrl);
        return model;
    }

}
