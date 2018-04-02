package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.utils.BoardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "WeakerAccess"})
public class BoardService {

    @Inject
    private BoardRepository boardRepository;

    @Inject
    private ActionService actionService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourcePatchService resourcePatchService;

    @Inject
    private UserService userService;

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
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        return (Board) actionService.executeAction(user, department, Action.EXTEND, () -> {
            String name = StringUtils.normalizeSpace(boardDTO.getName());
            resourceService.validateUniqueName(Scope.BOARD, null, department, name, ExceptionCode.DUPLICATE_BOARD);

            Board board = new Board();
            board.setName(name);

            board.setHandle(resourceService.createHandle(department, name, boardRepository::findHandleLikeSuggestedHandle));
            board = boardRepository.save(board);

            resourceService.updateCategories(board, CategoryType.POST, boardDTO.getPostCategories());
            resourceService.createResourceRelation(department, board);
            resourceService.setIndexDataAndQuarter(board);
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

    @SuppressWarnings("unchecked")
    private void updateBoard(Board board, BoardPatchDTO boardDTO) {
        board.setChangeList(new ChangeListRepresentation());
        resourcePatchService.patchName(board, boardDTO.getName(), ExceptionCode.DUPLICATE_BOARD);
        resourcePatchService.patchHandle(board, boardDTO.getHandle(), ExceptionCode.DUPLICATE_BOARD_HANDLE);
        resourcePatchService.patchCategories(board, CategoryType.POST, boardDTO.getPostCategories());
        resourceService.setIndexDataAndQuarter(board);
        boardRepository.update(board);
    }

}
