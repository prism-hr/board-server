package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.UserNotificationSuppressionService;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

@RestController
public class UserApi {

    @Inject
    private UserService userService;

    @Inject
    private UserNotificationSuppressionService userNotificationSuppressionService;

    @Inject
    private UserMapper userMapper;

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public UserRepresentation getCurrentUser() {
        User currentUser = userService.getCurrentUserSecured();
        return userMapper.apply(currentUser);
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.PATCH)
    public UserRepresentation updateUser(@RequestBody @Valid UserPatchDTO userDTO) {
        User currentUser = userService.updateUser(userDTO);
        return userMapper.apply(currentUser);
    }

    @RequestMapping(value = "api/user/suppressions", method = RequestMethod.GET)
    public List<UserNotificationSuppressionRepresentation> getSuppressions() {
        return userNotificationSuppressionService.getSuppressions();
    }

    @RequestMapping(value = "api/user/suppressions/{resourceId}", method = RequestMethod.POST)
    public UserNotificationSuppressionRepresentation postSuppression(@PathVariable("resourceId") Long resourceId, @RequestParam(required = false) String uuid) {
        return userNotificationSuppressionService.postSuppression(uuid, resourceId);
    }

    @RequestMapping(value = "api/user/suppressions", method = RequestMethod.POST)
    public List<UserNotificationSuppressionRepresentation> postSuppressions() {
        return userNotificationSuppressionService.postSuppressions();
    }

    @RequestMapping(value = "api/user/suppressions/{resourceId}", method = RequestMethod.DELETE)
    public void deleteSuppression(@PathVariable Long resourceId) {
        userNotificationSuppressionService.deleteSuppression(resourceId);
    }

    @RequestMapping(value = "api/user/suppressions", method = RequestMethod.DELETE)
    public void deleteSuppressions() {
        userNotificationSuppressionService.deleteSuppressions();
    }


}
