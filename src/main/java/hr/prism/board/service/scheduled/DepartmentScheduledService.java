package hr.prism.board.service.scheduled;

import hr.prism.board.service.DepartmentService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class DepartmentScheduledService {

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Inject
    private DepartmentService departmentService;

    // TODO: state change logic
    @Scheduled(initialDelay = 60000, fixedDelay = 86400000)
    public void updateState() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            departmentService.getDepartmentsToMoveToPendingOrRejected(null, null).forEach(departmentId -> {

            });
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 86400000)
    public void updateTasks() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            departmentService.updateTasks();
        }
    }

}
