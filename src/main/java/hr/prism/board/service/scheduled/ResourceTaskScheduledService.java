package hr.prism.board.service.scheduled;

import hr.prism.board.service.ResourceTaskService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

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
            resourceTaskService.updateTasks();
        }
    }
    
}
