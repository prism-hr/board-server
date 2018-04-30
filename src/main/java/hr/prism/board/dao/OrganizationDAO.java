package hr.prism.board.dao;

import hr.prism.board.value.OrganizationSearch;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@Repository
@Transactional
public class OrganizationDAO {

    private final EntityManager entityManager;

    @Inject
    public OrganizationDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public List<OrganizationSearch> findOrganizations(String searchTerm) {
        return entityManager.createNamedQuery("searchOrganizations", OrganizationSearch.class)
            .setParameter("searchTermHard", searchTerm + "%")
            .setParameter("searchTermSoft", searchTerm)
            .getResultList();
    }

}
