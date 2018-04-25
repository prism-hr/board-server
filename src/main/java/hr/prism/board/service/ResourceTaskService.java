package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.ResourceTaskDAO;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.enums.Notification;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.repository.ResourceTaskRepository;
import hr.prism.board.workflow.Activity;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.Activity.CREATE_TASK_ACTIVITY;
import static hr.prism.board.enums.Activity.UPDATE_TASK_ACTIVITY;
import static hr.prism.board.enums.Notification.CREATE_TASK_NOTIFICATION;
import static hr.prism.board.enums.Notification.UPDATE_TASK_NOTIFICATION;
import static hr.prism.board.enums.ResourceTask.DEPARTMENT_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static java.util.Collections.singletonList;

@Service
@Transactional
public class ResourceTaskService {

    private static final List<hr.prism.board.enums.Activity> TASK_ACTIVITIES =
        ImmutableList.of(CREATE_TASK_ACTIVITY, UPDATE_TASK_ACTIVITY);

    private final Long resourceTaskNotificationInterval1Seconds;

    private final Long resourceTaskNotificationInterval2Seconds;

    private final Long resourceTaskNotificationInterval3Seconds;

    private final ResourceTaskRepository resourceTaskRepository;

    private final ResourceTaskDAO resourceTaskDAO;

    private final ActivityService activityService;

    private final EventProducer eventProducer;

    private final EntityManager entityManager;

    @Inject
    public ResourceTaskService(
        @Value("${resource.task.notification.interval1.seconds}") Long resourceTaskNotificationInterval1Seconds,
        @Value("${resource.task.notification.interval2.seconds}") Long resourceTaskNotificationInterval2Seconds,
        @Value("${resource.task.notification.interval3.seconds}") Long resourceTaskNotificationInterval3Seconds,
        ResourceTaskRepository resourceTaskRepository, ResourceTaskDAO resourceTaskDAO, ActivityService activityService,
        EventProducer eventProducer, EntityManager entityManager) {
        this.resourceTaskNotificationInterval1Seconds = resourceTaskNotificationInterval1Seconds;
        this.resourceTaskNotificationInterval2Seconds = resourceTaskNotificationInterval2Seconds;
        this.resourceTaskNotificationInterval3Seconds = resourceTaskNotificationInterval3Seconds;
        this.resourceTaskRepository = resourceTaskRepository;
        this.resourceTaskDAO = resourceTaskDAO;
        this.activityService = activityService;
        this.eventProducer = eventProducer;
        this.entityManager = entityManager;
    }

    public ResourceTask getById(Long id) {
        return resourceTaskRepository.findOne(id);
    }

    public List<ResourceTask> getByResourceId(Long resourceId) {
        return resourceTaskRepository.findByResourceId(resourceId);
    }

    public List<hr.prism.board.enums.ResourceTask> getByResource(Resource resource) {
        return resourceTaskRepository.findByResource(resource);
    }

    public void createForNewResource(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskDAO.insertResourceTasks(resourceId, userId, tasks);
    }

    public void createForExistingResource(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskRepository.deleteByResourceId(resourceId);
        resourceTaskDAO.insertResourceTasks(resourceId, userId, tasks);
    }

    public void completeTasks(Resource resource, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskRepository.updateByResourceAndTasks(resource, tasks, true);
        entityManager.flush();

        if (resourceTaskRepository.findByResourceAndNotCompleted(resource).isEmpty()) {
            activityService.deleteActivities(resource, TASK_ACTIVITIES);
            eventProducer.produce(
                new ActivityEvent(this, resource.getId()));
        }
    }

    public ArrayListMultimap<Pair<Long, Integer>, hr.prism.board.enums.ResourceTask> getResourceTasks(
        LocalDateTime baseline) {
        LocalDateTime baseline1 = baseline.minusSeconds(resourceTaskNotificationInterval1Seconds);
        LocalDateTime baseline2 = baseline.minusSeconds(resourceTaskNotificationInterval2Seconds);
        LocalDateTime baseline3 = baseline.minusSeconds(resourceTaskNotificationInterval3Seconds);

        ArrayListMultimap<Pair<Long, Integer>, hr.prism.board.enums.ResourceTask> resourceTasks =
            ArrayListMultimap.create();

        resourceTaskRepository.findByNotificationHistory(
            1, 2, baseline1, baseline2, baseline3)
            .forEach(resourceTask -> resourceTasks.put(
                Pair.of(resourceTask.getResource().getId(), resourceTask.getNotifiedCount()), resourceTask.getTask()));

        return resourceTasks;
    }

    public void sendNotification(Pair<Long, Integer> resource, List<hr.prism.board.enums.ResourceTask> tasks) {
        Long resourceId = resource.getKey();
        Integer notifiedCount = resource.getValue();

        hr.prism.board.enums.Activity activity = CREATE_TASK_ACTIVITY;
        Notification notification = CREATE_TASK_NOTIFICATION;
        if (tasks.stream().noneMatch(DEPARTMENT_TASKS::contains)) {
            activity = UPDATE_TASK_ACTIVITY;
            notification = UPDATE_TASK_NOTIFICATION;
        }

        if (notifiedCount == null) {
            eventProducer.produce(
                new ActivityEvent(this, resourceId,
                    singletonList(
                        new Activity()
                            .setScope(DEPARTMENT)
                            .setRole(ADMINISTRATOR)
                            .setActivity(activity))));
        }

        eventProducer.produce(
            new NotificationEvent(this, resourceId, tasks,
                singletonList(
                    new hr.prism.board.workflow.Notification()
                        .setScope(DEPARTMENT)
                        .setRole(ADMINISTRATOR)
                        .setNotification(notification))));

        resourceTaskRepository.updateNotifiedCountByResourceId(resourceId);
    }

}
