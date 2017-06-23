package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.*;
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

    public List<Department> getDepartments(Boolean includePublicDepartments) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilterDTO()
                .setScope(Scope.DEPARTMENT)
                .setIncludePublicResources(includePublicDepartments)
                .setOrderStatement("order by resource.name"))
            .stream().map(resource -> (Department) resource).collect(Collectors.toList());
    }

    public Department getOrCreateDepartment(User currentUser, DepartmentDTO departmentDTO) {
        Long id = departmentDTO.getId();
        String name = StringUtils.normalizeSpace(departmentDTO.getName());

        Department departmentById = null;
        Department departmentByName = null;
        for (Department department : departmentRepository.findByIdOrName(id, name)) {
            if (department.getId().equals(id)) {
                departmentById = department;
            }

            if (department.getName().equals(name)) {
                departmentByName = department;
                break;
            }
        }

        if (departmentById != null) {
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentById.getId());
        } else if (departmentByName != null) {
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentByName.getId());
        } else {
            Department department = new Department();
            resourceService.updateState(department, State.ACCEPTED);
            department.setName(name);
            department.setSummary(departmentDTO.getSummary());
            if (departmentDTO.getDocumentLogo() != null) {
                department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
            }

            String handle = ResourceService.suggestHandle(name);
            List<String> similarHandles = departmentRepository.findHandleByLikeSuggestedHandle(handle);
            handle = ResourceService.confirmHandle(handle, similarHandles);

            resourceService.updateHandle(department, handle);
            department = departmentRepository.save(department);
            resourceService.updateCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(departmentDTO.getMemberCategories()));
            resourceService.createResourceRelation(department, department);
            resourceService.createResourceOperation(department, Action.EXTEND, currentUser);
            userRoleService.createUserRole(department, currentUser, Role.ADMINISTRATOR);
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
        }
    }

    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            department.setChangeList(new ResourceChangeListRepresentation());
            resourcePatchService.patchName(department, departmentDTO.getName(), ExceptionCode.DUPLICATE_DEPARTMENT);
            resourcePatchService.patchProperty(department, "summary", department::getSummary, department::setSummary, departmentDTO.getSummary());
            resourcePatchService.patchHandle(department, departmentDTO.getHandle(), ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
            resourcePatchService.patchDocument(department, "documentLogo", department::getDocumentLogo, department::setDocumentLogo, departmentDTO.getDocumentLogo());
            resourcePatchService.patchCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(departmentDTO.getMemberCategories()));
            departmentRepository.update(department);
            return department;
        });
    }

}
