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
import hr.prism.board.representation.ResourceChangeListRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
            validateNameUnique(name, department);
            
            Board board = new Board();
            board.setName(name);
            board.setDescription(boardDTO.getPurpose());
            board.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
    
            String handle = department.getHandle() + "/" + ResourceService.suggestHandle(name);
            List<String> similarHandles = boardRepository.findHandleLikeSuggestedHandle(handle);
            board.setHandle(ResourceService.confirmHandle(handle, similarHandles));
    
            validateBoard(board);
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
            ResourceChangeListRepresentation changeList = new ResourceChangeListRepresentation();
    
            Optional<String> nameOptional = boardDTO.getName();
            if (nameOptional != null) {
                String oldName = board.getName();
                String newName = nameOptional.orElse(null);
                if (!Objects.equals(newName, oldName)) {
                    validateNameUnique(newName, department);
                    board.setName(newName);
                    changeList.put("name", oldName, newName);
                }
            }
    
            if (boardDTO.getPurpose() != null) {
                String oldPurpose = board.getDescription();
                board.setDescription(boardDTO.getPurpose().orElse(null));
                changeList.put("purpose", oldPurpose, board.getDescription());
            }
    
            Optional<String> handleOptional = boardDTO.getHandle();
            if (handleOptional != null) {
                String oldHandle = board.getHandle();
                String newHandle = handleOptional.orElse(null);
                newHandle = newHandle == null ? null : department.getHandle() + "/" + newHandle;
                if (!Objects.equals(newHandle, oldHandle)) {
                    if (boardRepository.findByHandle(newHandle) != null) {
                        throw new ApiException(ExceptionCode.DUPLICATE_BOARD_HANDLE);
                    }
            
                    resourceService.updateHandle(board, newHandle);
                    changeList.put("handle", oldHandle, newHandle);
                }
            }
    
            if (boardDTO.getPostCategories() != null) {
                List<String> oldPostCategories = resourceService.getCategories(board, CategoryType.POST);
                resourceService.updateCategories(board, boardDTO.getPostCategories().orElse(Collections.emptyList()), CategoryType.POST);
                changeList.put("postCategories", oldPostCategories, resourceService.getCategories(board, CategoryType.POST));
            }
    
            if (boardDTO.getDefaultPostVisibility() != null) {
                PostVisibility oldDefaultPostVisibility = board.getDefaultPostVisibility();
                board.setDefaultPostVisibility(boardDTO.getDefaultPostVisibility().orElse(null));
                changeList.put("defaultPostVisibility", oldDefaultPostVisibility, board.getDefaultPostVisibility());
            }
    
            validateBoard(board);
            board.setChangeList(changeList);
            board.setComment(boardDTO.getComment());
            return board;
        });
    
        return updatedBoard;
    }
    
    private void validateNameUnique(String name, Department department) {
        Board board = boardRepository.findByNameAndDepartment(name, department);
        if (board != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_BOARD);
        }
    }
    
    private void validateBoard(Board board) {
        if (board.getName() == null) {
            throw new ApiException(ExceptionCode.MISSING_BOARD_NAME);
        } else if (board.getHandle() == null) {
            throw new ApiException(ExceptionCode.MISSING_BOARD_HANDLE);
        }
    }
    
}
