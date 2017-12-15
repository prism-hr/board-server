package hr.prism.board.service.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.rest.Pusher;
import hr.prism.board.service.ActivityService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ActivityScheduledService {

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Inject
    private Pusher pusher;

    @Inject
    private ActivityService activityService;

    @Inject
    private ObjectMapper objectMapper;

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void updateUserIds() throws IOException {
        if (BooleanUtils.isTrue(schedulerOn)) {
//            Result result = pusher.get("/channels");
//            Map<String, Map<String, Map<String, Object>>> channels = objectMapper.readValue(
//                result.getMessage(), new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {
//                });
//
//            activityService.setUserIds(channels.get("message").keySet().stream().map(channel -> Long.parseLong(channel.split("-")[2])).collect(Collectors.toList()));
        }
    }

}
