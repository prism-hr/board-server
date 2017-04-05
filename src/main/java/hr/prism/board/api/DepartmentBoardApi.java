package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DepartmentBoardApi {
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Inject
    private DepartmentMapper departmentMapper;
    
    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments() {
        return departmentService.getDepartments().stream().map(departmentMapper.create()).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long id) {
        return departmentMapper.create(ImmutableSet.of("boards", "roles")).apply(departmentService.getDepartment(id));
    }
    
    @RequestMapping(value = "/departments", method = RequestMethod.GET, params = "handle")
    public DepartmentRepresentation getDepartmentByHandle(@RequestParam String handle) {
        return departmentMapper.create(ImmutableSet.of("boards")).apply(departmentService.getDepartment(handle));
    }
    
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.PUT)
    public void updateDepartment(@PathVariable Long id, @RequestBody @Valid DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(id, departmentDTO);
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public BoardRepresentation postBoard(@RequestBody @Valid BoardDTO boardDTO) {
        Board board = boardService.createBoard(boardDTO);
        return boardMapper.create(ImmutableSet.of("actions")).apply(board);
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards() {
        return boardService.getBoards().stream().map(board -> boardMapper.create().apply(board)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.create(ImmutableSet.of("actions")).apply(boardService.getBoard(id));
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.GET, params = "handle")
    public BoardRepresentation getBoardByHandle(@RequestParam String handle) {
        return boardMapper.create(ImmutableSet.of("actions")).apply(boardService.getBoard(handle));
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.PUT)
    public void updateBoard(@PathVariable Long id, @RequestBody @Valid BoardDTO boardDTO) {
        boardService.updateBoard(id, boardDTO);
    }
    
    @RequestMapping(value = "/boards/{id}/settings", method = RequestMethod.PUT)
    public void updateBoardSettings(@PathVariable Long id, @RequestBody @Valid BoardSettingsDTO boardSettingsDTO) {
        boardService.updateBoardSettings(id, boardSettingsDTO);
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
