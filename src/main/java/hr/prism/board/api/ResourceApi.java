package hr.prism.board.api;

import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.dto.ResourceUserBulkDTO;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.service.UserRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

@RestController
public class ResourceApi {

    @Inject
    private UserRoleService userRoleService;

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users", method = RequestMethod.GET)
    public List<ResourceUserRepresentation> getResourceUsers(@ModelAttribute Scope scope, @PathVariable Long resourceId) {
        return userRoleService.getResourceUsers(scope, resourceId);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users", method = RequestMethod.POST)
    public ResourceUserRepresentation addResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestBody @Valid ResourceUserDTO user) {
        return userRoleService.addResourceUser(scope, resourceId, user);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/bulk", method = RequestMethod.POST)
    public void addResourceUsers(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestBody @Valid ResourceUserBulkDTO users) {
        userRoleService.addResourceUsers(scope, resourceId, users);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.DELETE)
    public void removeResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId) {
        userRoleService.removeResourceUser(scope, resourceId, userId);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}/roles/{role}", method = RequestMethod.PUT)
    public void addUserRole(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId, @PathVariable Role role) {
        userRoleService.addUserRole(scope, resourceId, userId, role);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}/roles/{role}", method = RequestMethod.DELETE)
    public void removeUserRole(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId, @PathVariable Role role) {
        userRoleService.removeUserRole(scope, resourceId, userId, role);
    }

    @ModelAttribute
    public Scope getScope(@PathVariable String scopePlural) {
        return Scope.valueOf(StringUtils.removeEnd(scopePlural, "s").toUpperCase());
    }

}
