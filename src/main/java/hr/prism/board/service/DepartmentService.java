package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {
    
    @Inject
    private UserService userService;
    
    @Inject
    private DocumentService documentService;
    
    @Inject
    private DepartmentRepository departmentRepository;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserRoleService userRoleService;
    
    public Iterable<Department> getDepartments() {
        return departmentRepository.findAll();
    }
    
    public Department getDepartment(Long id) {
        return departmentRepository.findOne(id);
    }
    
    public Department getOrCreateDepartment(DepartmentDTO departmentDTO) {
        String name = departmentDTO.getName();
        Department department = departmentRepository.findByIdOrName(departmentDTO.getId(), name);
        if (department != null) {
            return department;
        }
        
        department = new Department();
        department.setName(departmentDTO.getName());
        if (departmentDTO.getDocumentLogo() != null) {
            department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
        }
        
        List<String> memberCategories = departmentDTO.getMemberCategories();
        if (memberCategories != null) {
            department.setCategoryList(memberCategories.stream().collect(Collectors.joining("|")));
        }
        
        department = departmentRepository.save(department);
        resourceService.createResourceRelation(department, department);
        userRoleService.createUserRole(department, userService.getCurrentUser(), Role.ADMINISTRATOR);
        return department;
    }
    
    public void updateDepartment(DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findOne(departmentDTO.getId());
        department.setName(departmentDTO.getName());
        String existingLogoId = Optional.ofNullable(department.getDocumentLogo()).map(Document::getCloudinaryId).orElse(null);
        String newLogoId = Optional.ofNullable(departmentDTO.getDocumentLogo()).map(DocumentDTO::getCloudinaryId).orElse(null);
        if (!Objects.equals(existingLogoId, newLogoId)) {
            department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
        }
    }
    
    public Department findByBoard(Board board) {
        return departmentRepository.findByBoard(board);
    }
    
}
