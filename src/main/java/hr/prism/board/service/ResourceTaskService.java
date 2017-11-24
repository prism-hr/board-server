package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.ResourceTaskSuppression;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Notification;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.repository.ResourceTaskRepository;
import hr.prism.board.repository.ResourceTaskSuppressionRepository;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.workflow.Activity;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceTaskService {

    private static final String SEPARATOR = ", ";

    @SuppressWarnings("SqlResolve")
    private static final String INSERT_RESOURCE_TASK = "INSERT INTO resource_task (resource_id, task, creator_id, created_timestamp) VALUES ";

    private static final List<hr.prism.board.enums.ResourceTask> CREATE_TASKS = Arrays.asList(hr.prism.board.enums.ResourceTask.CREATE_MEMBER,
        hr.prism.board.enums.ResourceTask.CREATE_INTERNAL_POST, hr.prism.board.enums.ResourceTask.DEPLOY_BADGE);

    @Inject
    private ResourceTaskRepository resourceTaskRepository;

    @Inject
    private ResourceTaskSuppressionRepository resourceTaskSuppressionRepository;

    @Inject
    private ActivityService activityService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public ResourceTask findOne(Long id) {
        return resourceTaskRepository.findOne(id);
    }

    public List<hr.prism.board.enums.ResourceTask> findByResource(Resource resource, User user) {
        return resourceTaskRepository.findByResource(resource, user);
    }

    public ArrayListMultimap<Pair<Long, Integer>, Pair<Long, hr.prism.board.enums.ResourceTask>> getResourceTasks(
        LocalDateTime baseline1, LocalDateTime baseline2, LocalDateTime baseline3) {
        ArrayListMultimap<Pair<Long, Integer>, Pair<Long, hr.prism.board.enums.ResourceTask>> resourceTasks = ArrayListMultimap.create();
        resourceTaskRepository.findByNotificationHistory(1, 2, baseline1, baseline2, baseline3)
            .forEach(resourceTask -> resourceTasks.put(
                Pair.of(resourceTask.getResource().getId(), resourceTask.getNotifiedCount()),
                Pair.of(resourceTask.getId(), resourceTask.getTask())));
        return resourceTasks;
    }

    public void sendNotification(Pair<Long, Integer> resource, List<Pair<Long, hr.prism.board.enums.ResourceTask>> tasks) {
        Long resourceId = resource.getKey();
        Integer notifiedCount = resource.getValue();
        String notificationContext = CREATE_TASKS.contains(tasks.get(0).getValue()) ? "CREATE" : "UPDATE";
        if (notifiedCount == null) {
            tasks.forEach(task -> {
                Activity activity = new Activity().setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR)
                    .setActivity(hr.prism.board.enums.Activity.valueOf(notificationContext + "_TASK_ACTIVITY"));
                activityEventService.publishEvent(this, resourceId, Collections.singletonList(activity));
            });
        }

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR).setNotification(Notification.valueOf(notificationContext + "_TASK_NOTIFICATION"));

        notificationEventService.publishEvent(this, resourceId, tasks.stream().map(Pair::getValue).collect(Collectors.toList()), Collections.singletonList(notification));
        resourceTaskRepository.updateNotifiedCountByResourceId(resourceId);
    }

    void createForNewResource(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        insertResourceTasks(resourceId, userId, tasks);
    }

    void createForExistingResource(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskSuppressionRepository.deleteByResourceId(resourceId);
        resourceTaskRepository.deleteByResourceId(resourceId);
        insertResourceTasks(resourceId, userId, tasks);
    }

    List<ResourceTask> findBySuppressions(User user) {
        return resourceTaskRepository.findBySuppressions(user);
    }

    List<ResourceTask> findByResourceAndSuppressions(Resource resource, User user) {
        return resourceTaskRepository.findByResourceAndSuppressions(resource, user);
    }

    void completeTasks(Resource resource, List<hr.prism.board.enums.ResourceTask> tasks) {
        activityService.deleteActivities(resource, tasks);
        resourceTaskRepository.updateByResourceAndTasks(resource, tasks, true);
    }

    void createSuppression(User user, Long taskId) {
        ResourceTask task = resourceTaskRepository.findOne(taskId);
        ResourceTaskSuppression suppression = resourceTaskSuppressionRepository.findByResourceTaskAndUser(task, user);
        if (suppression == null) {
            resourceTaskSuppressionRepository.save(new ResourceTaskSuppression().setResourceTask(task).setUser(user));
        }
    }

    private void insertResourceTasks(Long resourceId, Long userId, List<hr.prism.board.enums.ResourceTask> tasks) {
        String insert = INSERT_RESOURCE_TASK + tasks.stream()
            .map(task -> "(:resourceId, '" + task.name() + "', :creatorId, :baseline)").collect(Collectors.joining(SEPARATOR));
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            Query query = entityManager.createNativeQuery(insert);
            query.setParameter("resourceId", resourceId);
            query.setParameter("creatorId", userId);
            query.setParameter("baseline", LocalDateTime.now());
            return query.executeUpdate();
        });

        entityManager.flush();
    }

}
