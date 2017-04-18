package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BoardApi {
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private ResourceOperationMapper resourceOperationMapper;
    
    @RequestMapping(value = "/boards", method = RequestMethod.POST)
    public BoardRepresentation postBoard(@RequestBody @Valid BoardDTO boardDTO) {
        Board board = boardService.createBoard(boardDTO);
        return boardMapper.apply(board);
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards() {
        return boardService.getBoards(null).stream().map(board -> boardMapper.apply(board)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "departments/{departmentId}/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoardsByDepartment(@PathVariable Long departmentId) {
        return boardService.getBoards(departmentId).stream().map(board -> boardMapper.apply(board)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.apply(boardService.getBoard(id));
    }
    
    @RequestMapping(value = "/boards", method = RequestMethod.GET, params = "handle")
    public BoardRepresentation getBoardByHandle(@RequestParam String handle) {
        return boardMapper.apply(boardService.getBoard(handle));
    }
    
    @RequestMapping(value = "/boards/{id}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getBoardOperations(@PathVariable Long id) {
        return resourceService.getResourceOperations(id).stream().map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.PATCH)
    public void updateBoard(@PathVariable Long id, @RequestBody @Valid BoardPatchDTO boardDTO) {
        boardService.updateBoard(id, boardDTO);
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
