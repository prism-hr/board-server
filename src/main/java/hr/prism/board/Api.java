package hr.prism.board;

import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardWithDepartmentDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class Api {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private BoardMapper boardMapper;

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @RequestMapping(value = {"/register", "/login", "/forgot", "/logout"}, method = RequestMethod.GET)
    public void suppressStormpathMvcViews() {
    }

    @RequestMapping(value = "/postLogout", method = RequestMethod.POST)
    public void postLogout() {
    }

    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    public List<DepartmentDTO> getDepartments() {
        return departmentService.getDepartments();
    }

    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public void postBoard(@RequestBody BoardWithDepartmentDTO boardDTO) {
        boardService.createBoard(boardDTO);
    }

    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardDTO> getBoards() {
        return boardService.getBoards().stream()
                .map(boardMapper)
                .collect(Collectors.toList());
    }
}
