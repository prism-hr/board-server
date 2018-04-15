package hr.prism.board.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.dto.PusherAuthenticationDTO;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.service.ActivityService;
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
        return objectMapper.readTree(activityService.authenticatePusher(pusherAuthentication));
    }

}
