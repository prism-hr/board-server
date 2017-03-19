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
import hr.prism.board.mapper.DepartmentMapperFactory;
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
import java.util.stream.StreamSupport;

@RestController
public class DepartmentBoardApi {
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Inject
    private DepartmentMapperFactory departmentMapperFactory;
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments() {
        return StreamSupport.stream(departmentService.findAllByOrderByName().spliterator(), false)
            .map(departmentMapperFactory.create())
            .collect(Collectors.toList());
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/departments/{departmentId}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable("departmentId") Long departmentId) {
        return departmentMapperFactory.create(ImmutableSet.of("boards")).apply(departmentService.findOne(departmentId));
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/departments/byHandle/{departmentHandle}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartmentByHandle(@PathVariable("departmentHandle") String handle) {
        return departmentMapperFactory.create(ImmutableSet.of("boards")).apply(departmentService.findByHandle(handle));
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/departments/{departmentId}", method = RequestMethod.PUT)
    public void updateDepartment(@PathVariable("departmentId") Long departmentId, @RequestBody DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(departmentId, departmentDTO);
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public BoardRepresentation postBoard(@RequestBody BoardDTO boardDTO) {
        Board board = boardService.createBoard(boardDTO);
        return boardMapper.apply(board);
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards() {
        return StreamSupport.stream(boardService.findAllByOrderByName().spliterator(), false)
            .map(boardMapper)
            .collect(Collectors.toList());
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards/{boardId}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable("boardId") Long boardId) {
        return boardMapper.apply(boardService.findOne(boardId));
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards/byHandle/{departmentHandle}/{boardHandle}", method = RequestMethod.GET)
    public BoardRepresentation getBoardByHandle(@PathVariable("departmentHandle") String departmentHandle, @PathVariable("boardHandle") String boardHandle) {
        return boardMapper.apply(boardService.findByHandleAndDepartmentHandle(boardHandle, departmentHandle));
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards/{boardId}", method = RequestMethod.PUT)
    public void updateBoard(@PathVariable("boardId") Long boardId, @RequestBody BoardDTO boardDTO) {
        boardService.updateBoard(boardId, boardDTO);
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/boards/{boardId}/settings", method = RequestMethod.PUT)
    public void updateBoardSettings(@PathVariable("boardId") Long boardId, @RequestBody BoardSettingsDTO boardSettingsDTO) {
        boardService.updateBoardSettings(boardId, boardSettingsDTO);
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
