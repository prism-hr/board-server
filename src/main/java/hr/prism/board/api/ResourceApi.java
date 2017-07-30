package hr.prism.board.api;

import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.ResourceUsersDTO;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.UserRoleService;
import hr.prism.board.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

@RestController
public class ResourceApi {

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;

    @Inject
    private ResourceService resourceService;

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

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/lookupUsers", method = RequestMethod.GET)
    public List<UserRepresentation> getSimilarUsers(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestParam String query) {
        return userService.findBySimilarNameAndEmail(scope, resourceId, query);
    }

    @ModelAttribute
    public Scope getScope(@PathVariable(required = false) String scopePlural) {
        return scopePlural == null ? null : Scope.valueOf(StringUtils.removeEnd(scopePlural, "s").toUpperCase());
    }

}
