package hr.prism.board.api;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.value.ResourceFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private final ResourceOperationMapper resourceOperationMapper;

    @Inject
    public BoardApi(BoardService boardService, BoardMapper boardMapper,
                    ResourceOperationMapper resourceOperationMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
        this.resourceOperationMapper = resourceOperationMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/boards", method = POST)
    public BoardRepresentation createBoard(@AuthenticationPrincipal User user, @PathVariable Long departmentId,
                                           @RequestBody @Valid BoardDTO boardDTO) {
        Board board = boardService.createBoard(user, departmentId, boardDTO);
        return boardMapper.apply(board);
    }

    @RequestMapping(value = "/api/boards", method = GET)
    public List<BoardRepresentation> getBoards(@AuthenticationPrincipal User user,
                                               @ModelAttribute ResourceFilter filter) {
        return boardService.getBoards(user, filter).stream().map(boardMapper).collect(toList());
    }

    @RequestMapping(value = "/api/boards/{boardId}", method = GET)
    public BoardRepresentation getBoard(@AuthenticationPrincipal User user, @PathVariable Long boardId) {
        return boardMapper.apply(boardService.getById(user, boardId));
    }

    @RequestMapping(value = "/api/boards", method = GET, params = "handle")
    public BoardRepresentation getBoardByHandle(@AuthenticationPrincipal User user, @RequestParam String handle) {
        return boardMapper.apply(boardService.getByHandle(user, handle));
    }

    @RequestMapping(value = "/api/boards/{boardId}/operations", method = GET)
    public List<ResourceOperationRepresentation> getBoardOperations(@PathVariable Long boardId) {
        return boardService.getBoardOperations(boardId).stream().map(resourceOperationMapper).collect(toList());
    }

    @RequestMapping(value = "/api/boards/{boardId}", method = PATCH)
    public BoardRepresentation updateBoard(@PathVariable Long boardId, @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(boardId, EDIT, boardDTO));
    }

    @RequestMapping(value = "/api/boards/{boardId}/actions/{action:reject|restore}", method = POST)
    public BoardRepresentation performActionOnBoard(@PathVariable Long boardId, @PathVariable String action,
                                                    @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(boardId, Action.valueOf(action.toUpperCase()), boardDTO));
    }

}
