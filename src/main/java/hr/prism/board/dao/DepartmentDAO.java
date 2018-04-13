package hr.prism.board.dao;

import hr.prism.board.enums.State;
import hr.prism.board.value.DepartmentSearch;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static hr.prism.board.enums.Scope.DEPARTMENT;

@Repository
@Transactional
public class DepartmentDAO {

    private final EntityManager entityManager;

    @Inject
    public DepartmentDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<DepartmentSearch> findDepartments(Long universityId, String searchTerm) {
        return entityManager.createNamedQuery("departmentSearch", DepartmentSearch.class)
            .setParameter("searchTermHard", searchTerm + "%")
            .setParameter("searchTermSoft", searchTerm)
            .setParameter("universityId", universityId)
            .setParameter("scope", DEPARTMENT.name())
            .setParameter("state", State.ACCEPTED.name())
            .getResultList();
    }

    public List<String> findDepartmentPrograms(Long departmentId, String searchTerm) {
        return entityManager.createNamedQuery("programSearch", String.class)
            .setParameter("searchTermHard", searchTerm + "%")
            .setParameter("searchTermSoft", searchTerm)
            .setParameter("departmentId", departmentId)
            .getResultList();
    }

}
