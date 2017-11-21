package hr.prism.board.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.security.Principal;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import hr.prism.board.authentication.AuthenticationToken;
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
import hr.prism.board.service.WebSocketService;

@RestController
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "unused"})
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
    private WebSocketService webSocketService;

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

    @SubscribeMapping("/api/user/activities")
    public void subscribeToActivities(Principal principal) {
        SecurityContextHolder.getContext().setAuthentication((AuthenticationToken) principal);
        Long userId = userService.getCurrentUserSecured().getId();
        List<ActivityRepresentation> activities = activityService.getActivities(userId);
        webSocketService.sendActivities(userId, activities);
    }

    public DeferredResult<List<ActivityRepresentation>> refreshActivities(Long userId) {
        DeferredResult<List<ActivityRepresentation>> request = new DeferredResult<>(deferredRequestTimeoutMillis);
        request.onTimeout(() -> userActivityService.processRequestTimeout(userId, request));
        userActivityService.storeRequest(userId, request);
        return request;
    }

}
