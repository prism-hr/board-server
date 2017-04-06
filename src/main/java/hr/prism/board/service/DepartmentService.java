package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
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
    
    @Inject
    private ActionService actionService;
    
    public Department getDepartment(Long id) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, id);
        return (Department) actionService.executeAction(currentUser, department, Action.VIEW, () -> department);
    }
    
    public Department getDepartment(String handle) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, handle);
        return (Department) actionService.executeAction(currentUser, department, Action.VIEW, () -> department);
    }
    
    public List<Department> getDepartments() {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilterDTO()
                .setScope(Scope.DEPARTMENT)
                .setOrderStatement("order by resource.name"))
            .stream().map(resource -> (Department) resource).collect(Collectors.toList());
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
                break;
            }
        }
    
        Department department;
        User currentUser = userService.getCurrentUserSecured();
        if (departmentById != null) {
            department = departmentById;
        } else if (departmentByName != null) {
            department = departmentByName;
        } else {
            department = new Department();
            resourceService.updateState(department, State.ACCEPTED);
            department.setName(departmentDTO.getName());
            if (departmentDTO.getDocumentLogo() != null) {
                department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
            }
            
            String handle = departmentDTO.getHandle();
            if (departmentRepository.findByHandle(handle) != null) {
                throw new ApiException(ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
            }
            
            resourceService.updateHandle(department, departmentDTO.getHandle());
            List<String> memberCategories = departmentDTO.getMemberCategories();
            
            department = departmentRepository.save(department);
            resourceService.updateCategories(department, memberCategories, CategoryType.MEMBER);
            resourceService.createResourceRelation(department, department);
            userRoleService.createUserRole(department, currentUser, Role.ADMINISTRATOR);
        }
    
        return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
    }
    
    public Department updateDepartment(Long departmentId, DepartmentDTO departmentDTO) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        Department updatedDepartment = (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
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
            
            String newHandle = departmentDTO.getHandle();
            if (!newHandle.equals(department.getHandle())) {
                if (departmentRepository.findByHandle(newHandle) != null) {
                    throw new ApiException(ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
                }
    
                resourceService.updateHandle(department, departmentDTO.getHandle());
            }
            
            List<String> memberCategories = departmentDTO.getMemberCategories();
            resourceService.updateCategories(department, memberCategories, CategoryType.MEMBER);
            return department;
        });
        
        return updatedDepartment;
    }
    
}
