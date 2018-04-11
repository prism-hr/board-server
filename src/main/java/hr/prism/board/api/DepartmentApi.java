package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.dto.WidgetOptionsDTO;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.representation.*;
import hr.prism.board.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DepartmentApi {

    private final DepartmentService departmentService;

    private final ResourceService resourceService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private final BadgeService badgeService;

    private final DepartmentMapper departmentMapper;

    private final ResourceOperationMapper resourceOperationMapper;

    private final UserRoleMapper userRoleMapper;

    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

    @Inject
    public DepartmentApi(DepartmentService departmentService, ResourceService resourceService, UserService userService,
                         UserRoleService userRoleService, BadgeService badgeService, DepartmentMapper departmentMapper,
                         ResourceOperationMapper resourceOperationMapper, UserRoleMapper userRoleMapper,
                         UserMapper userMapper, ObjectMapper objectMapper) {
        this.departmentService = departmentService;
        this.resourceService = resourceService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.badgeService = badgeService;
        this.departmentMapper = departmentMapper;
        this.resourceOperationMapper = resourceOperationMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/api/universities/{universityId}/departments", method = POST)
    public DepartmentRepresentation postDepartment(@PathVariable Long universityId,
                                                   @RequestBody @Valid DepartmentDTO departmentDTO) {
        return departmentMapper.apply(departmentService.createDepartment(universityId, departmentDTO));
    }

    @RequestMapping(value = "/api/universities/{universityId}/departments", method = GET)
    public List<DepartmentRepresentation> lookupDepartments(@PathVariable Long universityId,
                                                            @RequestParam String query) {
        return departmentService.findBySimilarName(universityId, query);
    }

    @RequestMapping(value = "/api/departments", method = GET)
    public List<DepartmentRepresentation> getDepartments(@RequestParam(required = false) Boolean includePublic,
                                                         @RequestParam(required = false) String searchTerm) {
        return departmentService.getDepartments(includePublic, searchTerm)
            .stream().map(departmentMapper).collect(toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}", method = GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long departmentId) {
        return departmentMapper.apply(departmentService.getDepartment(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/dashboard", method = GET)
    public DepartmentDashboardRepresentation getDepartmentDashboard(@PathVariable Long departmentId) {
        return departmentMapper.apply(departmentService.getDepartmentDashboard(departmentId));
    }

    @RequestMapping(value = "/api/departments", method = GET, params = "handle")
    public DepartmentRepresentation getDepartmentByHandle(@RequestParam String handle) {
        return departmentMapper.apply(departmentService.getDepartment(handle));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/operations", method = GET)
    public List<ResourceOperationRepresentation> getDepartmentOperations(@PathVariable Long departmentId) {
        return resourceService.getResourceOperations(DEPARTMENT, departmentId)
            .stream().map(resourceOperationMapper).collect(toList());
    }

    @RequestMapping(value = "/api/departments/{departmentId}", method = PATCH)
    public DepartmentRepresentation patchDepartment(@PathVariable Long departmentId,
                                                    @RequestBody @Valid DepartmentPatchDTO departmentDTO) {
        return departmentMapper.apply(departmentService.updateDepartment(departmentId, departmentDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/users/bulk", method = POST)
    public DepartmentRepresentation postMembers(@PathVariable Long departmentId,
                                                @RequestBody @Valid List<UserRoleDTO> users) {
        return departmentMapper.apply(departmentService.postMembers(departmentId, users));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests", method = POST)
    public UserRepresentation postMembershipRequest(@PathVariable Long departmentId,
                                                    @RequestBody @Valid UserRoleDTO userRoleDTO) {
        return userMapper.apply(departmentService.postMembershipRequest(departmentId, userRoleDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests/{userId}", method = PUT)
    public UserRoleRepresentation viewMembershipRequest(@PathVariable Long departmentId, @PathVariable Long userId) {
        return userRoleMapper.apply(departmentService.viewMembershipRequest(departmentId, userId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests/{userId}/{state:accepted|rejected}",
        method = PUT)
    public void putMembershipRequest(@PathVariable Long departmentId, @PathVariable Long userId,
                                     @PathVariable String state) {
        departmentService.putMembershipRequest(departmentId, userId, State.valueOf(state.toUpperCase()));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests", method = PUT)
    public UserRepresentation putMembershipUpdate(@PathVariable Long departmentId,
                                                  @RequestBody @Valid UserRoleDTO userRoleDTO) {
        return userMapper.apply(departmentService.putMembershipUpdate(departmentId, userRoleDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/programs", method = GET)
    public List<String> getDepartmentPrograms(@PathVariable Long departmentId, @RequestParam String searchTerm) {
        return departmentService.findProgramsBySimilarName(departmentId, searchTerm);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources", method = GET)
    public JsonNode getPaymentSources(@PathVariable Long departmentId) throws IOException {
        Customer customer = departmentService.getPaymentSources(departmentId);
        return readTree(customer);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = POST)
    public JsonNode addPaymentSourceAndSubscription(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentService.addPaymentSourceAndSubscription(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}/setDefault", method = POST)
    public JsonNode setPaymentSourceAsDefault(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentService.setPaymentSourceAsDefault(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = DELETE)
    public JsonNode deletePaymentSource(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentService.deletePaymentSource(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/cancelSubscription", method = POST)
    public JsonNode cancelSubscription(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentService.cancelSubscription(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/reactivateSubscription", method = POST)
    public JsonNode reactivateSubscription(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentService.reactivateSubscription(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/invoices", method = GET)
    public List<Invoice> getInvoices(@PathVariable Long departmentId) {
        return departmentService.getInvoices(departmentId);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users", method = GET)
    public UserRolesRepresentation getUserRoles(
        @PathVariable Long resourceId, @RequestParam(value = "/searchTerm", required = false) String searchTerm) {
        return userRoleService.getUserRoles(DEPARTMENT, resourceId, searchTerm);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users", method = POST)
    public UserRoleRepresentation createResourceUser(@PathVariable Long resourceId,
                                                     @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.createResourceUser(DEPARTMENT, resourceId, user);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users/{userId}", method = DELETE)
    public void deleteResourceUser(@PathVariable Long resourceId, @PathVariable Long userId) {
        userRoleService.deleteResourceUser(DEPARTMENT, resourceId, userId);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users/{userId}", method = PUT)
    public UserRoleRepresentation updateResourceUser(@PathVariable Long resourceId,
                                                     @PathVariable Long userId, @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.updateResourceUser(DEPARTMENT, resourceId, userId, user);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/lookupUsers", method = GET)
    public List<UserRepresentation> getSimilarUsers(@PathVariable Long resourceId, @RequestParam String query) {
        return userService.findBySimilarNameAndEmail(DEPARTMENT, resourceId, query);
    }

    @RequestMapping(value = "/api/departments/{id}/badge", method = GET)
    public String getResourceBadge(@PathVariable Long id, @RequestParam String options, HttpServletResponse response)
        throws IOException {
        response.setHeader("X-Frame-Options", "ALLOW");
        return badgeService.getResourceBadge(resourceService.getResource(null, DEPARTMENT, id),
            objectMapper.readValue(options, new TypeReference<WidgetOptionsDTO>() {
            }));
    }

    private JsonNode readTree(StripeObject object) throws IOException {
        return object == null ? null : objectMapper.readTree(object.toJson());
    }

}
