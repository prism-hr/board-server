package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.mapper.DocumentMapper;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
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
    
    @Inject
    private DocumentMapper documentMapper;
    
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
    
            validateDepartment(department);
            department = departmentRepository.save(department);
    
            resourceService.updateCategories(department, memberCategories, CategoryType.MEMBER);
            resourceService.createResourceRelation(department, department);
            userRoleService.createUserRole(department, currentUser, Role.ADMINISTRATOR);
        }
        
        return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
    }
    
    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        Department updatedDepartment = (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            updateDepartment(department, departmentDTO);
            return department;
        });
    
        return updatedDepartment;
    }
    
    void updateDepartment(Department department, DepartmentPatchDTO departmentDTO) {
        ResourceChangeListRepresentation changeList = new ResourceChangeListRepresentation();
        
        Optional<String> nameOptional = departmentDTO.getName();
        if (nameOptional != null) {
            String oldName = department.getName();
            String newName = nameOptional.orElse(null);
            if (!Objects.equals(newName, oldName)) {
                if (departmentRepository.findByName(newName) != null) {
                    throw new ApiException(ExceptionCode.DUPLICATE_DEPARTMENT);
                }
                
                department.setName(newName);
                changeList.put("name", oldName, newName);
            }
        }
        
        if (departmentDTO.getDocumentLogo() != null) {
            Document oldLogo = department.getDocumentLogo();
            String oldLogoId = Optional.ofNullable(oldLogo).map(Document::getCloudinaryId).orElse(null);
            String newLogoId = departmentDTO.getDocumentLogo().map(DocumentDTO::getCloudinaryId).orElse(null);
            if (!Objects.equals(newLogoId, oldLogoId)) {
                department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo().orElse(null)));
                changeList.put("documentLogo", documentMapper.apply(oldLogo), documentMapper.apply(department.getDocumentLogo()));
            }
        }
        
        Optional<String> handleOptional = departmentDTO.getHandle();
        if (handleOptional != null) {
            String oldHandle = department.getHandle();
            String newHandle = handleOptional.orElse(null);
            if (!Objects.equals(newHandle, oldHandle)) {
                if (departmentRepository.findByHandle(newHandle) != null) {
                    throw new ApiException(ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
                }
                
                resourceService.updateHandle(department, newHandle);
                changeList.put("handle", oldHandle, newHandle);
            }
        }
        
        if (departmentDTO.getMemberCategories() != null) {
            List<String> oldMemberCategories = resourceService.getCategories(department, CategoryType.MEMBER);
            resourceService.updateCategories(department, departmentDTO.getMemberCategories().orElse(Collections.emptyList()), CategoryType.MEMBER);
            changeList.put("memberCategories", oldMemberCategories, resourceService.getCategories(department, CategoryType.MEMBER));
        }
        
        validateDepartment(department);
        department.setChangeList(changeList);
        department.setComment(departmentDTO.getComment());
    }
    
    private void validateDepartment(Department department) {
        if (department.getName() == null) {
            throw new ApiException(ExceptionCode.MISSING_DEPARTMENT_NAME);
        } else if (department.getHandle() == null) {
            throw new ApiException(ExceptionCode.MISSING_DEPARTMENT_HANDLE);
        }
    }
    
}
