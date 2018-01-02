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

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void executeScheduled() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            notifyTasks();
        }
    }

    public void notifyTasks() {
        LocalDateTime baseline = LocalDateTime.now();
        ArrayListMultimap<Pair<Long, Integer>, ResourceTask> resourceTasks = resourceTaskService.getResourceTasks(baseline);
        resourceTasks.keySet().forEach(resourceId -> resourceTaskService.sendNotification(resourceId, resourceTasks.get(resourceId)));
    }

}
