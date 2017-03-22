package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Category;
import hr.prism.board.domain.Department;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardMapper implements Function<Board, BoardRepresentation> {

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private DepartmentService departmentService;

    @Override
    // TODO: refactor, we are using it get department (SQL) for each board in a list
    public BoardRepresentation apply(Board board) {
        Department department = departmentService.findByBoard(board);
        return new BoardRepresentation()
            .setId(board.getId())
            .setName(board.getName())
            .setPurpose(board.getDescription())
            .setHandle(board.getHandle().replaceFirst(department.getHandle() + "/", ""))
            .setPostCategories(board.getCategories().stream().filter(Category::isActive).map(Category::getName).collect(Collectors.toList()))
            .setDepartment(departmentMapper.create().apply(department))
            .setDefaultPostVisibility(board.getDefaultPostVisibility());
    }

}
