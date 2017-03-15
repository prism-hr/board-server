package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
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
    
    public Department findOne(Long id) {
        return departmentRepository.findOne(id);
    }
    
    public Department findByBoard(Board board) {
        return departmentRepository.findByBoard(board);
    }
    
    public Department getOrCreateDepartment(DepartmentDTO departmentDTO) {
        Long id = departmentDTO.getId();
        String name = departmentDTO.getName();
        
        Department departmentById = null;
        Department departmentByName = null;
        for (Department department : departmentRepository.findByIdOrName(departmentDTO.getId(), name)) {
            if (department.getId().equals(id)) {
                departmentById = department;
            }
    
            if (department.getName().equals(name)) {
                departmentByName = department;
            }
        }
        
        if (departmentById == null && departmentByName == null) {
            Department department = new Department();
            department.setType("DEPARTMENT");
            department.setName(departmentDTO.getName());
            if (departmentDTO.getDocumentLogo() != null) {
                department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
            }
            
            List<String> memberCategories = departmentDTO.getMemberCategories();
            department.setCategoryList(memberCategories.stream().collect(Collectors.joining("|")));
            
            department = departmentRepository.save(department);
            resourceService.createResourceRelation(department, department);
            userRoleService.createUserRole(department, userService.getCurrentUser(), Role.ADMINISTRATOR);
            return department;
        }
        
        return departmentByName == null ? departmentById : departmentByName;
    }
    
    public void updateDepartment(DepartmentDTO departmentDTO) {
        Long id = departmentDTO.getId();
        Department department = departmentRepository.findOne(id);
        
        String newName = departmentDTO.getName();
        if (!newName.equals(department.getName()) && departmentRepository.findByName(newName) != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_DEPARTMENT);
        }
        
        department.setName(newName);
        String existingLogoId = Optional.ofNullable(department.getDocumentLogo()).map(Document::getCloudinaryId).orElse(null);
        String newLogoId = Optional.ofNullable(departmentDTO.getDocumentLogo()).map(DocumentDTO::getCloudinaryId).orElse(null);
        if (!Objects.equals(existingLogoId, newLogoId)) {
            department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
        }
        
        department.setCategoryList(departmentDTO.getMemberCategories().stream().collect(Collectors.joining("|")));
    }
    
}
