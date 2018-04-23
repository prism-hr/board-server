package hr.prism.board.dao;

import hr.prism.board.value.UserSearch;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class UserDAO {

    private final EntityManager entityManager;

    @Inject
    public UserDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQueryApiInspection")
    public List<UserSearch> findUsers(String searchTerm) {
        return entityManager.createNamedQuery("userSearch", UserSearch.class)
            .setParameter("searchTerm", searchTerm + "%")
            .getResultList();
    }

}
