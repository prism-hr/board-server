package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.DepartmentRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DepartmentMapper implements Function<Department, DepartmentRepresentation> {
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Inject
    private ResourceMapper resourceMapper;
    
    @Override
    public DepartmentRepresentation apply(Department department) {
        if (department == null) {
            return null;
        }
    
        return resourceMapper.apply(department, DepartmentRepresentation.class)
            .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
            .setHandle(department.getHandle())
            .setMemberCategories(department.getMemberCategories().stream().map(ResourceCategory::getName).collect(Collectors.toList()));
    }
    
}
