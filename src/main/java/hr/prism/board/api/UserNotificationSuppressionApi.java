package hr.prism.board.api;

import hr.prism.board.mapper.UserNotificationSuppressionMapper;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.service.UserNotificationSuppressionService;
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

    @RequestMapping(value = "/api/user/suppressions", method = GET)
    public List<UserNotificationSuppressionRepresentation> getSuppressions() {
        return userNotificationSuppressionService.getSuppressions()
            .stream().map(userNotificationSuppressionMapper).collect(toList());
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = POST)
    public UserNotificationSuppressionRepresentation postSuppression(@PathVariable Long resourceId,
                                                                     @RequestParam(required = false) String uuid) {
        return userNotificationSuppressionMapper.apply(
            userNotificationSuppressionService.createSuppression(uuid, resourceId));
    }

    @RequestMapping(value = "/api/user/suppressions", method = POST)
    public List<UserNotificationSuppressionRepresentation> postSuppressions() {
        return userNotificationSuppressionService.createSuppressionsForAllResources()
            .stream().map(userNotificationSuppressionMapper).collect(toList());
    }

    @RequestMapping(value = "/api/user/suppressions/{resourceId}", method = DELETE)
    public void deleteSuppression(@PathVariable Long resourceId) {
        userNotificationSuppressionService.deleteSuppression(resourceId);
    }

    @RequestMapping(value = "/api/user/suppressions", method = DELETE)
    public void deleteSuppressions() {
        userNotificationSuppressionService.deleteSuppressions();
    }

}
