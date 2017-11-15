package hr.prism.board.api;

import com.stripe.model.Customer;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class DepartmentApi {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private ResourceOperationMapper resourceOperationMapper;

    @Inject
    private UserRoleMapper userRoleMapper;

    @Inject
    private UserMapper userMapper;

    @RequestMapping(value = "/api/universities/{universityId}/departments", method = RequestMethod.POST)
    public DepartmentRepresentation postDepartment(@PathVariable Long universityId, @RequestBody @Valid DepartmentDTO departmentDTO) {
        return departmentMapper.apply(departmentService.createDepartment(universityId, departmentDTO));
    }

    @RequestMapping(value = "/api/universities/{universityId}/departments", method = RequestMethod.GET, params = "query")
    public List<DepartmentRepresentation> lookupDepartments(@PathVariable Long universityId, @RequestParam String query) {
        return departmentService.findBySimilarName(universityId, query);
    }

    @RequestMapping(value = "/api/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments(@RequestParam(required = false) Boolean includePublic, @RequestParam(required = false) String searchTerm) {
        return departmentService.getDepartments(includePublic, searchTerm).stream().map(departmentMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long departmentId) {
        return departmentMapper.apply(departmentService.getDepartment(departmentId));
    }

    @RequestMapping(value = "/api/departments", method = RequestMethod.GET, params = "handle")
    public DepartmentRepresentation getDepartmentByHandle(@RequestParam String handle) {
        return departmentMapper.apply(departmentService.getDepartment(handle));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getDepartmentOperations(@PathVariable Long departmentId) {
        return resourceService.getResourceOperations(Scope.DEPARTMENT, departmentId).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}", method = RequestMethod.PATCH)
    public DepartmentRepresentation patchDepartment(@PathVariable Long departmentId, @RequestBody @Valid DepartmentPatchDTO departmentDTO) {
        return departmentMapper.apply(departmentService.updateDepartment(departmentId, departmentDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/users/bulk", method = RequestMethod.POST)
    public DepartmentRepresentation postMembers(@PathVariable Long departmentId, @RequestBody @Valid List<UserRoleDTO> users) {
        return departmentMapper.apply(departmentService.postMembers(departmentId, users));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests", method = RequestMethod.POST)
    public UserRepresentation postMembershipRequest(@PathVariable Long departmentId, @RequestBody @Valid UserRoleDTO userRoleDTO) {
        return userMapper.apply(departmentService.postMembershipRequest(departmentId, userRoleDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests/{userId}", method = RequestMethod.PUT)
    public UserRoleRepresentation viewMembershipRequest(@PathVariable Long departmentId, @PathVariable Long userId) {
        return userRoleMapper.apply(departmentService.viewMembershipRequest(departmentId, userId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests/{userId}/{state:accepted|rejected}", method = RequestMethod.PUT)
    public void putMembershipRequest(@PathVariable Long departmentId, @PathVariable Long userId, @PathVariable String state) {
        departmentService.putMembershipRequest(departmentId, userId, State.valueOf(state.toUpperCase()));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests", method = RequestMethod.PUT)
    public UserRepresentation putMembershipUpdate(@PathVariable Long departmentId, @RequestBody @Valid UserRoleDTO userRoleDTO) {
        return userMapper.apply(departmentService.putMembershipUpdate(departmentId, userRoleDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/programs", method = RequestMethod.GET)
    public List<String> getDepartmentPrograms(@PathVariable Long departmentId, @RequestParam String searchTerm) {
        return departmentService.findProgramsBySimilarName(departmentId, searchTerm);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/tasks/{taskId}", method = RequestMethod.PUT)
    public void putTask(@PathVariable Long departmentId, @PathVariable Long taskId) {
        departmentService.putTask(departmentId, taskId);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/customer", method = RequestMethod.GET)
    public Customer getCustomer(@PathVariable Long departmentId) {
        return departmentService.getCustomer(departmentId);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/customer", method = RequestMethod.PUT)
    public Customer putCustomer(@PathVariable Long departmentId, @RequestParam String source) {
        return departmentService.putCustomer(departmentId, source);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/customer/{source}", method = RequestMethod.DELETE)
    public Customer deleteSource(@PathVariable Long departmentId, @PathVariable String source) {
        return departmentService.deleteSource(departmentId, source);
    }

}
