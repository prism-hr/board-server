package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.representation.BoardRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;

@Component
public class BoardMapper implements Function<Board, BoardRepresentation> {

    private final DepartmentMapper departmentMapper;

    private final ResourceMapper resourceMapper;

    @Inject
    public BoardMapper(DepartmentMapper departmentMapper, ResourceMapper resourceMapper) {
        this.departmentMapper = departmentMapper;
        this.resourceMapper = resourceMapper;
    }

    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.apply(board, BoardRepresentation.class)
            .setDepartment(departmentMapper.applySmall(department))
            .setHandle(resourceMapper.getHandle(board, department))
            .setPostCategories(board.getPostCategoryStrings());
    }

    BoardRepresentation applySmall(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.applySmall(board, BoardRepresentation.class)
            .setDepartment(departmentMapper.applySmall((Department) board.getParent()))
            .setHandle(resourceMapper.getHandle(board, department));
    }

    BoardRepresentation applyMedium(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.applySmall(board, BoardRepresentation.class)
            .setDepartment(departmentMapper.applyMedium((Department) board.getParent()))
            .setHandle(resourceMapper.getHandle(board, department))
            .setPostCategories(board.getPostCategoryStrings());
    }

}
