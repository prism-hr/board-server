package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class DepartmentService {

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @Inject
    private DepartmentRepository departmentRepository;

    public Department getOrCreateDepartment(DepartmentDTO departmentDTO) {
        if (departmentDTO.getId() != null) {
            return departmentRepository.findOne(departmentDTO.getId());
        }

        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setUser(userService.getCurrentUser());
        if (departmentDTO.getDocumentLogo() != null) {
            department.setDocumentLogo(documentService.saveDocument(departmentDTO.getDocumentLogo()));
        }
        return departmentRepository.save(department);
    }

    public Iterable<Department> getDepartments() {
        return departmentRepository.findAll();
    }

}
