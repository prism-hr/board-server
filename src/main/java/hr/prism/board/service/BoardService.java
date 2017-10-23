package hr.prism.board.service;

import freemarker.template.TemplateException;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.WidgetOptionsDTO;
import hr.prism.board.enums.*;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.util.BoardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardService {

    private static final List<ResourceTask> BADGE_TASKS = Collections.singletonList(ResourceTask.DEPLOY_BADGE);

    @Inject
    private BoardRepository boardRepository;

    @Inject
    private ActionService actionService;

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
    private ResourceTaskService resourceTaskService;

    @Inject
    private FreeMarkerConfig freemarkerConfig;

    @Value("${app.url}")
    private String appUrl;

    public Board getBoard(Long id) {
        User user = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(user, Scope.BOARD, id);
        return (Board) actionService.executeAction(user, board, Action.VIEW, () -> board);
    }

    public Board getBoard(String handle) {
        User user = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(user, Scope.BOARD, handle);
        return (Board) actionService.executeAction(user, board, Action.VIEW, () -> board);
    }

    public List<Board> getBoards(Long departmentId, Boolean includePublicBoards, State state, String quarter, String searchTerm) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            ResourceService.makeResourceFilter(Scope.BOARD, departmentId, includePublicBoards, state, quarter, searchTerm)
                .setOrderStatement("resource.name"))
            .stream().map(resource -> (Board) resource).collect(Collectors.toList());
    }

    public Board createBoard(Long departmentId, BoardDTO boardDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource department = resourceService.findOne(departmentId);
        return (Board) actionService.executeAction(currentUser, department, Action.EXTEND, () -> {
            String name = StringUtils.normalizeSpace(boardDTO.getName());
            resourceService.validateUniqueName(Scope.BOARD, null, department, name, ExceptionCode.DUPLICATE_BOARD);

            Board board = new Board();
            board.setName(name);
            board.setSummary(boardDTO.getSummary());

            BoardType type = boardDTO.getType();
            board.setType(type == null ? BoardType.CUSTOM : type);

            DocumentDTO documentLogoDTO = boardDTO.getDocumentLogo();
            if (documentLogoDTO != null) {
                board.setDocumentLogo(documentService.getOrCreateDocument(documentLogoDTO));
            } else {
                board.setDocumentLogo(department.getDocumentLogo());
            }

            String handle = resourceService.createHandle(department, name, boardRepository::findHandleLikeSuggestedHandle);
            board.setHandle(handle);
            board = boardRepository.save(board);

            resourceService.updateCategories(board, CategoryType.POST, boardDTO.getPostCategories());
            resourceService.createResourceRelation(department, board);
            resourceService.setIndexDataAndQuarter(board);
            userRoleService.createOrUpdateUserRole(board, currentUser, Role.ADMINISTRATOR);
            return board;
        });
    }

    public Board executeAction(Long id, Action action, BoardPatchDTO boardDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, id);
        board.setComment(boardDTO.getComment());
        return (Board) actionService.executeAction(currentUser, board, action, () -> {
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

    public String getBoardBadge(Long boardId, WidgetOptionsDTO options) {
        Board board = getBoard(boardId);
        Department department = (Department) board.getParent();
        Map<String, Object> model = createBoardBadgeModel(board, department, options);

        List<Post> posts = postService.getPosts(board.getId());
        posts = posts.subList(0, Math.min(posts.size(), options.getPostCount()));
        model.put("posts", posts);

        StringWriter stringWriter = new StringWriter();
        try {
            freemarkerConfig.getConfiguration().getTemplate("board_badge.ftl").process(model, stringWriter);
        } catch (IOException | TemplateException e) {
            throw new Error(e);
        }

        resourceTaskService.deleteTasks(department, BADGE_TASKS);
        return stringWriter.toString();
    }

    public void updateBoardPostCounts(List<Long> postIds, String state) {
        boardRepository.updateBoardPostCounts(postIds, state);
    }

    private Map<String, Object> createBoardBadgeModel(Board board, Department department, WidgetOptionsDTO options) {
        Map<String, Object> model = new HashMap<>();
        model.put("options", options);
        model.put("board", board);
        model.put("department", department);
        model.put("applicationUrl", appUrl);
        return model;
    }

    @SuppressWarnings("unchecked")
    private void updateBoard(Board board, BoardPatchDTO boardDTO) {
        board.setChangeList(new ChangeListRepresentation());
        resourcePatchService.patchName(board, boardDTO.getName(), ExceptionCode.DUPLICATE_BOARD);
        resourcePatchService.patchHandle(board, boardDTO.getHandle(), ExceptionCode.DUPLICATE_BOARD_HANDLE);
        resourcePatchService.patchDocument(board, "documentLogo", board::getDocumentLogo, board::setDocumentLogo, boardDTO.getDocumentLogo());
        resourcePatchService.patchProperty(board, "summary", board::getSummary, board::setSummary, boardDTO.getSummary());
        resourcePatchService.patchCategories(board, CategoryType.POST, boardDTO.getPostCategories());
        resourceService.setIndexDataAndQuarter(board);
        boardRepository.update(board);
    }

}
