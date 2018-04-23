package hr.prism.board.dao;

import hr.prism.board.value.ResourceSearch;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@Repository
@Transactional
public class UniversityDAO {

    private final EntityManager entityManager;

    @Inject
    public UniversityDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public List<ResourceSearch> findUniversities(String searchTerm) {
        return entityManager.createNamedQuery("departmentSearch", ResourceSearch.class)
            .setParameter("searchTermHard", searchTerm + "%")
            .setParameter("searchTermSoft", searchTerm)
            .getResultList();
    }

}
