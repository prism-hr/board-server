package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.utils.BoardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_BOARD;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_BOARD_HANDLE;
import static hr.prism.board.utils.ResourceUtils.makeResourceFilter;
import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    private final ActionService actionService;

    private final ResourceService resourceService;

    private final ResourcePatchService resourcePatchService;

    private final UserService userService;

    @Inject
    public BoardService(BoardRepository boardRepository, ActionService actionService, ResourceService resourceService,
                        ResourcePatchService resourcePatchService, UserService userService) {
        this.boardRepository = boardRepository;
        this.actionService = actionService;
        this.resourceService = resourceService;
        this.resourcePatchService = resourcePatchService;
        this.userService = userService;
    }

    public Board getBoard(Long id) {
        User user = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(user, BOARD, id);
        return (Board) actionService.executeAction(user, board, VIEW, () -> board);
    }

    public Board getBoard(String handle) {
        User user = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(user, BOARD, handle);
        return (Board) actionService.executeAction(user, board, VIEW, () -> board);
    }

    public List<Board> getBoards(Long departmentId, Boolean includePublicBoards, State state, String quarter,
                                 String searchTerm) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            makeResourceFilter(BOARD,
                departmentId, includePublicBoards, state, quarter, searchTerm).setOrderStatement("resource.name"))
            .stream().map(resource -> (Board) resource).collect(toList());
    }

    public List<ResourceOperation> getBoardOperations(Long id) {
        User user = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(user, BOARD, id);
        actionService.executeAction(user, board, EDIT, () -> board);
        return resourceService.getResourceOperations(board);
    }

    public Board createBoard(Long departmentId, BoardDTO boardDTO) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, DEPARTMENT, departmentId);
        return (Board) actionService.executeAction(user, department, EXTEND, () -> {
            String name = StringUtils.normalizeSpace(boardDTO.getName());
            resourceService.validateUniqueName(BOARD, null, department, name, DUPLICATE_BOARD);

            Board board = new Board();
            board.setName(name);

            board.setHandle(
                resourceService.createHandle(department, name, boardRepository::findHandleLikeSuggestedHandle));
            board = boardRepository.save(board);

            resourceService.updateCategories(board, POST, boardDTO.getPostCategories());
            resourceService.createResourceRelation(department, board);
            resourceService.setIndexDataAndQuarter(board);
            return board;
        });
    }

    public Board executeAction(Long id, Action action, BoardPatchDTO boardDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(currentUser, BOARD, id);
        board.setComment(boardDTO.getComment());
        return (Board) actionService.executeAction(currentUser, board, action, () -> {
            if (action == EDIT) {
                updateBoard(board, boardDTO);
            } else if (BoardUtils.hasUpdates(boardDTO)) {
                actionService.executeAction(currentUser, board, EDIT, () -> {
                    updateBoard(board, boardDTO);
                    return board;
                });
            }

            return board;
        });
    }

    private void updateBoard(Board board, BoardPatchDTO boardDTO) {
        board.setChangeList(new ChangeListRepresentation());
        resourcePatchService.patchName(board, boardDTO.getName(), DUPLICATE_BOARD);
        resourcePatchService.patchHandle(board, boardDTO.getHandle(), DUPLICATE_BOARD_HANDLE);
        resourcePatchService.patchCategories(board, POST, boardDTO.getPostCategories());
        resourceService.setIndexDataAndQuarter(board);
        boardRepository.update(board);
    }

}
