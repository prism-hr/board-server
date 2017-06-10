package hr.prism.board.api;

import hr.prism.board.domain.Scope;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.ResourceUsersDTO;
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
    public ResourceUserRepresentation createResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestBody @Valid ResourceUserDTO user) {
        return userRoleService.createResourceUser(scope, resourceId, user);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/bulk", method = RequestMethod.POST)
    public void createResourceUsers(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestBody @Valid ResourceUsersDTO users) {
        userRoleService.createResourceUsers(scope, resourceId, users);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.DELETE)
    public void deleteResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId) {
        userRoleService.deleteResourceUser(scope, resourceId, userId);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.PUT)
    public ResourceUserRepresentation updateResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId, @RequestBody @Valid ResourceUserDTO user) {
        return userRoleService.updateResourceUser(scope, resourceId, userId, user);
    }

    @ModelAttribute
    public Scope getScope(@PathVariable String scopePlural) {
        return Scope.valueOf(StringUtils.removeEnd(scopePlural, "s").toUpperCase());
    }

}
