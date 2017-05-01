package hr.prism.board.api;

import com.google.common.collect.Lists;
import hr.prism.board.definition.DocumentDefinition;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.service.ActionService;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.TestUserService;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public abstract class AbstractIT {
    
    @Inject
    BoardApi boardApi;
    
    @Inject
    PostApi postApi;
    
    @Inject
    ActionService actionService;
    
    @Inject
    BoardService boardService;
    
    @Inject
    ResourceService resourceService;
    
    @Inject
    TestUserService testUserService;
    
    TransactionTemplate transactionTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private PlatformTransactionManager platformTransactionManager;
    
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
    
    List<User> makeUnprivilegedUsers(Long departmentId, Long boardId, PostDTO samplePost) {
        // Other department and board
        List<User> unprivilegedUsers = Lists.newArrayList(testUserService.authenticate());
        transactionTemplate.execute(transactionStatus -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("other board")
                    .setDepartment(new DepartmentDTO()
                        .setName("other department")));
            return null;
        });
        
        // Same department other board
        unprivilegedUsers.add(testUserService.authenticate());
        transactionTemplate.execute(transactionStatus -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("sibling board")
                    .setDepartment(new DepartmentDTO()
                        .setId(departmentId)));
            return null;
        });
        
        // Same board other post
        unprivilegedUsers.add(testUserService.authenticate());
        transactionTemplate.execute(transactionStatus -> postApi.postPost(boardId, samplePost));
        return unprivilegedUsers;
    }
    
    void verifyResourceActions(Scope scope, Long id, ForbiddenActionExecutor forbiddenActionExecutor, Action... expectedActions) {
        User user = null;
        verifyResourceActions(user, scope, id, forbiddenActionExecutor, expectedActions);
    }
    
    void verifyResourceActions(Collection<User> users, Scope scope, Long id, ForbiddenActionExecutor forbiddenActionExecutor, Action... expectedActions) {
        users.forEach(user -> verifyResourceActions(user, scope, id, forbiddenActionExecutor, expectedActions));
    }
    
    void verifyResourceActions(User user, Scope scope, Long id, ForbiddenActionExecutor forbiddenActionExecutor, Action... expectedActions) {
        ExceptionCode exceptionCode;
        if (user == null) {
            testUserService.unauthenticate();
            exceptionCode = ExceptionCode.UNAUTHENTICATED_USER;
        } else {
            testUserService.setAuthentication(user.getStormpathId());
            exceptionCode = ExceptionCode.FORBIDDEN_ACTION;
        }
        
        Resource resource = resourceService.getResource(user, scope, id);
        assertThat(resource.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()), Matchers.containsInAnyOrder(expectedActions));
        for (Action action : Action.values()) {
            if (!ArrayUtils.contains(expectedActions, action)) {
                transactionTemplate.execute(status -> {
                    ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> forbiddenActionExecutor.execute(id, action), exceptionCode, status);
                    return null;
                });
            }
        }
    }
    
    void verifyDocument(DocumentDefinition documentDefinition, DocumentRepresentation documentRepresentation) {
        if (documentDefinition == null) {
            assertNull(documentRepresentation);
        } else {
            assertEquals(documentDefinition.getFileName(), documentRepresentation.getFileName());
            assertEquals(documentDefinition.getCloudinaryId(), documentRepresentation.getCloudinaryId());
            assertEquals(documentDefinition.getCloudinaryUrl(), documentRepresentation.getCloudinaryUrl());
        }
    }
    
    interface ForbiddenActionExecutor {
        
        void execute(Long id, Action action);
        
    }
    
}
