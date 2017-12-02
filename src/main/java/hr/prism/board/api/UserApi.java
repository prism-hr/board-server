package hr.prism.board.api;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPasswordDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.UserNotificationSuppressionService;
import hr.prism.board.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

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
    
    @RequestMapping(value = "/api/user/activities/{activityId}", method = RequestMethod.GET)
    public void viewActivity(@PathVariable Long activityId) {
        activityService.viewActivity(activityId);
    }
    
    @RequestMapping(value = "/api/user/activities/{activityId}", method = RequestMethod.DELETE)
    public void dismissActivity(@PathVariable Long activityId) {
        activityService.dismissActivity(activityId);
    }
    
    @SubscribeMapping("/api/user/activities")
    public void subscribe(Principal principal) {
        SecurityContextHolder.getContext().setAuthentication((AuthenticationToken) principal);
        activityService.sendActivities(userService.getCurrentUserSecured().getId());
    }
    
    @RequestMapping(value = "/api/user/test", method = RequestMethod.DELETE)
    public void deleteTestUsers() {
        userService.deleteTestUsers();
    }
    
}
