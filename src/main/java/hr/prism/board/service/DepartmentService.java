package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

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
    
    public List<Department> findAllByUserOrderByName() {
        User currentUser = userService.getCurrentUserSecured();
        Collection<Long> departmentIds = currentUser.getResourceActions().getIds();
        if (departmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return departmentRepository.findAllByUserByOrderByName(departmentIds);
    }
    
    public Department findOne(Long id) {
        return departmentRepository.findOne(id);
    }
    
    public Department findByHandle(String handle) {
        return departmentRepository.findByHandle(handle);
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
                break;
            }
        }
        
        if (departmentById == null && departmentByName == null) {
            Department department = new Department();
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
            userRoleService.createUserRole(department, userService.getCurrentUserSecured(), Role.ADMINISTRATOR);
            return department;
        }
        
        return departmentByName == null ? departmentById : departmentByName;
    }
    
    public void updateDepartment(Long departmentId, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findOne(departmentId);
        
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
    }
    
}
