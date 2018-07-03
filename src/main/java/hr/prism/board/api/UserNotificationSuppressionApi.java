package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.mapper.UserNotificationSuppressionMapper;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.service.UserNotificationSuppressionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class UserNotificationSuppressionApi {

    private final UserNotificationSuppressionService userNotificationSuppressionService;

    private final UserNotificationSuppressionMapper userNotificationSuppressionMapper;

    @Inject
    public UserNotificationSuppressionApi(UserNotificationSuppressionService userNotificationSuppressionService,
                                          UserNotificationSuppressionMapper userNotificationSuppressionMapper) {
        this.userNotificationSuppressionService = userNotificationSuppressionService;
        this.userNotificationSuppressionMapper = userNotificationSuppressionMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/suppressions", method = GET)
    public List<UserNotificationSuppressionRepresentation> getSuppressions(@AuthenticationPrincipal User user) {
        return userNotificationSuppressionService.getSuppressions(user)
            .stream().map(userNotificationSuppressionMapper).collect(toList());
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = POST)
    public UserNotificationSuppressionRepresentation postSuppression(@AuthenticationPrincipal User user,
                                                                     @PathVariable Long resourceId,
                                                                     @RequestParam(required = false) String uuid) {
        return userNotificationSuppressionMapper.apply(
            userNotificationSuppressionService.createSuppression(user, uuid, resourceId));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/suppressions", method = POST)
    public List<UserNotificationSuppressionRepresentation> postSuppressions(@AuthenticationPrincipal User user) {
        return userNotificationSuppressionService.createSuppressionsForAllResources(user)
            .stream().map(userNotificationSuppressionMapper).collect(toList());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = DELETE)
    public void deleteSuppression(@AuthenticationPrincipal User user, @PathVariable Long resourceId) {
        userNotificationSuppressionService.deleteSuppression(user, resourceId);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/suppressions", method = DELETE)
    public void deleteSuppressions(@AuthenticationPrincipal User user) {
        userNotificationSuppressionService.deleteSuppressions(user);
    }

}
