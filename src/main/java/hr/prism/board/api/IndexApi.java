package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class IndexApi {

    private static final String SOCIAL_LOGO_URL =
        "http://res.cloudinary.com/board-prism-hr/image/upload/v1507475419/static/social.png";

    private final String appUrl;

    private final String facebookClientId;

    private final DepartmentService departmentService;

    private final PostService postService;

    @Inject
    public IndexApi(@Value("${app.url}") String appUrl, @Value("${auth.facebook.clientId}") String facebookClientId,
                    DepartmentService departmentService, PostService postService) {
        this.appUrl = appUrl;
        this.facebookClientId = facebookClientId;
        this.departmentService = departmentService;
        this.postService = postService;
    }

    @RequestMapping(value = "/api/index/privacyPolicy", method = GET)
    public String getPrivacyPolicy(Model model) {
        fillGenericModel(model);
        return "privacyPolicy";
    }

    @RequestMapping(value = "/api/index/{universityHandle}/{departmentHandle}", method = GET)
    public String getDepartmentIndex(@PathVariable String universityHandle, @PathVariable String departmentHandle,
                                     Model model) {
        fillGenericModel(model);
        Department department = departmentService.getDepartment(universityHandle + "/" + departmentHandle);
        if (department != null) {
            model.addAttribute("title", department.getName());
            model.addAttribute("description", department.getSummary());
            model.addAttribute("url", appUrl + "/" + departmentHandle);
            if (department.getDocumentLogo() != null) {
                model.addAttribute("image", department.getDocumentLogo().getCloudinaryUrl());
            }
        }

        return "index";
    }

    @RequestMapping(value = "/api/index/{universityHandle}/{departmentHandle}/{boardHandle}/{postId}", method = GET)
    public String getPostIndex(@PathVariable Long postId, Model model) {
        fillGenericModel(model);
        Post post = postService.getPost(postId);
        if (post != null) {
            Board board = (Board) post.getParent();
            model.addAttribute("title", post.getName());
            model.addAttribute("description", post.getSummary());
            model.addAttribute("url", appUrl + "/" + board.getHandle() + "/" + post.getId());

            Department department = (Department) board.getParent();
            if (department.getDocumentLogo() != null) {
                model.addAttribute("image", department.getDocumentLogo().getCloudinaryUrl());
            }
        }

        return "index";
    }

    @RequestMapping(value = "/api/index", method = GET)
    public String getSiteIndex(Model model) {
        fillGenericModel(model);
        return "index";
    }

    private void fillGenericModel(Model model) {
        model.addAttribute("title", "Prism");
        model.addAttribute("description", "Student job board");
        model.addAttribute("url", appUrl);
        model.addAttribute("type", "website");
        model.addAttribute("image", SOCIAL_LOGO_URL);
        model.addAttribute("imageAlt", SOCIAL_LOGO_URL);
        model.addAttribute("facebookAppId", facebookClientId);
    }

}
