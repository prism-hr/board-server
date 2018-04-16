package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.enums.ResourceTask;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

@Service
public class ScheduledService {

    private final boolean schedulerOn;

    private final ActivityService activityService;

    private final PostService postService;

    private final ResourceTaskService resourceTaskService;

    private final DepartmentService departmentService;

    public ScheduledService(@Value("${scheduler.on}") boolean schedulerOn, ActivityService activityService,
                            PostService postService, ResourceTaskService resourceTaskService,
                            DepartmentService departmentService) {
        this.schedulerOn = schedulerOn;
        this.activityService = activityService;
        this.postService = postService;
        this.resourceTaskService = resourceTaskService;
        this.departmentService = departmentService;
    }

    @SuppressWarnings("unused")
    @Scheduled(initialDelay = 60000, fixedDelay = 10000)
    public void processActivities() throws IOException {
        if (isTrue(schedulerOn)) {
            activityService.updateActivities();
        }
    }

    @SuppressWarnings("unused")
    @Scheduled(initialDelay = 60000, fixedDelay = 10000)
    public void processResources() {
        if (isTrue(schedulerOn)) {
            // Publish / retire posts
            LocalDateTime baseline = getBaseline();
            postService.publishAndRetirePosts(baseline);

            // Notify department tasks
            notifyDepartmentTasks(baseline);

            // Update department subscriptions
            updateDepartmentSubscriptions(baseline);

            // Update department tasks
            updateDepartmentTasks(baseline);

            // Archive expired posts
            postService.archivePosts();
        }
    }

    public void notifyDepartmentTasks(LocalDateTime baseline) {
        ArrayListMultimap<Pair<Long, Integer>, ResourceTask> resourceTasks =
            resourceTaskService.getResourceTasks(baseline);
        resourceTasks.keySet().forEach(resourceId ->
            resourceTaskService.sendNotification(resourceId, resourceTasks.get(resourceId)));
    }

    public void updateDepartmentSubscriptions(LocalDateTime baseline) {
        departmentService.updateSubscriptions(baseline);
        departmentService.findAllIdsForSubscribeNotification(baseline)
            .forEach(departmentService::sendSubscribeNotification);
    }

    public void updateDepartmentTasks(LocalDateTime baseline) {
        departmentService.findAllIdsForTaskUpdates(baseline)
            .forEach(departmentId -> departmentService.updateTasks(departmentId, baseline));
    }

    public LocalDateTime getBaseline() {
        return LocalDateTime.now();
    }

}
