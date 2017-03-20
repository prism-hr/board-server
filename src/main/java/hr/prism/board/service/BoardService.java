package hr.prism.board.service;

import com.google.common.base.Joiner;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.UserRoleService;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
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
    private DepartmentService departmentService;

    @Inject
    private BoardRepository boardRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;
    
    // TODO: make it a query with the user roles
    public Iterable<Board> findAllByOrderByName() {
        return boardRepository.findAllByOrderByName();
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
        board.setName(name);
        board.setDescription(boardDTO.getPurpose());

        BoardSettingsDTO settingsDTO = boardDTO.getSettings();
        if (settingsDTO == null) {
            settingsDTO = new BoardSettingsDTO();
        }

        if (boardDTO.getSettings().getDefaultPostVisibility() == null) {
            settingsDTO.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
        }

        updateBoardSettings(board, settingsDTO, department);
        board = boardRepository.save(board);
        resourceService.createResourceRelation(board, board);
        resourceService.createResourceRelation(department, board);
        userRoleService.createUserRole(board, userService.getCurrentUser(), Role.ADMINISTRATOR);
        return board;
    }
    
    public void updateBoard(Long boardId, BoardDTO boardDTO) {
        Board board = boardRepository.findOne(boardId);
    
        String newName = boardDTO.getName();
        if (!newName.equals(board.getName())) {
            Department department = departmentService.findByBoard(board);
            validateNameUniqueness(newName, department);
        }

        board.setName(boardDTO.getName());
        board.setDescription(boardDTO.getPurpose());
    }

    public void updateBoardSettings(Long id, BoardSettingsDTO boardSettingsDTO) {
        Board board = boardRepository.findOne(id);
        Department department = departmentService.findByBoard(board);
        updateBoardSettings(board, boardSettingsDTO, department);
    }
    
    public Iterable<Board> findByDepartment(Department department) {
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
        if (postCategories != null) {
            board.setCategoryList(postCategories.stream().collect(Collectors.joining("|")));
        }
    
        board.setDefaultPostVisibility(boardSettingsDTO.getDefaultPostVisibility());
    }

}
