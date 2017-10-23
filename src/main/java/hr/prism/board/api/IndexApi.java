package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

@Controller
public class IndexApi {

    private static final String SOCIAL_LOGO_URL = "http://res.cloudinary.com/board-prism-hr/image/upload/v1507475419/static/social.png";

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private PostService postService;

    @Value("${app.url}")
    private String appUrl;

    @RequestMapping(value = "/api/index/{universityHandle}/{departmentHandle}", method = RequestMethod.GET)
    public String getDepartment(@PathVariable String departmentHandle, Model model) {
        fillGenericModel(model);
        Department department = departmentService.getDepartment(departmentHandle);
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

    @RequestMapping(value = "/api/index/{universityHandle}/{departmentHandle}/{boardHandle}", method = RequestMethod.GET)
    public String getBoard(@PathVariable String departmentHandle, @PathVariable String boardHandle, Model model) {
        fillGenericModel(model);
        Board board = boardService.getBoard(departmentHandle + "/" + boardHandle);
        if (board != null) {
            model.addAttribute("title", board.getName());
            model.addAttribute("description", board.getSummary());
            model.addAttribute("url", appUrl + "/" + board.getParent().getHandle() + "/" + boardHandle);
            if (board.getDocumentLogo() != null) {
                model.addAttribute("image", board.getDocumentLogo().getCloudinaryUrl());
            }
        }

        return "index";
    }

    @RequestMapping(value = "/api/index/{universityHandle}/{departmentHandle}/{boardHandle}/{postId}", method = RequestMethod.GET)
    public String getPost(@PathVariable Long postId, Model model) {
        fillGenericModel(model);
        Post post = postService.getPost(postId);
        if (post != null) {
            Board board = (Board) post.getParent();
            model.addAttribute("title", post.getName());
            model.addAttribute("description", post.getSummary());
            model.addAttribute("url", appUrl + "/" + board.getParent().getHandle()
                + "/" + board.getHandle() + "/" + post.getId());
            if (board.getDocumentLogo() != null) {
                model.addAttribute("image", board.getDocumentLogo().getCloudinaryUrl());
            }
        }

        return "index";
    }

    @RequestMapping(value = "/api/index", method = RequestMethod.GET)
    public String getIndex(Model model) {
        fillGenericModel(model);
        return "index";
    }

    private void fillGenericModel(Model model) {
        model.addAttribute("title", "Prism");
        model.addAttribute("description", "Student job board");
        model.addAttribute("url", appUrl);
        model.addAttribute("image", SOCIAL_LOGO_URL);
        model.addAttribute("imageAlt", SOCIAL_LOGO_URL);
    }

}
