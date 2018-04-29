package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.ResourceFilter;
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
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    private final ActionService actionService;

    private final ResourceService resourceService;

    private final BoardPatchService boardPatchService;

    private final UserService userService;

    @Inject
    public BoardService(BoardRepository boardRepository, ActionService actionService, ResourceService resourceService,
                        BoardPatchService boardPatchService, UserService userService) {
        this.boardRepository = boardRepository;
        this.actionService = actionService;
        this.resourceService = resourceService;
        this.boardPatchService = boardPatchService;
        this.userService = userService;
    }

    public Board getById(Long id) {
        User user = userService.getUser();
        Board board = (Board) resourceService.getResource(user, BOARD, id);
        return (Board) actionService.executeAction(user, board, VIEW, () -> board);
    }

    public Board getById(String handle) {
        User user = userService.getUser();
        Board board = (Board) resourceService.getResource(user, BOARD, handle);
        return (Board) actionService.executeAction(user, board, VIEW, () -> board);
    }

    public List<Board> getBoards(ResourceFilter filter) {
        User user = userService.getUser();
        filter.setScope(BOARD);
        filter.setOrderStatement("resource.name");

        List<Resource> resources = resourceService.getResources(user, filter);
        return resources.stream()
            .map(resource -> (Board) resource)
            .collect(toList());
    }

    public List<ResourceOperation> getBoardOperations(Long id) {
        User user = userService.getUserSecured();
        Board board = (Board) resourceService.getResource(user, BOARD, id);
        actionService.executeAction(user, board, EDIT, () -> board);
        return resourceService.getResourceOperations(board);
    }

    public Board createBoard(Long departmentId, BoardDTO boardDTO) {
        User user = userService.getUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, departmentId);
        return (Board) actionService.executeAction(user, department, EXTEND, () -> {
            String name = normalizeSpace(boardDTO.getName());
            resourceService.checkUniqueName(BOARD, null, department, name, DUPLICATE_BOARD);

            Board board = new Board();
            board.setName(name);

            board.setHandle(
                resourceService.createHandle(department, BOARD, name));
            board = boardRepository.save(board);

            resourceService.updateCategories(board, POST, boardDTO.getPostCategories());
            resourceService.createResourceRelation(department, board);
            resourceService.setIndexDataAndQuarter(board);
            return board;
        });
    }

    public Board executeAction(Long id, Action action, BoardPatchDTO boardDTO) {
        User currentUser = userService.getUserSecured();
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
        boardPatchService.patchName(board, boardDTO.getName(), DUPLICATE_BOARD);
        boardPatchService.patchHandle(board, boardDTO.getHandle(), DUPLICATE_BOARD_HANDLE);
        boardPatchService.patchPostCategories(board, boardDTO.getPostCategories());
        resourceService.setIndexDataAndQuarter(board);
    }

}
