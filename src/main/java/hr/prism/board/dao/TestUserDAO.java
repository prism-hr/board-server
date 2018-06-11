package hr.prism.board.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
@Transactional
public class TestUserDAO {

    private final EntityManager entityManager;

    public TestUserDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void disableForeignKeyChecks() {
        entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0")
            .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<String> getTablesWithCreatorIdColumn() {
        List<String> tablesNames =
            entityManager.createNativeQuery("SHOW TABLES")
                .getResultList();

        return tablesNames.stream()
            .filter(this::hasCreatorIdColumn)
            .collect(toList());
    }

    @SuppressWarnings("SqlResolve")
    public void deleteRecords(String tableName, List<Long> userIds) {
        Query deleteUserData = entityManager.createNativeQuery(
            "DELETE FROM " + tableName + " WHERE creator_id IN (:userIds)");

        deleteUserData.setParameter("userIds", userIds);
        deleteUserData.executeUpdate();
    }

    public void enableForeignKeyChecks() {
        entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1")
            .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private boolean hasCreatorIdColumn(String tableName) {
        List<Object[]> columns =
            entityManager.createNativeQuery("SHOW COLUMNS FROM " + tableName)
                .getResultList();

        return columns.stream()
            .anyMatch(column -> "creator_id".equals(column[0]));
    }

}
