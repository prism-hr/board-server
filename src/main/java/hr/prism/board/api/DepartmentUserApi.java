package hr.prism.board.api;

import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.representation.UserRolesRepresentation;
import hr.prism.board.service.DepartmentUserService;
import hr.prism.board.service.UserRoleService;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DepartmentUserApi {

    private final DepartmentUserService departmentUserService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private final DepartmentMapper departmentMapper;

    private final UserMapper userMapper;

    private final UserRoleMapper userRoleMapper;

    @Inject
    public DepartmentUserApi(DepartmentUserService departmentUserService, UserService userService,
                             UserRoleService userRoleService, DepartmentMapper departmentMapper, UserMapper userMapper,
                             UserRoleMapper userRoleMapper) {
        this.departmentUserService = departmentUserService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @RequestMapping(value = "/api/departments/{departmentId}/users/bulk", method = POST)
    public DepartmentRepresentation createMembers(@PathVariable Long departmentId,
                                                  @RequestBody @Valid List<UserRoleDTO> users) {
        return departmentMapper.apply(departmentUserService.createMembers(departmentId, users));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests", method = POST)
    public UserRepresentation createMembershipRequest(@PathVariable Long departmentId,
                                                      @RequestBody @Valid UserRoleDTO userRoleDTO) {
        return userMapper.apply(departmentUserService.createMembershipRequest(departmentId, userRoleDTO));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests/{userId}", method = PUT)
    public UserRoleRepresentation viewMembershipRequest(@PathVariable Long departmentId, @PathVariable Long userId) {
        return userRoleMapper.apply(departmentUserService.viewMembershipRequest(departmentId, userId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests/{userId}/{state:accepted|rejected}",
        method = PUT)
    public void reviewMembershipRequest(@PathVariable Long departmentId, @PathVariable Long userId,
                                        @PathVariable String state) {
        departmentUserService.reviewMembershipRequest(departmentId, userId, State.valueOf(state.toUpperCase()));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/memberRequests", method = PUT)
    public UserRepresentation updateMembershipData(@PathVariable Long departmentId,
                                                   @RequestBody @Valid UserRoleDTO userRoleDTO) {
        return userMapper.apply(departmentUserService.updateMembershipData(departmentId, userRoleDTO));
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users", method = GET)
    public UserRolesRepresentation getUserRoles(
        @PathVariable Long resourceId, @RequestParam(value = "/searchTerm", required = false) String searchTerm) {
        return userRoleService.getUserRoles(DEPARTMENT, resourceId, searchTerm);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users", method = POST)
    public UserRoleRepresentation createUserRole(@PathVariable Long resourceId,
                                                 @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.createUserRole(DEPARTMENT, resourceId, user);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users/{userId}", method = PUT)
    public UserRoleRepresentation updateUserRole(@PathVariable Long resourceId,
                                                 @PathVariable Long userId, @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.updateUserRole(DEPARTMENT, resourceId, userId, user);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/users/{userId}", method = DELETE)
    public void deleteUserRoles(@PathVariable Long resourceId, @PathVariable Long userId) {
        userRoleService.deleteUserRoles(DEPARTMENT, resourceId, userId);
    }

    @RequestMapping(value = "/api/departments/{resourceId}/lookupUsers", method = GET)
    public List<UserRepresentation> findUsers(@PathVariable Long resourceId, @RequestParam String query) {
        return userService.findUsers(DEPARTMENT, resourceId, query);
    }

}
