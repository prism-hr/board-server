package hr.prism.board.mapper;

import hr.prism.board.domain.ActionService;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
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
    
    @Inject
    private ActionService actionService;
    
    public Function<Department, DepartmentRepresentation> create() {
        return create(new HashSet<>());
    }
    
    public Function<Department, DepartmentRepresentation> create(Set<String> options) {
        return (Department department) -> {
            DepartmentRepresentation departmentRepresentation = new DepartmentRepresentation();
            departmentRepresentation
                .setId(department.getId())
                .setName(department.getName())
                .setState(department.getState());
            departmentRepresentation
                .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
                .setHandle(department.getHandle())
                .setMemberCategories(department.getCategories().stream().filter(ResourceCategory::isActive).map(ResourceCategory::getName).collect(Collectors.toList()));
    
            if (options.contains("boards")) {
                departmentRepresentation.setBoards(boardService.findByDepartment(department).stream().map(board -> boardMapper.create().apply(board)).collect(Collectors.toList()));
            }
    
            if (options.contains("actions")) {
                departmentRepresentation.setActions(actionService.getActions(department));
            }
    
            return departmentRepresentation;
        };
    }
    
}
