package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.util.BoardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.inject.Inject;
import java.util.List;
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
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private FreeMarkerConfig freemarkerConfig;

    @Value("${app.url}")
    private String appUrl;

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

    public List<Board> getBoards(Long departmentId, Boolean includePublicBoards, State state, String quarter, String searchTerm) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            ResourceService.makeResourceFilter(Scope.BOARD, departmentId, includePublicBoards, state, quarter, searchTerm)
                .setOrderStatement("resource.name"))
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

    public void updateBoardPostCounts(List<Long> postIds, String state) {
        boardRepository.updateBoardPostCounts(postIds, state);
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
