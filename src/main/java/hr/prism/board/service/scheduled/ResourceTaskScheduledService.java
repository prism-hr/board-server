package hr.prism.board.service.scheduled;

import hr.prism.board.service.ResourceTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
public class ResourceTaskScheduledService {

    @Inject
    private ResourceTaskService resourceTaskService;

    @Scheduled(cron = "0 0 7 1/1 * ? *")
    public void updateTasks() {
        LocalDateTime baseline = LocalDateTime.now();
        LocalDateTime baseline1 = baseline.minusWeeks(1);
        LocalDateTime baseline2 = baseline.minusWeeks(2);
        LocalDateTime baseline3 = baseline.minusWeeks(4);
        resourceTaskService.findAllIds(baseline1, baseline2, baseline3).forEach(taskId -> resourceTaskService.sendNotifications(taskId));
    }

}
