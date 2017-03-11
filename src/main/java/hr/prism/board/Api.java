package hr.prism.board;

import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import org.springframework.core.env.Environment;
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
    
    @Inject
    private Environment environment;
    
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
    
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long id) {
        return departmentMapper.apply(departmentService.getDepartment(id));
    }
    
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.PUT)
    public void updateDepartment(@RequestBody DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(departmentDTO);
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public BoardRepresentation postBoard(@RequestBody BoardDTO boardDTO) {
        Board board = boardService.createBoard(boardDTO);
        return boardMapper.apply(board);
    }
    
    @RequestMapping(value = "/groupedBoards", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getBoardsGroupedByDepartment() {
        return boardService.getBoardsGroupedByDepartment();
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.apply(boardService.getBoard(id));
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.PUT)
    public void updateBoard(@RequestBody BoardDTO boardDTO) {
        boardService.updateBoard(boardDTO);
    }
    
    @RequestMapping(value = "/boards/{id}/settings", method = RequestMethod.PUT)
    public void updateBoardSettings(@PathVariable Long id, @RequestBody BoardSettingsDTO boardSettingsDTO) {
        boardService.updateBoardSettings(id, boardSettingsDTO);
    }
    
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String getApplicationProfile() {
        return environment.getProperty("id");
    }
}
