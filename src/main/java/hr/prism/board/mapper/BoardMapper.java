package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class BoardMapper implements Function<Board, BoardRepresentation> {

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private ResourceService resourceService;

    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.apply(board, BoardRepresentation.class)
            .setHandle(getHandle(board, department))
            .setDepartment(departmentMapper.applySmall((Department) board.getParent()))
            .setPostCategories(resourceService.getCategories(board, CategoryType.POST));
    }

    BoardRepresentation applySmall(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.applySmall(board, BoardRepresentation.class)
            .setHandle(getHandle(board, department))
            .setDepartment(departmentMapper.applySmall((Department) board.getParent()));
    }

    private String getHandle(Board board, Department department) {
        return board.getHandle().replaceFirst(department.getHandle() + "/", "");
    }

}
