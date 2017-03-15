package hr.prism.board.service;

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
    
    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }
    
    public Board findOne(Long id) {
        return boardRepository.findOne(id);
    }
    
    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        
        String name = boardDTO.getName();
        Board board = boardRepository.findByNameAndDepartment(name, department);
        if (board != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_BOARD);
        }
        
        board = new Board();
        board.setType("BOARD");
        board.setName(name);
        board.setDescription(boardDTO.getPurpose());
        
        BoardSettingsDTO settingsDTO = boardDTO.getSettings();
        if (settingsDTO == null) {
            settingsDTO = new BoardSettingsDTO();
        }
        
        if (boardDTO.getSettings().getDefaultPostVisibility() == null) {
            settingsDTO.setDefaultPostVisibility(PostVisibility.PART_PRIVATE);
        }
        
        updateBoardSettings(board, settingsDTO);
        board = boardRepository.save(board);
        resourceService.createResourceRelation(board, board);
        resourceService.createResourceRelation(department, board);
        userRoleService.createUserRole(board, userService.getCurrentUser(), Role.ADMINISTRATOR);
        return board;
    }
    
    public void updateBoard(BoardDTO boardDTO) {
        Long id = boardDTO.getId();
        Board board = boardRepository.findOne(id);
        
        String newName = boardDTO.getName();
        if (!newName.equals(board.getName())) {
            Department department = departmentService.findByBoard(board);
            if (boardRepository.findByNameAndDepartment(newName, department) != null) {
                throw new ApiException(ExceptionCode.DUPLICATE_BOARD);
            }
        }
        
        board.setName(boardDTO.getName());
        board.setDescription(boardDTO.getPurpose());
    }
    
    public void updateBoardSettings(Long id, BoardSettingsDTO boardSettingsDTO) {
        Board board = boardRepository.findOne(id);
        updateBoardSettings(board, boardSettingsDTO);
    }
    
    public void updateBoardSettings(Board board, BoardSettingsDTO boardSettingsDTO) {
        List<String> postCategories = boardSettingsDTO.getPostCategories();
        if (postCategories != null) {
            board.setCategoryList(postCategories.stream().collect(Collectors.joining("|")));
        }
        board.setDefaultPostVisibility(boardSettingsDTO.getDefaultPostVisibility());
    }
    
    public List<Board> findByDepartment(Department department) {
        return boardRepository.findByDepartment(department);
    }
    
}
