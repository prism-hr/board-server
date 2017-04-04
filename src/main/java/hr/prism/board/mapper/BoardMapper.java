package hr.prism.board.mapper;

import hr.prism.board.domain.*;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardMapper {
    
    @Inject
    private DepartmentMapper departmentMapper;
    
    @Inject
    private ActionService actionService;
    
    @Inject
    private UserService userService;
    
    public Function<Board, BoardRepresentation> create() {
        return create(new HashSet<>());
    }
    
    public Function<Board, BoardRepresentation> create(Set<String> options) {
        User user = userService.getCurrentUser();
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
    
            if (options.contains("actions")) {
                boardRepresentation.setActions(actionService.getActions(board, user));
            }
    
            return boardRepresentation;
        };
    }
    
}
