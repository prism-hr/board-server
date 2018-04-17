package hr.prism.board.dao;

import hr.prism.board.value.Statistics;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@Repository
@Transactional
public class UserRoleDAO {

    private final EntityManager entityManager;

    @Inject
    public UserRoleDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Statistics getMemberStatistics(Long departmentId) {
        return (Statistics) entityManager.createNamedQuery("memberStatistics")
            .setParameter("departmentId", departmentId)
            .getSingleResult();
    }

}
