package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
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
    private ResourcePatchService resourcePatchService;
    
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
        String name = StringUtils.normalizeSpace(departmentDTO.getName());
        
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
    
            String handle = ResourceService.suggestHandle(name);
            List<String> similarHandles = departmentRepository.findHandleLikeSuggestedHandle(handle);
            handle = ResourceService.confirmHandle(handle, similarHandles);
    
            resourceService.updateHandle(department, handle);
            List<String> memberCategories = departmentDTO.getMemberCategories();
    
            department = departmentRepository.save(department);
            resourceService.updateCategories(department, CategoryType.MEMBER, memberCategories);
            resourceService.createResourceRelation(department, department);
            resourceService.createResourceOperation(department, Action.EXTEND, currentUser);
            userRoleService.createUserRole(department, currentUser, Role.ADMINISTRATOR);
        }
        
        return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
    }
    
    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            department.setChangeList(new ResourceChangeListRepresentation());
            resourcePatchService.patchName(department, departmentDTO.getName(), ExceptionCode.MISSING_DEPARTMENT_NAME, ExceptionCode.DUPLICATE_DEPARTMENT);
            resourcePatchService.patchDocument(department, "documentLogo", department::getDocumentLogo, department::setDocumentLogo, departmentDTO.getDocumentLogo());
            resourcePatchService.patchHandle(department, departmentDTO.getHandle(), ExceptionCode.MISSING_DEPARTMENT_HANDLE, ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
            resourcePatchService.patchCategories(department, CategoryType.MEMBER, departmentDTO.getMemberCategories());
            department.setComment(departmentDTO.getComment());
            return department;
        });
    }
    
}
