package hr.prism.board.service.scheduled;

import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.ResourceTaskService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class DepartmentScheduledService {

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Inject
    private ResourceService resourceService;

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void executeScheduled() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            updateTasks();
        }
    }

    public void updateTasks() {
        LocalDateTime baseline = getBaseline();
        updateTasks(baseline);
        updateSubscriptions(baseline);
    }

    public LocalDateTime getBaseline() {
        return LocalDateTime.now();
    }

    private void updateTasks(LocalDateTime baseline) {
        departmentService.findAllIdsForTaskNotification(baseline)
            .forEach(departmentId -> departmentService.updateTasks(departmentId, baseline));
    }

    private void updateSubscriptions(LocalDateTime baseline) {
        departmentService.updateSubscriptions(baseline);
        departmentService.findAllIdsForSubscribeNotification(baseline)
            .forEach(departmentId -> departmentService.sendSubscribeNotification(departmentId));
        departmentService.findAllIdsForSuspendNotification(baseline)
            .forEach(departmentId -> departmentService.sendSuspendNotification(departmentId));
    }

}
