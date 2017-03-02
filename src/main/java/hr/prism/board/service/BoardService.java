package hr.prism.board.service;

import hr.prism.board.dao.BoardsDAO;
import hr.prism.board.dao.EntityDAO;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
public class BoardService {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserService userService;

    @Inject
    private EntityDAO entityDAO;

    @Inject
    private BoardsDAO boardDAO;

    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        Board board = new Board();
        board.setName(boardDTO.getName());
        board.setPurpose(boardDTO.getPurpose());
        board.setDepartment(department);
        board.setUser(userService.getCurrentUser());
        entityDAO.save(board);
        return board;
    }

    public List<Board> getBoards() {
        return boardDAO.getBoards();
    }

    public Board getBoard(Long id) {
        return entityDAO.getById(Board.class, id);
    }

}
