package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
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

    @RequestMapping(value = "/api/boards", method = RequestMethod.POST)
    public BoardRepresentation postBoard(@RequestBody @Valid BoardDTO boardDTO) {
        Board board = boardService.createBoard(boardDTO);
        return boardMapper.apply(board);
    }

    @RequestMapping(value = "/api/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoards(@RequestParam(required = false) Boolean includePublicBoards) {
        return boardService.getBoards(null, includePublicBoards).stream().map(board -> boardMapper.apply(board)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoardsByDepartment(@PathVariable Long departmentId, @RequestParam(required = false) Boolean includePublicBoards) {
        return boardService.getBoards(departmentId, includePublicBoards).stream().map(board -> boardMapper.apply(board)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.apply(boardService.getBoard(id));
    }

    @RequestMapping(value = "/api/boards", method = RequestMethod.GET, params = "handle")
    public BoardRepresentation getBoardByHandle(@RequestParam String handle) {
        return boardMapper.apply(boardService.getBoard(handle));
    }

    @RequestMapping(value = "/api/boards/{id}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getBoardOperations(@PathVariable Long id) {
        return resourceService.getResourceOperations(Scope.BOARD, id).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/boards/{id}", method = RequestMethod.PATCH)
    public BoardRepresentation updateBoard(@PathVariable Long id, @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(id, Action.EDIT, boardDTO));
    }

    @RequestMapping(value = "/api/boards/{id}/{action}", method = RequestMethod.POST)
    public BoardRepresentation executeAction(@PathVariable Long id, @PathVariable String action, @RequestBody @Valid BoardPatchDTO boardDTO) {
        Action actionEnum = Action.valueOf(action.toUpperCase());
        return boardMapper.apply(boardService.executeAction(id, actionEnum, boardDTO));
    }

}
