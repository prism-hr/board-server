package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.authentication.Restriction;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
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
        return departmentService.findAllByUserOrderByName().stream().map(departmentMapper.create()).collect(Collectors.toList());
    }

    @RequestMapping(value = "/departments/{id}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable("id") Long id) {
        return departmentMapper.create(ImmutableSet.of("boards")).apply(departmentService.findOne(id));
    }

    @RequestMapping(value = "/departments", method = RequestMethod.GET, params = "handle")
    public DepartmentRepresentation getDepartmentByHandle(@RequestParam("handle") String handle) {
        return departmentMapper.create(ImmutableSet.of("boards")).apply(departmentService.findByHandle(handle));
    }

    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.PUT)
    public void updateDepartment(@PathVariable("id") Long id, @RequestBody DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(id, departmentDTO);
    }

    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public BoardRepresentation postBoard(@RequestBody BoardDTO boardDTO) {
        Board board = boardService.createBoard(boardDTO);
        return boardMapper.apply(board);
    }

    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards() {
        return boardService.findAllByUserOrderByName().stream().map(boardMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable("id") Long id) {
        return boardMapper.apply(boardService.findOne(id));
    }

    @RequestMapping(value = "/boards", method = RequestMethod.GET, params = "handle")
    public BoardRepresentation getBoardByHandle(@RequestParam("handle") String handle) {
        return boardMapper.apply(boardService.findByHandle(handle));
    }

    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.PUT)
    public void updateBoard(@PathVariable("id") Long id, @RequestBody BoardDTO boardDTO) {
        boardService.updateBoard(id, boardDTO);
    }

    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards/{id}/settings", method = RequestMethod.PUT)
    public void updateBoardSettings(@PathVariable("id") Long id, @RequestBody BoardSettingsDTO boardSettingsDTO) {
        boardService.updateBoardSettings(id, boardSettingsDTO);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }

}
