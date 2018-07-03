package hr.prism.board.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.User;
import hr.prism.board.dto.PusherAuthenticationDTO;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.service.ActivityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class UserActivityApi {

    private final ActivityService activityService;

    private final ObjectMapper objectMapper;

    @Inject
    public UserActivityApi(ActivityService activityService, ObjectMapper objectMapper) {
        this.activityService = activityService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/activities", method = GET)
    public List<ActivityRepresentation> getActivities(@AuthenticationPrincipal User user) {
        return activityService.getActivities(user);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/activities/{activityId}", method = GET)
    public void viewActivity(@AuthenticationPrincipal User user, @PathVariable Long activityId) {
        activityService.viewActivity(user, activityId);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user/activities/{activityId}", method = DELETE)
    public void dismissActivity(@AuthenticationPrincipal User user, @PathVariable Long activityId) {
        activityService.dismissActivity(user, activityId);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/pusher/authenticate", method = POST)
    public JsonNode authenticatePusher(@AuthenticationPrincipal User user,
                                       @RequestBody PusherAuthenticationDTO pusherAuthentication) throws IOException {
        return objectMapper.readTree(activityService.authenticatePusher(user, pusherAuthentication));
    }

}
