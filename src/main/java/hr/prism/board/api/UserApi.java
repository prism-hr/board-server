package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPasswordDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.UserActivityService;
import hr.prism.board.service.UserNotificationSuppressionService;
import hr.prism.board.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
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

    @Value("${deferred.request.timeout.millis}")
    private Long deferredRequestTimeoutMillis;

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public UserRepresentation getUser() {
        return userMapper.apply(userService.getUserForRepresentation());
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.PATCH)
    public UserRepresentation updateUser(@RequestBody @Valid UserPatchDTO userDTO) {
        User currentUser = userService.updateUser(userDTO);
        return userMapper.apply(currentUser);
    }

    @RequestMapping(value = "/api/user/password", method = RequestMethod.PATCH)
    public void resetPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        userService.resetPassword(userPasswordDTO);
    }

    @RequestMapping(value = "/api/user/suppressions", method = RequestMethod.GET)
    public List<UserNotificationSuppressionRepresentation> getSuppressions() {
        return userNotificationSuppressionService.getSuppressions();
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = RequestMethod.POST)
    public UserNotificationSuppressionRepresentation postSuppression(@PathVariable Long resourceId, @RequestParam(required = false) String uuid) {
        return userNotificationSuppressionService.postSuppression(uuid, resourceId);
    }

    @RequestMapping(value = "/api/user/suppressions", method = RequestMethod.POST)
    public List<UserNotificationSuppressionRepresentation> postSuppressions() {
        return userNotificationSuppressionService.postSuppressions();
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = RequestMethod.DELETE)
    public void deleteSuppression(@PathVariable Long resourceId) {
        userNotificationSuppressionService.deleteSuppression(resourceId);
    }

    @RequestMapping(value = "/api/user/suppressions", method = RequestMethod.DELETE)
    public void deleteSuppressions() {
        userNotificationSuppressionService.deleteSuppressions();
    }

    @RequestMapping(value = "/api/user/activities", method = RequestMethod.GET)
    public List<ActivityRepresentation> getActivities() {
        return activityService.getActivities(userService.getCurrentUserSecured().getId());
    }

    @RequestMapping(value = "/api/user/activities/refresh", method = RequestMethod.GET)
    public DeferredResult<List<ActivityRepresentation>> refreshActivities() {
        return refreshActivities(userService.getCurrentUserSecured().getId());
    }

    @RequestMapping(value = "/api/user/activities/{activityId}", method = RequestMethod.GET)
    public void viewActivity(@PathVariable Long activityId) {
        activityService.viewActivity(activityId);
    }

    @RequestMapping(value = "/api/user/activities/{activityId}", method = RequestMethod.DELETE)
    public void dismissActivity(@PathVariable Long activityId) {
        activityService.dismissActivity(activityId);
    }

    public DeferredResult<List<ActivityRepresentation>> refreshActivities(Long userId) {
        DeferredResult<List<ActivityRepresentation>> request = new DeferredResult<>(deferredRequestTimeoutMillis);
        request.onTimeout(() -> userActivityService.processRequestTimeout(userId, request));
        userActivityService.storeRequest(userId, request);
        return request;
    }

}
