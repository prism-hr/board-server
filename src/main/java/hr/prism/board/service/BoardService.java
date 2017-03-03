package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class BoardService {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserService userService;

    @Inject
    private BoardRepository boardRepository;

    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        Board board = new Board();
        board.setName(boardDTO.getName());
        board.setPurpose(boardDTO.getPurpose());
        board.setDepartment(department);
        board.setUser(userService.getCurrentUser());
        return boardRepository.save(board);
    }

    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }

    public Board getBoard(Long id) {
        return boardRepository.findOne(id);
    }

}
