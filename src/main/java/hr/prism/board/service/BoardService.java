package hr.prism.board.service;

import hr.prism.board.dao.BoardsDAO;
import hr.prism.board.dao.EntityDAO;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardWithDepartmentDTO;
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

    public Board createBoard(BoardWithDepartmentDTO boardWithDepartmentDTO) {
        Department department = departmentService.createDepartment(boardWithDepartmentDTO.getDepartment());
        return createBoard(department, boardWithDepartmentDTO.getBoard());
    }

    public Board createBoard(Department department, BoardDTO boardDTO) {
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

}
