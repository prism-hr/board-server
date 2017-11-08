package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.ResourceTaskSuppression;
import hr.prism.board.domain.User;
import hr.prism.board.repository.ResourceTaskRepository;
import hr.prism.board.repository.ResourceTaskSuppressionRepository;
import hr.prism.board.service.event.NotificationEventService;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceTaskService {

    private static final String SEPARATOR = ", ";

    private static final String INSERT_RESOURCE_TASK = "INSERT INTO resource_task (resource_id, task, created_timestamp) VALUES ";

    @Inject
    private ResourceTaskRepository resourceTaskRepository;

    @Inject
    private ResourceTaskSuppressionRepository resourceTaskSuppressionRepository;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public List<Long> findAllIds(LocalDateTime baseline1, LocalDateTime baseline2, LocalDateTime baseline3) {
        return resourceTaskRepository.findAllIds(1, 2, baseline1, baseline2, baseline3);
    }

    public void createForNewResource(Long resourceId, List<hr.prism.board.enums.ResourceTask> tasks) {
        insertResourceTasks(resourceId, tasks);
    }

    public void createForExistingResource(Long resourceId, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskSuppressionRepository.deleteByResourceId(resourceId);
        resourceTaskRepository.deleteByResourceId(resourceId);
        insertResourceTasks(resourceId, tasks);
    }

    public List<ResourceTask> findBySuppressions(User user) {
        return resourceTaskRepository.findBySuppressions(user);
    }

    public List<ResourceTask> findByResourceAndSuppressions(Resource resource, User user) {
        return resourceTaskRepository.findByResourceAndSuppressions(resource, user);
    }

    public void deleteTasks(Resource resource, List<hr.prism.board.enums.ResourceTask> tasks) {
        resourceTaskSuppressionRepository.deleteByResourceAndTasks(resource, tasks);
        resourceTaskRepository.deleteByResourceAndTasks(resource, tasks);
    }

    public void createSuppression(User user, Long taskId) {
        ResourceTask task = resourceTaskRepository.findOne(taskId);
        ResourceTaskSuppression suppression = resourceTaskSuppressionRepository.findByResourceTaskAndUser(task, user);
        if (suppression == null) {
            resourceTaskSuppressionRepository.save(new ResourceTaskSuppression().setResourceTask(task).setUser(user));
        }
    }

    public void sendNotifications(Long taskId) {
        ResourceTask task = resourceTaskRepository.findOne(taskId);
        Integer notifiedCount = task.getNotifiedCount();

        if (notifiedCount == null) {
            // TODO: initial notification
            task.setNotifiedCount(1);
        } else if (notifiedCount == 1) {
            // TODO: follow up notification
            task.setNotifiedCount(2);
        } else if (notifiedCount == 2) {
            // TODO: final notification
            task.setNotifiedCount(3);
        }
    }

    private void insertResourceTasks(Long resourceId, List<hr.prism.board.enums.ResourceTask> tasks) {
        String insert = INSERT_RESOURCE_TASK + tasks.stream().map(task -> "(:resourceId, '" + task.name() + "', :baseline)").collect(Collectors.joining(SEPARATOR));
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            Query query = entityManager.createNativeQuery(insert);
            query.setParameter("resourceId", resourceId);
            query.setParameter("baseline", LocalDateTime.now());
            return query.executeUpdate();
        });

        entityManager.flush();
    }

}
