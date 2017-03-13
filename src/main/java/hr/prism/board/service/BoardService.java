package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.DepartmentMapperFactory;
import hr.prism.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardService {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserService userService;

    @Inject
    private BoardRepository boardRepository;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private DepartmentMapperFactory departmentMapperFactory;

    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }

    public Board getBoard(Long id) {
        return boardRepository.findOne(id);
    }

    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        Board board = new Board();
        board.setName(boardDTO.getName());
        board.setPurpose(boardDTO.getPurpose());
        board.setPostCategories("");
        board.setDepartment(department);
        board.setUser(userService.getCurrentUser());
        updateBoardSettings(board, boardDTO.getSettings());
        return boardRepository.save(board);
    }

    public void updateBoard(BoardDTO boardDTO) {
        Board board = boardRepository.findOne(boardDTO.getId());
        board.setName(boardDTO.getName());
        board.setPurpose(boardDTO.getPurpose());
    }

    public void updateBoardSettings(Long id, BoardSettingsDTO boardSettingsDTO) {
        Board board = boardRepository.findOne(id);
        updateBoardSettings(board, boardSettingsDTO);
    }

    public void updateBoardSettings(Board board, BoardSettingsDTO boardSettingsDTO) {
        board.setPostCategories(boardSettingsDTO.getPostCategories().stream().collect(Collectors.joining("|")));
    }
}
