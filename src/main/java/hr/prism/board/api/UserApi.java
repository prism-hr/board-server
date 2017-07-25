package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.UserActivityService;
import hr.prism.board.service.UserNotificationSuppressionService;
import hr.prism.board.service.UserService;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

@RestController
public class UserApi {

    @Inject
    private ActivityService activityService;

    @Inject
    private UserService userService;

    @Inject
    private UserNotificationSuppressionService userNotificationSuppressionService;

    @Inject
    private UserActivityService userActivityService;

    @Inject
    private UserMapper userMapper;

    @Inject
    private Environment environment;

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

    @RequestMapping(value = "api/user/activities", method = RequestMethod.GET)
    public List<ActivityRepresentation> getActivities() {
        return activityService.getActivities();
    }

    @RequestMapping(value = "api/user/activities/refresh", method = RequestMethod.GET)
    public DeferredResult<List<ActivityRepresentation>> refreshActivities() {
        Long userId = userService.getCurrentUserSecured().getId();
        DeferredResult<List<ActivityRepresentation>> request = new DeferredResult<>(Long.parseLong(environment.getProperty("deferred.request.timeout.millis")));
        request.onTimeout(() -> userActivityService.processRequestTimeout(userId, request));
        userActivityService.storeRequest(userId, request);
        return request;
    }

    @RequestMapping(value = "api/user/activities/{activityId}/dismiss", method = RequestMethod.POST)
    public void dismissActivity(@PathVariable Long activityId) {
        activityService.dismissActivity(activityId);
    }

}
