package hr.prism.board.mapper;

import hr.prism.board.domain.Category;
import hr.prism.board.domain.Department;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentMapper {

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private BoardService boardService;

    public Function<Department, DepartmentRepresentation> create() {
        return create(new HashSet<>());
    }

    // TODO: refactor to make sure we never actually get boards (SQL) for each department
    public Function<Department, DepartmentRepresentation> create(Set<String> options) {
        return (Department department) -> {
            DepartmentRepresentation departmentRepresentation = new DepartmentRepresentation()
                .setId(department.getId())
                .setName(department.getName())
                .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
                .setHandle(department.getHandle())
                .setMemberCategories(department.getCategories().stream().filter(Category::isActive).map(Category::getName).collect(Collectors.toList()));
            if (options.contains("boards")) {
                departmentRepresentation.setBoards(boardService.findByDepartment(department).stream().map(boardMapper).collect(Collectors.toList()));
            }

            return departmentRepresentation;
        };
    }

}
