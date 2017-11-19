package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.dto.WidgetOptionsDTO;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.representation.UserRolesRepresentation;
import hr.prism.board.service.BadgeService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.ResourceTaskService;
import hr.prism.board.service.UserRoleService;
import hr.prism.board.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceApi {

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Inject
    private BadgeService badgeService;

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users", method = RequestMethod.GET)
    public UserRolesRepresentation getUserRoles(@ModelAttribute Scope scope, @PathVariable Long resourceId,
                                                @RequestParam(value = "/searchTerm", required = false) String searchTerm) {
        return userRoleService.getUserRoles(scope, resourceId, searchTerm);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users", method = RequestMethod.POST)
    public UserRoleRepresentation createResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId,
                                                     @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.createResourceUser(scope, resourceId, user);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.DELETE)
    public void deleteResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId, @PathVariable Long userId) {
        userRoleService.deleteResourceUser(scope, resourceId, userId);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/users/{userId}", method = RequestMethod.PUT)
    public UserRoleRepresentation updateResourceUser(@ModelAttribute Scope scope, @PathVariable Long resourceId,
                                                     @PathVariable Long userId, @RequestBody @Valid UserRoleDTO user) {
        return userRoleService.updateResourceUser(scope, resourceId, userId, user);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{resourceId}/lookupUsers", method = RequestMethod.GET)
    public List<UserRepresentation> getSimilarUsers(@ModelAttribute Scope scope, @PathVariable Long resourceId,
                                                    @RequestParam String query) {
        return userService.findBySimilarNameAndEmail(scope, resourceId, query);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards|posts}/archiveQuarters", method = RequestMethod.GET)
    public List<String> getResourceArchiveQuarters(@ModelAttribute Scope scope, @RequestParam(required = false) Long parentId) {
        return resourceService.getResourceArchiveQuarters(scope, parentId);
    }

    @RequestMapping(value = "/api/{scopePlural:departments|boards}/{id}/badge", method = RequestMethod.GET)
    public String getResourceBadge(@ModelAttribute Scope scope, @PathVariable Long id,
                                   @RequestParam String options, HttpServletResponse response) throws IOException {
        response.setHeader("X-Frame-Options", "ALLOW");
        ObjectMapper objectMapper = new ObjectMapper();
        WidgetOptionsDTO widgetOptions = objectMapper.readValue(options, new TypeReference<WidgetOptionsDTO>() {
        });
        return badgeService.getResourceBadge(resourceService.getResource(null, scope, id), widgetOptions);
    }

    @ModelAttribute
    public Scope getScope(@PathVariable(required = false) String scopePlural) {
        return scopePlural == null ? null : Scope.valueOf(StringUtils.removeEnd(scopePlural, "s").toUpperCase());
    }

}
