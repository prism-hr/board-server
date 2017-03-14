package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.UserRoleService;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
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
    
    public Board getBoard(Long id) {
        return boardRepository.findOne(id);
    }
    
    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        Board board = boardRepository.findByNameAndDepartment(boardDTO.getName(), department);
        if (board != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_BOARD);
        }
        
        board = new Board();
        board.setName(boardDTO.getName());
        board.setDescription(boardDTO.getPurpose());
        updateBoardSettings(board, boardDTO.getSettings());
        
        board = boardRepository.save(board);
        resourceService.createResourceRelation(board, board);
        resourceService.createResourceRelation(department, board);
        userRoleService.createUserRole(board, userService.getCurrentUser(), Role.ADMINISTRATOR);
        return board;
    }
    
    public void updateBoard(BoardDTO boardDTO) {
        Board board = boardRepository.findOne(boardDTO.getId());
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
    }
    
    public List<Board> findByDepartment(Department department) {
        return boardRepository.findByDepartment(department);
    }
    
}
