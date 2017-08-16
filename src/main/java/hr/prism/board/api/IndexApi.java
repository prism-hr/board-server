package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

@Controller
public class IndexApi {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private PostService postService;

    @Inject
    private Environment environment;

    @RequestMapping(value = "/api/index/{departmentHandle}", method = RequestMethod.GET)
    public String getDepartment(@PathVariable String departmentHandle, Model model) {
        Department department = departmentService.getDepartment(departmentHandle);
        if (department != null) {
            model.addAttribute("title", department.getName());
            model.addAttribute("description", department.getSummary());
            model.addAttribute("url", environment.getProperty("app.url") + "/" + departmentHandle);
            if (department.getDocumentLogo() != null) {
                model.addAttribute("image", department.getDocumentLogo().getCloudinaryUrl());
            }
            return "index";
        }
        return getGenericIndex(model);
    }

    @RequestMapping(value = "/api/index/{departmentHandle}/{boardHandle}", method = RequestMethod.GET)
    public String getBoard(@PathVariable String departmentHandle, @PathVariable String boardHandle, Model model) {
        Board board = boardService.getBoard(departmentHandle + "/" + boardHandle);
        if (board != null) {
            model.addAttribute("title", board.getName());
            model.addAttribute("description", board.getSummary());
            model.addAttribute("url", environment.getProperty("app.url") + "/" + board.getParent().getHandle() + "/" + boardHandle);
            if (board.getDocumentLogo() != null) {
                model.addAttribute("image", board.getDocumentLogo().getCloudinaryUrl());
            }
            return "index";
        }
        return getGenericIndex(model);
    }

    @RequestMapping(value = "/api/index/{departmentHandle}/{boardHandle}/{postId}", method = RequestMethod.GET)
    public String getPost(@PathVariable Long postId, Model model) {
        Post post = postService.getPost(postId);
        if (post != null) {
            Board board = (Board) post.getParent();
            model.addAttribute("title", post.getName());
            model.addAttribute("description", post.getSummary());
            model.addAttribute("url", environment.getProperty("app.url") + "/" + board.getParent().getHandle()
                + "/" + board.getHandle() + "/" + post.getId());
            if (post.getDocumentLogo() != null) {
                model.addAttribute("image", board.getDocumentLogo().getCloudinaryUrl());
            }
            return "index";
        }
        return getGenericIndex(model);
    }

    private String getGenericIndex(Model model) {
        model.addAttribute("title", "Prism");
        model.addAttribute("description", "Student placement marketplace");
        model.addAttribute("url", environment.getProperty("app.url"));
        model.addAttribute("image", environment.getProperty("app.url") + "/assets/prism-logo.png");
        return "index";
    }

}
