package hr.prism.board.service;

import com.google.common.base.Joiner;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;

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

    public List<Board> getBoards(Long departmentId) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilterDTO()
                .setScope(Scope.BOARD)
                .setParentId(departmentId)
                .setOrderStatement("order by resource.name"))
            .stream().map(resource -> (Board) resource).collect(Collectors.toList());
    }

    // TODO: notify the department administrator if they are not the creator
    public Board createBoard(BoardDTO boardDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        Board createdBoard = (Board) actionService.executeAction(currentUser, department, Action.EXTEND, () -> {
            String name = boardDTO.getName();
            validateNameUniqueness(name, department);

            Board board = new Board();
            board.setName(name);
            board.setDescription(boardDTO.getPurpose());
            board.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);

            board = boardRepository.save(board);

            updateHandle(board, boardDTO.getHandle(), department);
            resourceService.updateCategories(board, boardDTO.getPostCategories(), CategoryType.POST);
            resourceService.createResourceRelation(department, board);
            userRoleService.createUserRole(board, currentUser, Role.ADMINISTRATOR);
            return board;
        });

        return createdBoard;
    }

    public Board updateBoard(Long id, BoardPatchDTO boardDTO) {
        User currentUser = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, id);
        Board updatedBoard = (Board) actionService.executeAction(currentUser, board, Action.EDIT, () -> {
            Department department = (Department) board.getParent();
            if (boardDTO.getName() != null) {
                String newName = boardDTO.getName().orElse(null);
                if (!newName.equals(board.getName())) {
                    validateNameUniqueness(newName, department);
                }
                board.setName(boardDTO.getName().orElse(null));
            }

            if (boardDTO.getPurpose() != null) {
                board.setDescription(boardDTO.getPurpose().orElse(null));
            }

            if (boardDTO.getHandle() != null) {
                updateHandle(board, boardDTO.getHandle().orElse(null), department);
            }

            if (boardDTO.getPostCategories() != null) {
                resourceService.updateCategories(board, boardDTO.getPostCategories().orElse(null), CategoryType.POST);
            }

            if (boardDTO.getDefaultPostVisibility() != null) {
                board.setDefaultPostVisibility(boardDTO.getDefaultPostVisibility().orElse(null));
            }

            return board;
        });

        return updatedBoard;
    }

    private void validateNameUniqueness(String name, Department department) {
        Board board = boardRepository.findByNameAndDepartment(name, department);
        if (board != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_BOARD);
        }
    }

    private void updateHandle(Board board, String handle, Department department) {
        String fullHandle = Joiner.on("/").join(department.getHandle(), handle);
        if (!fullHandle.equals(board.getHandle())) {
            if (boardRepository.findByHandle(fullHandle) != null) {
                throw new ApiException(ExceptionCode.DUPLICATE_BOARD_HANDLE);
            }

            resourceService.updateHandle(board, fullHandle);
        }
    }

}
