package hr.prism.board.service;

import freemarker.template.TemplateException;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.util.BoardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardService {

    @Inject
    private BoardRepository boardRepository;

    @Inject
    private ActionService actionService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourcePatchService resourcePatchService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @Inject
    private PostService postService;

    @Inject
    private FreeMarkerConfig freemarkerConfig;

    @Inject
    private Environment environment;

    public Board getBoard(Long id) {
        User currentUser = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, id);
        return (Board) actionService.executeAction(currentUser, board, Action.VIEW, () -> board);
    }

    public Board getBoard(String handle) {
        User currentUser = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, handle);
        return (Board) actionService.executeAction(currentUser, board, Action.VIEW, () -> board);
    }

    public List<Board> getBoards(Long departmentId, Boolean includePublicBoards) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilterDTO()
                .setScope(Scope.BOARD)
                .setParentId(departmentId)
                .setIncludePublicResources(includePublicBoards)
                .setOrderStatement("order by resource.name"))
            .stream().map(resource -> (Board) resource).collect(Collectors.toList());
    }

    public Board createBoard(BoardDTO boardDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Department department = departmentService.getOrCreateDepartment(currentUser, boardDTO.getDepartment());
        return (Board) actionService.executeAction(currentUser, department, Action.EXTEND, () -> {
            String name = StringUtils.normalizeSpace(boardDTO.getName());
            resourceService.validateUniqueName(Scope.BOARD, null, department, name, ExceptionCode.DUPLICATE_BOARD);

            Board board = new Board();
            board.setName(name);
            board.setSummary(boardDTO.getSummary());
            board.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);

            Document departmentLogo = department.getDocumentLogo();
            DocumentDTO documentLogoDTO = boardDTO.getDocumentLogo();
            if (documentLogoDTO != null) {
                board.setDocumentLogo(documentService.getOrCreateDocument(documentLogoDTO));
            } else if (departmentLogo != null) {
                board.setDocumentLogo(departmentLogo);
            }

            String handle = department.getHandle() + "/" + ResourceService.suggestHandle(name);
            List<String> similarHandles = boardRepository.findHandleLikeSuggestedHandle(handle);
            board.setHandle(ResourceService.confirmHandle(handle, similarHandles));

            board = boardRepository.save(board);
            resourceService.updateCategories(board, CategoryType.POST, boardDTO.getPostCategories());
            resourceService.createResourceRelation(department, board);
            userRoleService.createUserRole(board, currentUser, Role.ADMINISTRATOR);
            return board;
        });
    }

    public Board executeAction(Long id, Action action, BoardPatchDTO boardDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, id);
        return (Board) actionService.executeAction(currentUser, board.setComment(boardDTO.getComment()), action, () -> {
            if (action == Action.EDIT) {
                updateBoard(board, boardDTO);
            } else if (BoardUtils.hasUpdates(boardDTO)) {
                actionService.executeAction(currentUser, board, Action.EDIT, () -> {
                    updateBoard(board, boardDTO);
                    return board;
                });
            }

            return board;
        });
    }

    public String getBoardBadge(Board board, WidgetOptionsDTO options) {
        Department department = departmentService.getDepartment(board.getParent().getId());
        Map<String, Object> model = createBoardBadgeModel(board, department, options);

        List<Post> posts = postService.getPosts(board.getId(), true);
        posts = posts.subList(0, Math.min(posts.size(), options.getPostCount())); // FIXME use query params in order to limit results
        model.put("posts", posts);

        StringWriter stringWriter = new StringWriter();
        try {
            freemarkerConfig.getConfiguration().getTemplate("board_badge.ftl").process(model, stringWriter);
        } catch (IOException | TemplateException e) {
            throw new Error(e);
        }
        return stringWriter.toString();
    }

    private Map<String, Object> createBoardBadgeModel(Board board, Department department, WidgetOptionsDTO options) {
        Map<String, Object> model = new HashMap<>();
        model.put("options", options);
        model.put("board", board);
        model.put("department", department);
        model.put("applicationUrl", environment.getProperty("app.url"));
        return model;
    }

    @SuppressWarnings("unchecked")
    private void updateBoard(Board board, BoardPatchDTO boardDTO) {
        board.setChangeList(new ResourceChangeListRepresentation());
        resourcePatchService.patchName(board, boardDTO.getName(), ExceptionCode.DUPLICATE_BOARD);
        resourcePatchService.patchHandle(board, boardDTO.getHandle(), ExceptionCode.DUPLICATE_BOARD_HANDLE);
        resourcePatchService.patchDocument(board, "documentLogo", board::getDocumentLogo, board::setDocumentLogo, boardDTO.getDocumentLogo());
        resourcePatchService.patchProperty(board, "defaultPostVisibility", board::getDefaultPostVisibility, board::setDefaultPostVisibility,
            boardDTO.getDefaultPostVisibility());
        resourcePatchService.patchProperty(board, "summary", board::getSummary, board::setSummary, boardDTO.getSummary());
        resourcePatchService.patchCategories(board, CategoryType.POST, boardDTO.getPostCategories());
        boardRepository.update(board);
    }

}
