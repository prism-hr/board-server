package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.BoardRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BoardMapper implements Function<Board, BoardRepresentation> {
    
    @Inject
    private DepartmentMapper departmentMapper;
    
    @Inject
    private ResourceMapper resourceMapper;
    
    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }
    
        Department department = (Department) board.getParent();
        return resourceMapper.apply(board, BoardRepresentation.class)
            .setDescription(board.getDescription())
            .setHandle(board.getHandle().replaceFirst(department.getHandle() + "/", ""))
            .setPostCategories(board.getPostCategories().stream().map(ResourceCategory::getName).collect(Collectors.toList()))
            .setDepartment(departmentMapper.apply(department))
            .setDefaultPostVisibility(board.getDefaultPostVisibility());
    }
    
}
