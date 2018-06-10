package hr.prism.board.service;

import hr.prism.board.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class TestUserService {

    private static final Logger LOGGER = getLogger(TestUserService.class);

    private final UserRepository userRepository;

    private final EntityManager entityManager;

    @Inject
    public TestUserService(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @SuppressWarnings({"unchecked", "SqlResolve"})
    public void deleteTestUsers() {
        List<Long> userIds = userRepository.findByTestUser(true);
        if (!userIds.isEmpty()) {
            entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0")
                .executeUpdate();

            List<String> tablesNames =
                entityManager.createNativeQuery("SHOW TABLES")
                    .getResultList();

            tablesNames
                .forEach(tableName -> {
                    List<Object[]> columns =
                        entityManager.createNativeQuery("SHOW COLUMNS FROM " + tableName)
                            .getResultList();

                    if (columns.stream()
                        .anyMatch(column -> "creator_id".equals(column[0]))) {
                        LOGGER.info("Deleting test users from table: " + tableName);

                        Query deleteUserData = entityManager.createNativeQuery(
                            "DELETE FROM " + tableName + " WHERE creator_id IN (:userIds)");

                        deleteUserData.setParameter("userIds", userIds);
                        deleteUserData.executeUpdate();
                    }
                });

            entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1")
                .executeUpdate();
        }
    }

}
