package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.DepartmentRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentMapper {
    
    @Inject
    private DocumentMapper documentMapper;
    
    public Function<Department, DepartmentRepresentation> create() {
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
    
            departmentRepresentation.setActions(department.getActions());
            return departmentRepresentation;
        };
    }
    
}
