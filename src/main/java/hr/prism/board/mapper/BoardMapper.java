package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;

import static hr.prism.board.enums.CategoryType.POST;

@Component
public class BoardMapper implements Function<Board, BoardRepresentation> {

    private final DepartmentMapper departmentMapper;

    private final ResourceMapper resourceMapper;

    private final ResourceService resourceService;

    @Inject
    public BoardMapper(DepartmentMapper departmentMapper, ResourceMapper resourceMapper,
                       ResourceService resourceService) {
        this.departmentMapper = departmentMapper;
        this.resourceMapper = resourceMapper;
        this.resourceService = resourceService;
    }

    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.apply(board, BoardRepresentation.class)
            .setHandle(resourceMapper.getHandle(board, department))
            .setDepartment(departmentMapper.applySmall(department))
            .setPostCategories(resourceService.getCategories(board, POST));
    }

    BoardRepresentation applySmall(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.applySmall(board, BoardRepresentation.class)
            .setHandle(resourceMapper.getHandle(board, department))
            .setDepartment(departmentMapper.applySmall((Department) board.getParent()));
    }

}
