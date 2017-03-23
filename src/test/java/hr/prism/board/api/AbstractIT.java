package hr.prism.board.api;

import org.junit.After;
import org.junit.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public abstract class AbstractIT {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private PlatformTransactionManager platformTransactionManager;
    
    protected TransactionTemplate transactionTemplate;
    
    @Before
    public void setUp() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }
    
    @After
    public void after() {
        transactionTemplate.execute(transactionTemplate -> {
            Query removeForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();
            
            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();
            tablesNames.stream().filter(tableName -> !tableName.equals("schema_version")).forEach(tableName -> {
                Query truncateTable = entityManager.createNativeQuery("TRUNCATE TABLE " + tableName);
                truncateTable.executeUpdate();
            });
            
            Query restoreForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();
            return null;
        });
    }
    
}
