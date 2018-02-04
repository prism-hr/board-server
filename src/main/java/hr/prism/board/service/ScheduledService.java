package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.enums.ResourceTask;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class ScheduledService {

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Inject
    private ActivityService activityService;

    @Inject
    private PostService postService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private ResourceService resourceService;

    @Scheduled(initialDelay = 60000, fixedDelay = 10000)
    public void processActivities() throws IOException {
        if (BooleanUtils.isTrue(schedulerOn)) {
            activityService.updateActivities();
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 10000)
    public void processResources() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            // Publish / retire posts
            LocalDateTime baseline = getBaseline();
            postService.publishAndRetirePosts(baseline);

            // Notify department tasks
            notifyDepartmentTasks(baseline);

            // Update department subscriptions
            updateDepartmentSubscriptions(baseline);

            // Update department tasks
            updateDepartmentTasks(baseline);

            // Archive expired resources
            resourceService.archiveResources();
        }
    }

    public void notifyDepartmentTasks(LocalDateTime baseline) {
        ArrayListMultimap<Pair<Long, Integer>, ResourceTask> resourceTasks = resourceTaskService.getResourceTasks(baseline);
        resourceTasks.keySet().forEach(resourceId -> resourceTaskService.sendNotification(resourceId, resourceTasks.get(resourceId)));
    }

    public void updateDepartmentSubscriptions(LocalDateTime baseline) {
        departmentService.updateSubscriptions(baseline);
        departmentService.findAllIdsForSubscribeNotification(baseline)
            .forEach(departmentId -> departmentService.sendSubscribeNotification(departmentId));
        departmentService.findAllIdsForSuspendNotification(baseline)
            .forEach(departmentId -> departmentService.sendSuspendNotification(departmentId));
    }

    public void updateDepartmentTasks(LocalDateTime baseline) {
        departmentService.findAllIdsForTaskUpdates(baseline)
            .forEach(departmentId -> departmentService.updateTasks(departmentId, baseline));
    }

    public LocalDateTime getBaseline() {
        return LocalDateTime.now();
    }

}
