package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Board;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.WidgetOptionsDTO;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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
    public List<BoardRepresentation> getBoards(@RequestParam(required = false) Boolean includePublic, @RequestParam(required = false) State state,
                                               @RequestParam(required = false) String quarter, @RequestParam(required = false) String searchTerm) {
        return boardService.getBoards(null, includePublic, state, quarter, searchTerm).stream().map(boardMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}/boards", method = RequestMethod.GET)
    public List<BoardRepresentation> getBoardsByDepartment(@PathVariable Long departmentId, @RequestParam(required = false) Boolean includePublic,
                                                           @RequestParam(required = false) State state, @RequestParam(required = false) String quarter,
                                                           @RequestParam(required = false) String searchTerm) {
        return boardService.getBoards(departmentId, includePublic, state, quarter, searchTerm).stream().map(boardMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/boards/{id}", method = RequestMethod.GET)
    public BoardRepresentation getBoard(@PathVariable Long id) {
        return boardMapper.apply(boardService.getBoard(id));
    }

    @RequestMapping(value = "/api/boards/{id}/badge", method = RequestMethod.GET)
    public String getBoardBadge(@PathVariable Long id, @RequestParam String options, HttpServletResponse response) throws IOException {
        response.setHeader("X-Frame-Options", "ALLOW");
        ObjectMapper objectMapper = new ObjectMapper();
        WidgetOptionsDTO widgetOptions = objectMapper.readValue(options, new TypeReference<WidgetOptionsDTO>() {
        });
        return boardService.getBoardBadge(boardService.getBoard(id), widgetOptions);
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
    public BoardRepresentation patchBoard(@PathVariable Long id, @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(id, Action.EDIT, boardDTO));
    }

    @RequestMapping(value = "/api/boards/{id}/actions/{action:accept|reject}", method = RequestMethod.POST)
    public BoardRepresentation executeAction(@PathVariable Long id, @PathVariable String action, @RequestBody @Valid BoardPatchDTO boardDTO) {
        return boardMapper.apply(boardService.executeAction(id, Action.exchangeAndValidate(action, boardDTO), boardDTO));
    }

}
