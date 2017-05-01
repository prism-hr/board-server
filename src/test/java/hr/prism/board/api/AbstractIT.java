package hr.prism.board.api;

import com.google.common.collect.Lists;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.service.ActionService;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.TestUserService;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThat;

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
    
    void verifyResourceActions(Scope scope, Long id, State expectedState, ExpectedUserActions expectedUserActions) {
        // Verify expected resource
        testUserService.unauthenticate();
        Resource resource = resourceService.getResource(null, scope, id);
        Assert.assertEquals(expectedState, resource.getState());
        
        // Verify public user
        verifyPermissions(null, resource, expectedUserActions.get(), ExceptionCode.UNAUTHENTICATED_USER);
        
        // Verify privileged / unprivileged users
        for (User user : expectedUserActions.keySet()) {
            testUserService.setAuthentication(user.getStormpathId());
            resource = resourceService.getResource(user, scope, id);
            verifyPermissions(user, resource, expectedUserActions.get(user), ExceptionCode.FORBIDDEN_ACTION);
        }
    }
    
    private void verifyPermissions(User user, Resource resource, ExpectedUserActions.ExpectedUserActionEntry expectedUserActionEntry, ExceptionCode expectedExceptionCode) {
        // Verify permitted actions
        Collection<Action> expectedActions = expectedUserActionEntry.getActions();
        assertThat(resource.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
            Matchers.containsInAnyOrder(expectedUserActionEntry.getActions().toArray(new Action[0])));
        
        // Verify forbidden actions
        for (Action action : Action.values()) {
            if (!expectedActions.contains(action)) {
                ExceptionUtil.verifyApiException(ApiForbiddenException.class,
                    () -> actionService.executeAction(user, resource, action, null), expectedExceptionCode, null);
            }
        }
        
        // Verify forbidden operations
        for (Runnable forbiddenOperation : expectedUserActionEntry.getForbiddenOperations()) {
            transactionTemplate.execute(status -> {
                ExceptionUtil.verifyApiException(ApiForbiddenException.class, forbiddenOperation, expectedExceptionCode, status);
                return null;
            });
        }
    }
    
    public static class ExpectedUserActions extends HashMap<User, ExpectedUserActions.ExpectedUserActionEntry> {
        
        private ExpectedUserActionEntry expectedPublicActions;
        
        public ExpectedUserActions add(Collection<Action> actions, Runnable... forbiddenOperations) {
            this.expectedPublicActions = new ExpectedUserActionEntry().setActions(actions).setForbiddenOperations(convertForbiddenOperations(forbiddenOperations));
            return this;
        }
        
        public ExpectedUserActions add(User user, Collection<Action> actions, Runnable... forbiddenOperations) {
            put(user, new ExpectedUserActionEntry().setActions(actions).setForbiddenOperations(convertForbiddenOperations(forbiddenOperations)));
            return this;
        }
        
        public ExpectedUserActions addAll(Collection<User> users, Collection<Action> actions, Runnable... forbiddenOperations) {
            users.forEach(key -> add(key, actions, forbiddenOperations));
            return this;
        }
        
        public ExpectedUserActionEntry get() {
            return expectedPublicActions;
        }
        
        private Collection<Runnable> convertForbiddenOperations(Runnable[] forbiddenActions) {
            return forbiddenActions == null ? Collections.emptyList() : Arrays.asList(forbiddenActions);
        }
        
        public static class ExpectedUserActionEntry {
            
            private Collection<Action> actions;
            
            private Collection<Runnable> forbiddenOperations;
            
            public Collection<Action> getActions() {
                return actions;
            }
            
            public ExpectedUserActionEntry setActions(Collection<Action> actions) {
                this.actions = actions;
                return this;
            }
            
            public Collection<Runnable> getForbiddenOperations() {
                return forbiddenOperations;
            }
            
            public ExpectedUserActionEntry setForbiddenOperations(Collection<Runnable> forbiddenOperations) {
                this.forbiddenOperations = forbiddenOperations;
                return this;
            }
            
        }
        
    }
    
}
