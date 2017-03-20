package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.domain.Board;
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

    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments() {
        return StreamSupport.stream(departmentService.findAllByOrderByName().spliterator(), false)
            .map(departmentMapperFactory.create())
            .collect(Collectors.toList());
    }

    @RequestMapping(value = "/departments/{id}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long id) {
        return departmentMapperFactory.create(ImmutableSet.of("boards")).apply(departmentService.findOne(id));
    }

    @RequestMapping(value = "/departments/byHandle/{handle}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartmentByHandle(@PathVariable String handle) {
        return departmentMapperFactory.create(ImmutableSet.of("boards")).apply(departmentService.findByHandle(handle));
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

    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards() {
        return StreamSupport.stream(boardService.findAllByOrderByName().spliterator(), false)
            .map(boardMapper)
            .collect(Collectors.toList());
    }

    @RequestMapping(value = "/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.apply(boardService.findOne(id));
    }

    @RequestMapping(value = "/boards/byHandle/{departmentHandle}/{boardHandle}", method = RequestMethod.GET)
    public BoardRepresentation getBoardByHandle(@PathVariable String departmentHandle, String handle) {
        return boardMapper.apply(boardService.findByHandleAndDepartmentHandle(handle, departmentHandle));
    }

    @RequestMapping(value = "/boards/{id}", method = RequestMethod.PUT)
    public void updateBoard(@PathVariable Long id, @RequestBody BoardDTO boardDTO) {
        boardService.updateBoard(id, boardDTO);
    }

    @RequestMapping(value = "/boards/{id}/settings", method = RequestMethod.PUT)
    public void updateBoardSettings(@PathVariable Long id, @RequestBody BoardSettingsDTO boardSettingsDTO) {
        boardService.updateBoardSettings(id, boardSettingsDTO);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }

}
