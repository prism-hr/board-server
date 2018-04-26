package hr.prism.board.dao;

import hr.prism.board.enums.ResourceTask;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Repository
@Transactional
public class ResourceTaskDAO {

    private static final String INSERT_RESOURCE_TASK =
        "INSERT INTO resource_task (resource_id, task, creator_id, created_timestamp, updated_timestamp) VALUES ";

    private static final String SEPARATOR = ", ";

    private final EntityManager entityManager;

    @Inject
    public ResourceTaskDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insertResourceTasks(Long resourceId, Long userId, List<ResourceTask> tasks) {
        String insert = INSERT_RESOURCE_TASK +
            tasks.stream()
                .map(task -> "(:resourceId, '" + task.name() + "', :creatorId, :baseline, :baseline)")
                .collect(joining(SEPARATOR));

        Query query = entityManager.createNativeQuery(insert);
        query.setParameter("resourceId", resourceId);
        query.setParameter("creatorId", userId);
        query.setParameter("baseline", LocalDateTime.now());

        query.executeUpdate();
        entityManager.flush();
    }

}
