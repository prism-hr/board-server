package hr.prism.board.service;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
            String name = StringUtils.normalizeSpace(boardDTO.getName());
            validateNameUniqueness(name, department);
    
            Board board = new Board();
            board.setName(name);
            board.setDescription(boardDTO.getPurpose());
            board.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
    
            String handle = department.getHandle() + "/" + ResourceService.suggestHandle(name);
            List<String> similarHandles = boardRepository.findHandleLikeSuggestedHandle(handle);
            board.setHandle(ResourceService.confirmHandle(handle, similarHandles));
            board = boardRepository.save(board);
    
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
            // TODO: test coverage
            Optional<String> nameOptional = boardDTO.getName();
            if (nameOptional != null) {
                if (nameOptional.isPresent()) {
                    String name = nameOptional.get();
                    if (!name.equals(board.getName())) {
                        validateNameUniqueness(name, department);
                    }
            
                    board.setName(name);
                }
        
                throw new IllegalStateException("Attempted to set board name to null");
            }
    
            if (boardDTO.getPurpose() != null) {
                board.setDescription(boardDTO.getPurpose().orElse(null));
            }
    
            // TODO: test coverage
            Optional<String> handleOptional = boardDTO.getHandle();
            if (handleOptional != null) {
                if (handleOptional.isPresent()) {
                    String handle = department.getHandle() + "/" + handleOptional.get();
                    if (!handle.equals(board.getHandle())) {
                        if (boardRepository.findByHandle(handle) != null) {
                            throw new ApiException(ExceptionCode.DUPLICATE_BOARD_HANDLE);
                        }
                
                        resourceService.updateHandle(board, handle);
                    }
                }
        
                throw new IllegalStateException("Attempted to set board handle to null");
            }
    
            if (boardDTO.getPostCategories() != null) {
                resourceService.updateCategories(board, boardDTO.getPostCategories().orElse(Collections.emptyList()), CategoryType.POST);
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
    
}
