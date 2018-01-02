package hr.prism.board.service.scheduled;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.Result;
import hr.prism.board.service.ActivityService;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ActivityScheduledService {

    private Logger LOGGER = LoggerFactory.getLogger(ActivityScheduledService.class);

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Inject
    private Pusher pusher;

    @Inject
    private ActivityService activityService;

    @Inject
    private ObjectMapper objectMapper;

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void executeScheduled() throws IOException {
        if (BooleanUtils.isTrue(schedulerOn)) {
            Result result = pusher.get("/channels");
            String message = result.getMessage();
            if (message != null) {
                Map<String, Map<String, Map<String, Object>>> wrapper = objectMapper.readValue(
                    message, new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {
                    });

                Map<String, Map<String, Object>> channels = wrapper.get("channels");
                if (channels != null) {
                    LOGGER.info("Updating users subscribed to activity stream");
                    activityService.setUserIds(wrapper.get("channels").keySet().stream()
                        .filter(channel -> channel.startsWith("presence-activities-"))
                        .map(channel -> Long.parseLong(channel.split("-")[2])).collect(Collectors.toList()));
                }
            }
        }
    }

}
