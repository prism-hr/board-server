package hr.prism.board.service;

import com.google.common.base.Joiner;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class BoardService {
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private BoardRepository boardRepository;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private UserService userService;
    
    public List<Board> findAllByUserOrderByName() {
        User user = userService.getCurrentUserSecured();
        Collection<Long> boardIds = user.getResources().keySet();
        if (boardIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return boardRepository.findAllByUserByOrderByName(boardIds);
    }
    
    public Board findOne(Long id) {
        return boardRepository.findOne(id);
    }
    
    public Board findByHandle(String handle) {
        return boardRepository.findByHandle(handle);
    }
    
    // TODO: notify the department administrator if they are not the creator
    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
    
        String name = boardDTO.getName();
        validateNameUniqueness(name, department);
    
        Board board = new Board();
        resourceService.updateState(board, State.ACCEPTED);
        board.setName(name);
        board.setDescription(boardDTO.getPurpose());
    
        BoardSettingsDTO settingsDTO = boardDTO.getSettings();
        if (settingsDTO == null) {
            settingsDTO = new BoardSettingsDTO();
        }
    
        if (boardDTO.getSettings().getDefaultPostVisibility() == null) {
            settingsDTO.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
        }
    
        board = boardRepository.save(board);
        updateBoardSettings(board, settingsDTO, department);
        resourceService.createResourceRelation(department, board);
    
        User currentUser = userService.getCurrentUserSecured();
        userRoleService.createUserRole(board, currentUser, Role.ADMINISTRATOR);
        currentUser.setResources(resourceService.getResources(new ResourceFilterDTO().setScope(Scope.BOARD).setId(board.getId())));
        return board;
    }
    
    public void updateBoard(Long boardId, BoardDTO boardDTO) {
        Board board = boardRepository.findOne(boardId);
        
        String newName = boardDTO.getName();
        if (!newName.equals(board.getName())) {
            Department department = (Department) board.getParent();
            validateNameUniqueness(newName, department);
        }
        
        board.setName(boardDTO.getName());
        board.setDescription(boardDTO.getPurpose());
    }
    
    public void updateBoardSettings(Long id, BoardSettingsDTO boardSettingsDTO) {
        Board board = boardRepository.findOne(id);
        Department department = (Department) board.getParent();
        updateBoardSettings(board, boardSettingsDTO, department);
    }
    
    public List<Board> findByDepartment(Department department) {
        return boardRepository.findByDepartment(department);
    }
    
    private void validateNameUniqueness(String name, Department department) {
        Board board = boardRepository.findByNameAndDepartment(name, department);
        if (board != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_BOARD);
        }
    }
    
    private void updateBoardSettings(Board board, BoardSettingsDTO boardSettingsDTO, Department department) {
        String handle = Joiner.on("/").join(department.getHandle(), boardSettingsDTO.getHandle());
        if (!handle.equals(board.getHandle())) {
            if (boardRepository.findByHandle(handle) != null) {
                throw new ApiException(ExceptionCode.DUPLICATE_BOARD_HANDLE);
            }
    
            resourceService.updateHandle(board, handle);
        }
        
        List<String> postCategories = boardSettingsDTO.getPostCategories();
        resourceService.updateCategories(board, postCategories, CategoryType.POST);
        board.setDefaultPostVisibility(boardSettingsDTO.getDefaultPostVisibility());
    }
    
}
