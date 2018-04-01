package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.ResourceTaskCompletion;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Notification;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.repository.ResourceTaskCompletionRepository;
import hr.prism.board.repository.ResourceTaskRepository;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.workflow.Activity;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "WeakerAccess"})
public class ResourceTaskService {

    private static final String SEPARATOR = ", ";

    @SuppressWarnings("SqlResolve")
    private static final String INSERT_RESOURCE_TASK = "INSERT INTO resource_task (resource_id, task, creator_id, created_timestamp, updated_timestamp) VALUES ";

    private static final List<hr.prism.board.enums.ResourceTask> CREATE_TASKS = Arrays.asList(hr.prism.board.enums.ResourceTask.CREATE_MEMBER,
        hr.prism.board.enums.ResourceTask.CREATE_POST, hr.prism.board.enums.ResourceTask.DEPLOY_BADGE);

    @Value("${resource.task.notification.interval1.seconds}")
    private Long resourceTaskNotificationInterval1Seconds;

    @Value("${resource.task.notification.interval2.seconds}")
    private Long resourceTaskNotificationInterval2Seconds;

    @Value("${resource.task.notification.interval3.seconds}")
    private Long resourceTaskNotificationInterval3Seconds;

    @Inject
    private ResourceTaskRepository resourceTaskRepository;

    @Inject
    private ResourceTaskCompletionRepository resourceTaskCompletionRepository;

    @Inject
    private ActivityService activityService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private EntityManager entityManager;

    public ResourceTask findOne(Long id) {
        return resourceTaskRepository.findOne(id);
    }

    public ArrayListMultimap<Long, ResourceTask> getTasks(Collection<Long> resourceIds, User user) {
        List<ResourceTask> tasks = resourceTaskRepository.findByResource(resourceIds);
        List<ResourceTask> completedTasks = resourceTaskRepository.findCompletionsByResource(resourceIds, user);

        ArrayListMultimap<Long, ResourceTask> response = ArrayListMultimap.create();
        tasks.forEach(task -> {
            task.setCompletedForUser(completedTasks.contains(task));
            response.put(task.getResource().getId(), task);
        });

        return response;
    }

    public List<hr.prism.board.enums.ResourceTask> getTasks(Resource resource, User user) {
        return resourceTaskRepository.findByResource(resource, user);
    }

    public void createForNewResource(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        insertResourceTasks(resourceId, userId, tasks);
    }

    public void createForExistingResource(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskCompletionRepository.deleteByResourceId(resourceId);
        resourceTaskRepository.deleteByResourceId(resourceId);
        insertResourceTasks(resourceId, userId, tasks);
    }

    public void completeTasks(Resource resource, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskRepository.updateByResourceAndTasks(resource, tasks, true);
        entityManager.flush();

        if (resourceTaskRepository.findByResourceAndNotCompleted(resource).isEmpty()) {
            activityService.deleteActivities(resource, Arrays.asList(hr.prism.board.enums.Activity.CREATE_TASK_ACTIVITY, hr.prism.board.enums.Activity.UPDATE_TASK_ACTIVITY));
            activityService.sendActivities(resource);
        }
    }

    public void createCompletion(User user, Long taskId) {
        ResourceTask resourceTask = resourceTaskRepository.findOne(taskId);
        ResourceTaskCompletion completion = resourceTaskCompletionRepository.findByResourceTaskAndUser(resourceTask, user);
        if (completion == null) {
            resourceTaskCompletionRepository.save(new ResourceTaskCompletion().setResourceTask(resourceTask).setUser(user));
            entityManager.flush();

            if (resourceTaskRepository.findByResourceAndNotCompleted(resourceTask.getResource(), user).isEmpty()) {
                Long userId = user.getId();
                activityService.dismissActivities(resourceTask.getResource().getId(),
                    ImmutableList.of(hr.prism.board.enums.Activity.CREATE_TASK_ACTIVITY, hr.prism.board.enums.Activity.UPDATE_TASK_ACTIVITY), userId);
                activityService.sendActivities(userId);
            }
        }
    }

    public ArrayListMultimap<Pair<Long, Integer>, hr.prism.board.enums.ResourceTask> getResourceTasks(LocalDateTime baseline) {
        LocalDateTime baseline1 = baseline.minusSeconds(resourceTaskNotificationInterval1Seconds);
        LocalDateTime baseline2 = baseline.minusSeconds(resourceTaskNotificationInterval2Seconds);
        LocalDateTime baseline3 = baseline.minusSeconds(resourceTaskNotificationInterval3Seconds);

        ArrayListMultimap<Pair<Long, Integer>, hr.prism.board.enums.ResourceTask> resourceTasks = ArrayListMultimap.create();
        resourceTaskRepository.findByNotificationHistory(1, 2, baseline1, baseline2, baseline3)
            .forEach(resourceTask -> resourceTasks.put(
                Pair.of(resourceTask.getResource().getId(), resourceTask.getNotifiedCount()), resourceTask.getTask()));
        return resourceTasks;
    }

    public void sendNotification(Pair<Long, Integer> resource, List<hr.prism.board.enums.ResourceTask> tasks) {
        Long resourceId = resource.getKey();
        Integer notifiedCount = resource.getValue();
        String notificationContext = CREATE_TASKS.contains(tasks.get(0)) ? "CREATE" : "UPDATE";
        if (notifiedCount == null) {
            Activity activity = new Activity().setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR)
                .setActivity(hr.prism.board.enums.Activity.valueOf(notificationContext + "_TASK_ACTIVITY"));
            activityEventService.publishEvent(this, resourceId, false, Collections.singletonList(activity));
        }

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR).setNotification(Notification.valueOf(notificationContext + "_TASK_NOTIFICATION"));

        notificationEventService.publishEvent(this, resourceId, tasks, Collections.singletonList(notification));
        resourceTaskRepository.updateNotifiedCountByResourceId(resourceId);
    }

    private void insertResourceTasks(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        String insert = INSERT_RESOURCE_TASK + tasks.stream()
            .map(task -> "(:resourceId, '" + task.name() + "', :creatorId, :baseline, :baseline)").collect(Collectors.joining(SEPARATOR));

        Query query = entityManager.createNativeQuery(insert);
        query.setParameter("resourceId", resourceId);
        query.setParameter("creatorId", userId);
        query.setParameter("baseline", LocalDateTime.now());

        query.executeUpdate();
        entityManager.flush();
    }

}
