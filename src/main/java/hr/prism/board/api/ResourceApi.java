package hr.prism.board.api;

import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.representation.UserRolesRepresentation;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.ResourceTaskService;
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

    @Inject
    private ResourceTaskService resourceTaskService;

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users", method = RequestMethod.GET)
    public UserRolesRepresentation getUserRoles(@ModelAttribute Scope scope, @PathVariable Long resourceId,
                                                @RequestParam(value = "searchTerm", required = false) String searchTerm) {
        return userRoleService.getUserRoles(scope, resourceId, searchTerm);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users", method = RequestMethod.POST)
    public UserRoleRepresentation createResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.createResourceUser(scope, resourceId, user);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.DELETE)
    public void deleteResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId) {
        userRoleService.deleteResourceUser(scope, resourceId, userId);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.PUT)
    public UserRoleRepresentation updateResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId, @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.updateResourceUser(scope, resourceId, userId, user);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/lookupUsers", method = RequestMethod.GET)
    public List<UserRepresentation> getSimilarUsers(@ModelAttribute Scope scope, @PathVariable Long resourceId, @RequestParam String query) {
        return userService.findBySimilarNameAndEmail(scope, resourceId, query);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards|posts}/archiveQuarters", method = RequestMethod.GET)
    public List<String> getResourceArchiveQuarters(@ModelAttribute Scope scope, @RequestParam(required = false) Long parentId) {
        return resourceService.getResourceArchiveQuarters(scope, parentId);
    }

    @ModelAttribute
    public Scope getScope(@PathVariable(required = false) String scopePlural) {
        return scopePlural == null ? null : Scope.valueOf(StringUtils.removeEnd(scopePlural, "s").toUpperCase());
    }

}
