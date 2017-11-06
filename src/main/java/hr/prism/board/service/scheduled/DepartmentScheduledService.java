package hr.prism.board.service.scheduled;

import hr.prism.board.service.DepartmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
public class DepartmentScheduledService {

    @Inject
    private DepartmentService departmentService;

    // FIXME: persist last run time so we don't run it twice if system restarted
    // Probably used scheduled job e.g. once / hour to check against date - impossible to test cron
    @Scheduled(cron = "0 0 7 1 9 ?")
    public void updateTasks() {
        LocalDateTime baseline = LocalDateTime.now().minusMonths(1);
        departmentService.findAllIds(baseline).forEach(departmentId -> departmentService.updateTasks(departmentId, baseline));
    }

}
