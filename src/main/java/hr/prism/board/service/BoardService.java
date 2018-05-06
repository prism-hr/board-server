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
import static java.util.stream.Collectors.toList;

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

    public Board getById(User user, Long id) {
        Board board = (Board) resourceService.getResource(user, BOARD, id);
        return (Board) actionService.executeAction(user, board, VIEW, () -> board);
    }

    public Board getByHandle(User user, String handle) {
        Board board = (Board) resourceService.getResource(user, BOARD, handle);
        return (Board) actionService.executeAction(user, board, VIEW, () -> board);
    }

    public List<Board> getBoards(User user, ResourceFilter filter) {
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

    public Board createBoard(User user, Long departmentId, BoardDTO boardDTO) {
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, departmentId);
        return (Board) actionService.executeAction(user, department, EXTEND, () -> {
            Board board = new Board();
            board.setParent(department);

            String name = boardDTO.getName();
            resourceService.checkUniqueName(board, name);
            board.setName(name);

            String handle = resourceService.createHandle(board);
            board.setHandle(handle);
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
        boardPatchService.patchName(board, boardDTO.getName());
        boardPatchService.patchHandle(board, boardDTO.getHandle());
        boardPatchService.patchPostCategories(board, boardDTO.getPostCategories());
        resourceService.setIndexDataAndQuarter(board);
    }

}
