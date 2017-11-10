package hr.prism.board.service.scheduled;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.service.ResourceTaskService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceTaskScheduledService {

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Scheduled(initialDelay = 60000, fixedDelay = 86400000)
    public void updateTasks() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime baseline1 = baseline.minusWeeks(1);
            LocalDateTime baseline2 = baseline.minusWeeks(2);
            LocalDateTime baseline3 = baseline.minusWeeks(4);

            ArrayListMultimap<Pair<Long, Integer>, ResourceTask> resourceTasks = resourceTaskService.getResourceTasks(baseline1, baseline2, baseline3);
            resourceTasks.keySet().forEach(resourceId -> resourceTaskService.sendNotifications(resourceId, resourceTasks.get(resourceId)));
        }
    }

}
