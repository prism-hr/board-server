package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;

@Service
@Transactional
public class DepartmentMapper implements Function<Department, DepartmentRepresentation> {

    @Inject
    private DocumentService documentService;

    @Override
    public DepartmentRepresentation apply(Department department) {
        return new DepartmentRepresentation()
                .withId(department.getId())
                .withName(department.getName())
                .withDocumentLogo(documentService.mapDocument(department.getDocumentLogo()));
    }
}
