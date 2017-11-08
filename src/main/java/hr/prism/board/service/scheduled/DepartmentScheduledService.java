package hr.prism.board.service.scheduled;

import hr.prism.board.service.DepartmentService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
public class DepartmentScheduledService {

    @Value("${scheduler.on}")
    private Boolean schedulerOn;


    @Inject
    private DepartmentService departmentService;

    @Scheduled(initialDelay = 60000, fixedDelay = 86400000)
    public void updateTasks() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            LocalDateTime baseline = LocalDateTime.now();
            LocalDateTime baseline1 = baseline.minusMonths(1);

            LocalDateTime baseline2;
            if (baseline.getMonth().getValue() > 8) {
                baseline2 = LocalDateTime.of(baseline.getYear(), 9, 1, 0, 0);
            } else {
                baseline2 = LocalDateTime.of(baseline.getYear() - 1, 9, 1, 0, 0);
            }

            departmentService.findAllIds(baseline1, baseline2).forEach(departmentId -> departmentService.updateTasks(departmentId, baseline));
        }
    }

}
