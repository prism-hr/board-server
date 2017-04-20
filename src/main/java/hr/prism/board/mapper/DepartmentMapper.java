package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.DepartmentRepresentation;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DepartmentMapper implements Function<Department, DepartmentRepresentation> {

    @Inject
    private DocumentMapper documentMapper;

    @Override
    public DepartmentRepresentation apply(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentRepresentation departmentRepresentation = new DepartmentRepresentation();
        departmentRepresentation
            .setId(department.getId())
            .setScope(department.getScope())
            .setName(department.getName())
            .setState(department.getState());
        departmentRepresentation
            .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
            .setHandle(department.getHandle())
            .setMemberCategories(department.getCategories().stream()
                .filter(resourceCategory -> BooleanUtils.isTrue(resourceCategory.getActive())).map(ResourceCategory::getName).collect(Collectors.toList()));

        departmentRepresentation.setActions(department.getActions());
        return departmentRepresentation;
    }

}
