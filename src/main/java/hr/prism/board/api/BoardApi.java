package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Scope;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController(value = "/api")
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
    public List<BoardRepresentation> getBoards(@RequestParam(required = false) Boolean includePublicBoards) {
        return boardService.getBoards(null, includePublicBoards).stream().map(board -> boardMapper.apply(board)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "departments/{departmentId}/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoardsByDepartment(@PathVariable Long departmentId, @RequestParam(required = false) Boolean includePublicBoards) {
        return boardService.getBoards(departmentId, includePublicBoards).stream().map(board -> boardMapper.apply(board)).collect(Collectors.toList());
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
        return resourceService.getResourceOperations(Scope.BOARD, id).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/boards/{id}", method = RequestMethod.PATCH)
    public BoardRepresentation updateBoard(@PathVariable Long id, @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.updateBoard(id, boardDTO));
    }
    
}
