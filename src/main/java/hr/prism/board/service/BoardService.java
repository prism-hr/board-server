package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import org.apache.commons.lang3.StringUtils;
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
    private ResourcePatchService resourcePatchService;
    
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
        return (Board) actionService.executeAction(currentUser, department, Action.EXTEND, () -> {
            String name = StringUtils.normalizeSpace(boardDTO.getName());
            resourceService.validateUniqueName(Scope.BOARD, null, department, name, ExceptionCode.DUPLICATE_BOARD);
            
            Board board = new Board();
            board.setName(name);
            board.setDescription(boardDTO.getDescription());
            board.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
    
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
    
    @SuppressWarnings("unchecked")
    public Board updateBoard(Long id, BoardPatchDTO boardDTO) {
        User currentUser = userService.getCurrentUser();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, id);
        return (Board) actionService.executeAction(currentUser, board, Action.EDIT, () -> {
            board.setChangeList(new ResourceChangeListRepresentation());
            resourcePatchService.patchName(board, boardDTO.getName(), ExceptionCode.MISSING_BOARD_NAME, ExceptionCode.DUPLICATE_BOARD);
            resourcePatchService.patchProperty(board, "description", board::getDescription, board::setDescription, boardDTO.getDescription());
            resourcePatchService.patchHandle(board, boardDTO.getHandle(), ExceptionCode.MISSING_BOARD_HANDLE, ExceptionCode.DUPLICATE_BOARD_HANDLE);
            resourcePatchService.patchCategories(board, CategoryType.POST, boardDTO.getPostCategories());
            resourcePatchService.patchProperty(board, "defaultPostVisibility", board::getDefaultPostVisibility, board::setDefaultPostVisibility,
                boardDTO.getDefaultPostVisibility(), ExceptionCode.MISSING_BOARD_DEFAULT_VISIBILITY);
            board.setComment(boardDTO.getComment());
            return board;
        });
    }
    
}
