package hr.prism.board.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.User;
import hr.prism.board.dto.PusherAuthenticationDTO;
import hr.prism.board.dto.UserPasswordDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.UserNotificationSuppressionService;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class UserApi {

    private final ActivityService activityService;

    private final UserService userService;

    private final UserNotificationSuppressionService userNotificationSuppressionService;

    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

    @Inject
    public UserApi(ActivityService activityService, UserService userService,
                   UserNotificationSuppressionService userNotificationSuppressionService, UserMapper userMapper,
                   ObjectMapper objectMapper) {
        this.activityService = activityService;
        this.userService = userService;
        this.userNotificationSuppressionService = userNotificationSuppressionService;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/api/user", method = GET)
    public UserRepresentation getUser() {
        return userMapper.apply(userService.getUserForRepresentation());
    }

    @RequestMapping(value = "/api/user", method = PATCH)
    public UserRepresentation updateUser(@RequestBody @Valid UserPatchDTO userDTO) {
        User currentUser = userService.updateUser(userDTO);
        return userMapper.apply(currentUser);
    }

    @RequestMapping(value = "/api/user/password", method = PATCH)
    public void resetPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        userService.resetPassword(userPasswordDTO);
    }

    @RequestMapping(value = "/api/user/suppressions", method = GET)
    public List<UserNotificationSuppressionRepresentation> getSuppressions() {
        return userNotificationSuppressionService.getSuppressions();
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = POST)
    public UserNotificationSuppressionRepresentation postSuppression(@PathVariable Long resourceId,
                                                                     @RequestParam(required = false) String uuid) {
        return userNotificationSuppressionService.postSuppression(uuid, resourceId);
    }

    @RequestMapping(value = "/api/user/suppressions", method = POST)
    public List<UserNotificationSuppressionRepresentation> postSuppressions() {
        return userNotificationSuppressionService.postSuppressions();
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = DELETE)
    public void deleteSuppression(@PathVariable Long resourceId) {
        userNotificationSuppressionService.deleteSuppression(resourceId);
    }

    @RequestMapping(value = "/api/user/suppressions", method = DELETE)
    public void deleteSuppressions() {
        userNotificationSuppressionService.deleteSuppressions();
    }

    @RequestMapping(value = "/api/user/activities", method = GET)
    public List<ActivityRepresentation> getActivities() {
        return activityService.getActivities();
    }

    @RequestMapping(value = "/api/user/activities/{activityId}", method = GET)
    public void viewActivity(@PathVariable Long activityId) {
        activityService.viewActivity(activityId);
    }

    @RequestMapping(value = "/api/user/activities/{activityId}", method = DELETE)
    public void dismissActivity(@PathVariable Long activityId) {
        activityService.dismissActivity(activityId);
    }

    @RequestMapping(value = "/api/pusher/authenticate", method = POST)
    public JsonNode authenticatePusher(@RequestBody PusherAuthenticationDTO pusherAuthentication) throws IOException {
        return objectMapper.readTree(userService.authenticatePusher(pusherAuthentication));
    }

    @RequestMapping(value = "/api/user/test", method = DELETE)
    public void deleteTestUsers() {
        userService.deleteTestUsers();
    }

}
