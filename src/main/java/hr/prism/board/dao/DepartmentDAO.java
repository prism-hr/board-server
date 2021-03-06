package hr.prism.board.dao;

import hr.prism.board.value.ResourceSearch;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@Repository
@Transactional
public class DepartmentDAO {

    private final EntityManager entityManager;

    @Inject
    public DepartmentDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public List<ResourceSearch> findDepartments(Long universityId, String searchTerm) {
        return entityManager.createNamedQuery("departmentSearch", ResourceSearch.class)
            .setParameter("searchTermHard", searchTerm + "%")
            .setParameter("searchTermSoft", searchTerm)
            .setParameter("universityId", universityId)
            .getResultList();
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public List<String> findDepartmentPrograms(Long departmentId, String searchTerm) {
        return entityManager.createNamedQuery("programSearch", String.class)
            .setParameter("searchTermHard", searchTerm + "%")
            .setParameter("searchTermSoft", searchTerm)
            .setParameter("departmentId", departmentId)
            .getResultList();
    }

}
