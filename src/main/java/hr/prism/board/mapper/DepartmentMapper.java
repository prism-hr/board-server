package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.representation.DepartmentRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;

@Service
@Transactional
public class DepartmentMapper implements Function<Department, DepartmentRepresentation> {
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Override
    public DepartmentRepresentation apply(Department department) {
        return new DepartmentRepresentation()
                .setId(department.getId())
                .setName(department.getName())
                .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()));
    }
}
