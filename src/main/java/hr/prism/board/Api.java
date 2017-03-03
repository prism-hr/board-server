package hr.prism.board;

import hr.prism.board.dto.BoardDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class Api {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private DepartmentMapper departmentMapper;

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @RequestMapping(value = {"/register", "/login", "/forgot", "/logout"}, method = RequestMethod.GET)
    public void suppressStormpathMvcViews() {
    }

    @RequestMapping(value = "/postLogout", method = RequestMethod.POST)
    public void postLogout() {
    }

    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments() {
        return StreamSupport.stream(departmentService.getDepartments().spliterator(), false)
                .map(departmentMapper)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public void postBoard(@RequestBody BoardDTO boardDTO) {
        boardService.createBoard(boardDTO);
    }

    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards() {
        return StreamSupport.stream(boardService.getBoards().spliterator(), false)
                .map(boardMapper)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.apply(boardService.getBoard(id));
    }
}
