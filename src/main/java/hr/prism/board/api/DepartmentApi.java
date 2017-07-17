package hr.prism.board.api;

import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Scope;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DepartmentApi {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourceOperationMapper resourceOperationMapper;

    @RequestMapping(value = "/api/departments", method = RequestMethod.POST)
    public DepartmentRepresentation postDepartment(@RequestBody @Valid DepartmentDTO departmentDTO) {
        return departmentMapper.apply(departmentService.createDepartment(departmentDTO));
    }

    @RequestMapping(value = "/api/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments(@RequestParam(required = false) Boolean includePublic) {
        return departmentService.getDepartments(includePublic).stream().map(department -> departmentMapper.apply(department)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/departments", method = RequestMethod.GET, params = "query")
    public List<DepartmentRepresentation> lookupDepartments(@RequestParam String query) {
        return departmentService.findBySimilarName(query);
    }

    @RequestMapping(value = "/api/departments/{id}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long id) {
        return departmentMapper.apply(departmentService.getDepartment(id));
    }

    @RequestMapping(value = "/api/departments", method = RequestMethod.GET, params = "handle")
    public DepartmentRepresentation getDepartmentByHandle(@RequestParam String handle) {
        return departmentMapper.apply(departmentService.getDepartment(handle));
    }

    @RequestMapping(value = "/api/departments/{id}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getDepartmentOperations(@PathVariable Long id) {
        return resourceService.getResourceOperations(Scope.DEPARTMENT, id).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/departments/{id}", method = RequestMethod.PATCH)
    public DepartmentRepresentation updateDepartment(@PathVariable Long id, @RequestBody @Valid DepartmentPatchDTO departmentDTO) {
        return departmentMapper.apply(departmentService.updateDepartment(id, departmentDTO));
    }

    @RequestMapping(value = "/api/departments/{id}/membership", method = RequestMethod.POST)
    public void postMembershipRequest(@PathVariable Long departmentId, @RequestBody @Valid UserRoleDTO userRoleDTO) {
        departmentService.createMembershipRequest(departmentId, userRoleDTO);
    }

}

