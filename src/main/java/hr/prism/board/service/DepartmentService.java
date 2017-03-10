package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Document;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @Inject
    private DepartmentRepository departmentRepository;

    public Iterable<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartment(Long id) {
        return departmentRepository.findOne(id);
    }

    public Department getOrCreateDepartment(DepartmentDTO departmentDTO) {
        if (departmentDTO.getId() != null) {
            return departmentRepository.findOne(departmentDTO.getId());
        }

        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setUser(userService.getCurrentUser());
        if (departmentDTO.getDocumentLogo() != null) {
            department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
        }
        department.setMemberCategories("");
        return departmentRepository.save(department);
    }

    public void updateDepartment(DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findOne(departmentDTO.getId());
        department.setName(departmentDTO.getName());
        String existingLogoId = Optional.of(department.getDocumentLogo()).map(Document::getCloudinaryId).orElse(null);
        String newLogoId = Optional.of(departmentDTO.getDocumentLogo()).map(DocumentDTO::getCloudinaryId).orElse(null);
        if (!Objects.equals(existingLogoId, newLogoId)) {
            department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
        }
    }
}
