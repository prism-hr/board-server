package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
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

import static hr.prism.board.enums.Action.EDIT;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class BoardApi {

    private final BoardService boardService;

    private final BoardMapper boardMapper;

    private final ResourceService resourceService;

    private final ResourceOperationMapper resourceOperationMapper;

    @Inject
    public BoardApi(BoardService boardService, BoardMapper boardMapper, ResourceService resourceService,
                    ResourceOperationMapper resourceOperationMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
        this.resourceService = resourceService;
        this.resourceOperationMapper = resourceOperationMapper;
    }

    @RequestMapping(value = "/api/departments/{departmentId}/boards", method = POST)
    public BoardRepresentation postBoard(@PathVariable Long departmentId, @RequestBody @Valid BoardDTO boardDTO) {
        Board board = boardService.createBoard(departmentId, boardDTO);
        return boardMapper.apply(board);
    }

    @RequestMapping(value = "/api/boards", method = GET)
    public List<BoardRepresentation> getBoards(@RequestParam(required = false) Long parentId,
                                               @RequestParam(required = false) Boolean includePublic,
                                               @RequestParam(required = false) State state,
                                               @RequestParam(required = false) String quarter,
                                               @RequestParam(required = false) String searchTerm) {
        return boardService.getBoards(parentId, includePublic, state, quarter, searchTerm)
            .stream().map(boardMapper).collect(toList());
    }

    @RequestMapping(value = "/api/boards/{boardId}", method = GET)
    public BoardRepresentation getBoard(@PathVariable Long boardId) {
        return boardMapper.apply(boardService.getBoard(boardId));
    }

    @RequestMapping(value = "/api/boards", method = GET, params = "handle")
    public BoardRepresentation getBoardByHandle(@RequestParam String handle) {
        return boardMapper.apply(boardService.getBoard(handle));
    }

    @RequestMapping(value = "/api/boards/{boardId}/operations", method = GET)
    public List<ResourceOperationRepresentation> getBoardOperations(@PathVariable Long boardId) {
        return resourceService.getResourceOperations(Scope.BOARD, boardId)
            .stream().map(resourceOperationMapper).collect(toList());
    }

    @RequestMapping(value = "/api/boards/{boardId}", method = PATCH)
    public BoardRepresentation patchBoard(@PathVariable Long boardId, @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(boardId, EDIT, boardDTO));
    }

    @RequestMapping(value = "/api/boards/{boardId}/actions/{action:reject|restore}", method = POST)
    public BoardRepresentation executeAction(@PathVariable Long boardId, @PathVariable String action,
                                             @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(boardId, Action.valueOf(action.toUpperCase()), boardDTO));
    }

}
