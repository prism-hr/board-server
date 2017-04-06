package hr.prism.board.mapper;

import hr.prism.board.domain.ActionService;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.BoardRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardMapper {
    
    @Inject
    private DepartmentMapper departmentMapper;
    
    @Inject
    private ActionService actionService;
    
    public Function<Board, BoardRepresentation> create() {
        return (Board board) -> {
            Department department = (Department) board.getParent();
            BoardRepresentation boardRepresentation = new BoardRepresentation();
            boardRepresentation
                .setId(board.getId())
                .setName(board.getName())
                .setState(board.getState());
            boardRepresentation
                .setPurpose(board.getDescription())
                .setHandle(board.getHandle().replaceFirst(department.getHandle() + "/", ""))
                .setPostCategories(board.getCategories().stream().filter(ResourceCategory::isActive).map(ResourceCategory::getName).collect(Collectors.toList()))
                .setDepartment(departmentMapper.create().apply(department))
                .setDefaultPostVisibility(board.getDefaultPostVisibility());
    
            boardRepresentation.setActions(actionService.getActions(board));
            return boardRepresentation;
        };
    }
    
}
