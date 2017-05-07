package hr.prism.board.api;

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
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.service.ActionService;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.TestUserService;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public abstract class AbstractIT {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);
    
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
    
    LinkedHashMap<Scope, User> makeUnprivilegedUsers(Long departmentId, Long boardId, int departmentSuffix, int boardSuffix, PostDTO samplePost) {
        LinkedHashMap<Scope, User> unprivilegedUsers = new LinkedHashMap<>();
        unprivilegedUsers.put(Scope.DEPARTMENT, testUserService.authenticate());
        transactionTemplate.execute(status -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("board" + departmentSuffix)
                    .setDepartment(new DepartmentDTO()
                        .setName("department" + departmentSuffix)));
            return null;
        });
        
        unprivilegedUsers.put(Scope.BOARD, testUserService.authenticate());
        transactionTemplate.execute(status -> {
            boardApi.postBoard(
                new BoardDTO()
                    .setName("board" + boardSuffix)
                    .setDepartment(new DepartmentDTO()
                        .setId(departmentId)));
            return null;
        });
        
        unprivilegedUsers.put(Scope.POST, testUserService.authenticate());
        transactionTemplate.execute(status -> postApi.postPost(boardId, samplePost));
        return unprivilegedUsers;
    }
    
    @SuppressWarnings("ConstantConditions")
    void verifyResourceActions(Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        User user = null;
        verifyResourceActions(user, scope, id, operations, expectedActions);
    }
    
    void verifyResourceActions(Collection<User> users, Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        users.forEach(user -> verifyResourceActions(user, scope, id, operations, expectedActions));
    }
    
    void verifyResourceActions(User user, Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        ExceptionCode exceptionCode;
        if (user == null) {
            testUserService.unauthenticate();
            exceptionCode = ExceptionCode.UNAUTHENTICATED_USER;
        } else {
            testUserService.setAuthentication(user.getStormpathId());
            exceptionCode = ExceptionCode.FORBIDDEN_ACTION;
        }
        
        Resource resource = resourceService.getResource(user, scope, id);
        if (ArrayUtils.isEmpty(expectedActions)) {
            Assert.assertNull(resource.getActions());
        } else {
            assertThat(resource.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()), Matchers.containsInAnyOrder(expectedActions));
        }
        
        for (Action action : Action.values()) {
            if (!ArrayUtils.contains(expectedActions, action)) {
                transactionTemplate.execute(status -> {
                    Runnable operation = operations.get(action);
                    if (operation != null) {
                        LOGGER.info("Verifying forbidden action: " + action.name().toLowerCase());
                        ExceptionUtils.verifyApiException(ApiForbiddenException.class, operation, exceptionCode, status);
                    }
                    
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
    
}
