package hr.prism.board.dao;

import hr.prism.board.value.PostStatistics;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@Repository
@Transactional
public class PostDAO {

    private final EntityManager entityManager;

    @Inject
    public PostDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public PostStatistics getPostStatistics(Long departmentId) {
        return entityManager.createNamedQuery("postStatistics", PostStatistics.class)
            .setParameter("departmentId", departmentId)
            .getSingleResult();
    }

}
