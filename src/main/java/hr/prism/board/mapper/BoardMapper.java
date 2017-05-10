package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
public class BoardMapper implements Function<Board, BoardRepresentation> {

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DepartmentService departmentService;

    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }

        Department department = departmentService.getDepartment(board.getParent().getId());
        return resourceMapper.apply(board, BoardRepresentation.class)
            .setSummary(board.getSummary())
            .setHandle(board.getHandle().replaceFirst(department.getHandle() + "/", ""))
            .setPostCategories(resourceService.getCategories(board, CategoryType.POST))
            .setDepartment(departmentMapper.apply(department))
            .setDefaultPostVisibility(board.getDefaultPostVisibility());
    }

}
