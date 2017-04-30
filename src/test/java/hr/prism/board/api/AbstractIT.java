package hr.prism.board.api;

import com.google.common.collect.Lists;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.service.TestUserService;
import org.junit.After;
import org.junit.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractIT {
    
    @Inject
    BoardApi boardApi;
    
    @Inject
    PostApi postApi;
    
    @Inject
    TestUserService testUserService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private PlatformTransactionManager platformTransactionManager;
    
    protected TransactionTemplate transactionTemplate;
    
    @Before
    public void before() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }
    
    @After
    @SuppressWarnings("unchecked")
    public void after() {
        transactionTemplate.execute(transactionTemplate -> {
            Query removeForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();
            
            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();
            tablesNames.stream().filter(tableName -> !Arrays.asList("schema_version", "permission").contains(tableName)).forEach(tableName -> {
                Query truncateTable = entityManager.createNativeQuery("TRUNCATE TABLE " + tableName);
                truncateTable.executeUpdate();
            });
            
            Query restoreForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();
            return null;
        });
    }
    
    List<User> makeUnprivilegedUsers(Long departmentId, Long boardId) {
        List<User> otherUsers = Lists.newArrayList(testUserService.authenticate());
        transactionTemplate.execute(transactionStatus -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("other board")
                    .setDepartment(new DepartmentDTO()
                        .setName("other department")));
            return null;
        });
        
        otherUsers.add(testUserService.authenticate());
        transactionTemplate.execute(transactionStatus -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("sibling board")
                    .setDepartment(new DepartmentDTO()
                        .setId(departmentId)));
            return null;
        });
        
        otherUsers.add(testUserService.authenticate());
        transactionTemplate.execute(transactionStatus -> postApi.postPost(boardId, TestHelper.smallSamplePost()));
        return otherUsers;
    }
    
    void verifyUnprivilegedUsers(List<User> unprivilegedUsers, Runnable operation) {
        unprivilegedUsers.stream().map(User::getStormpathId).forEach(stormpathId -> {
            testUserService.setAuthentication(stormpathId);
            transactionTemplate.execute(status -> {
                ExceptionUtil.verifyApiException(ApiForbiddenException.class, operation, ExceptionCode.FORBIDDEN_ACTION, status);
                return null;
            });
        });
        
        // Verify that a stranger is also unprivileged
        testUserService.unauthenticate();
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, operation, ExceptionCode.UNAUTHENTICATED_USER, status);
            return null;
        });
    }
    
}
