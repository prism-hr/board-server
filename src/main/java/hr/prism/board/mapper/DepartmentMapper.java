package hr.prism.board.mapper;

import hr.prism.board.domain.*;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
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
public class DepartmentMapper {

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private BoardService boardService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserService userService;

    public Function<Department, DepartmentRepresentation> create() {
        return create(new HashSet<>());
    }

    // TODO: refactor to make sure we never actually get boards (SQL) for each department
    public Function<Department, DepartmentRepresentation> create(Set<String> options) {
        User user = userService.getCurrentUser();
        return (Department department) -> {
            DepartmentRepresentation departmentRepresentation = new DepartmentRepresentation();
            departmentRepresentation
                .setId(department.getId())
                .setName(department.getName())
                .setState(department.getState());
            departmentRepresentation
                .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
                .setHandle(department.getHandle())
                .setMemberCategories(department.getCategories().stream().filter(Category::isActive).map(Category::getName).collect(Collectors.toList()));

            if (options.contains("boards")) {
                departmentRepresentation.setBoards(boardService.findByDepartment(department).stream().map(board -> boardMapper.create().apply(board)).collect(Collectors.toList()));
            }

            if (options.contains("roles")) {
                departmentRepresentation.setRoles(userRoleService.findByResourceAndUser(department, user));
                departmentRepresentation.setActions(actionService.getActions(department, user));
            }

            return departmentRepresentation;
        };
    }

}
