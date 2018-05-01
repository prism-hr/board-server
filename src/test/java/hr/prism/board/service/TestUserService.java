package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
public class TestUserService {

    private static final ImmutableList<String> FRAMEWORK_TABLES = ImmutableList.of("flyway_schema_history", "workflow");

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
            Query removeForeignKeyChecks =
                entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();

            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();

            tablesNames
                .stream()
                .filter(tableName -> !FRAMEWORK_TABLES.contains(tableName))
                .forEach(tableName -> {
                    Query deleteUserData = entityManager.createNativeQuery(
                        "DELETE FROM " + tableName + " WHERE creator_id IN (:userIds)");

                    deleteUserData.setParameter("userIds", userIds);
                    deleteUserData.executeUpdate();
                });

            Query restoreForeignKeyChecks = entityManager.createNativeQuery(
                "SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();
        }
    }

}
