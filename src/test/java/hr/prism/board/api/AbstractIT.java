package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.definition.DocumentDefinition;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.service.ActivityService;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceEventService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.TestUserService;
import hr.prism.board.service.TestWebSocketService;
import hr.prism.board.service.UniversityService;
import hr.prism.board.service.UserRoleService;
import hr.prism.board.service.UserService;
import hr.prism.board.service.cache.UserCacheService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public abstract class AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);

    TransactionTemplate transactionTemplate;

    @Value("${resource.archive.duration.seconds}")
    Long resourceArchiveDurationSeconds;

    @Inject
    ResourceApi resourceApi;

    @Inject
    DepartmentApi departmentApi;

    @Inject
    BoardApi boardApi;

    @Inject
    PostApi postApi;

    @Inject
    UserApi userApi;

    @Inject
    AuthenticationApi authenticationApi;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ResourceEventRepository resourceEventRepository;

    @Inject
    ActivityService activityService;

    @Inject
    DepartmentService departmentService;

    @Inject
    BoardService boardService;

    @Inject
    ResourceService resourceService;

    @Inject
    UserService userService;

    @Inject
    UserCacheService userCacheService;

    @Inject
    UserRoleService userRoleService;

    @Inject
    PostService postService;

    @Inject
    UniversityService universityService;

    @Inject
    TestUserService testUserService;

    @Inject
    TestWebSocketService testWebSocketService;

    @Inject
    TestNotificationService testNotificationService;

    @Inject
    ResourceEventService resourceEventService;

    @Inject
    MockMvc mockMvc;

    @Inject
    ObjectMapper objectMapper;

    @Value("${server.url}")
    String serverUrl;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    @Before
    public void before() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @After
    public void after() {
        transactionTemplate.execute(transactionTemplate -> {
            Query removeForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();

            @SuppressWarnings("unchecked")
            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();
            tablesNames.stream().filter(tableName -> !Arrays.asList("schema_version", "workflow").contains(tableName)).forEach(tableName -> {
                Query truncateTable = entityManager.createNativeQuery("DELETE FROM " + tableName);
                truncateTable.executeUpdate();
            });

            Query restoreForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();

            universityService.getOrCreateUniversity("University College London", "ucl");
            return null;
        });
    }

    LinkedHashMap<Scope, User> makeUnprivilegedUsers(Long departmentId, int departmentSuffix, int boardSuffix) {
        LinkedHashMap<Scope, User> unprivilegedUsers = new LinkedHashMap<>();
        unprivilegedUsers.put(Scope.DEPARTMENT, testUserService.authenticate());

        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long otherDepartmentId = transactionTemplate.execute(status -> departmentApi.postDepartment(
            universityId,
            new DepartmentDTO()
                .setName("department" + departmentSuffix)
                .setSummary("department summary")).getId());

        transactionTemplate.execute(status -> boardApi.postBoard(
            otherDepartmentId,
            new BoardDTO()
                .setName("board" + departmentSuffix)).getId());

        unprivilegedUsers.put(Scope.BOARD, testUserService.authenticate());
        Long boardId = transactionTemplate.execute(status -> boardApi.postBoard(
            departmentId,
            new BoardDTO()
                .setName("board" + boardSuffix)).getId());

        User departmentAdmin = transactionTemplate.execute(status -> {
            Resource department = resourceService.findOne(departmentId);
            Set<UserRole> userRoles = department.getUserRoles();
            for (UserRole userRole : userRoles) {
                if (userRole.getRole() == Role.ADMINISTRATOR) {
                    return userRole.getUser();
                }
            }

            return null;
        });

        testUserService.setAuthentication(departmentAdmin.getId());
        transactionTemplate.execute(status -> boardApi.executeAction(boardId, "accept", new BoardPatchDTO()));
        return unprivilegedUsers;
    }

    LinkedHashMap<Scope, User> makeUnprivilegedUsers(Long departmentId, Long boardId, int departmentSuffix, int boardSuffix, PostDTO samplePost) {
        LinkedHashMap<Scope, User> unprivilegedUsers = makeUnprivilegedUsers(departmentId, departmentSuffix, boardSuffix);
        unprivilegedUsers.put(Scope.POST, testUserService.authenticate());
        transactionTemplate.execute(status -> postApi.postPost(boardId, samplePost));
        return unprivilegedUsers;
    }

    void verifyDocument(DocumentDefinition expectedDocument, DocumentRepresentation actualDocument) {
        if (expectedDocument == null) {
            assertNull(actualDocument);
        } else {
            assertEquals(expectedDocument.getFileName(), actualDocument.getFileName());
            assertEquals(expectedDocument.getCloudinaryId(), actualDocument.getCloudinaryId());
            assertEquals(expectedDocument.getCloudinaryUrl(), actualDocument.getCloudinaryUrl());
        }
    }

    @SuppressWarnings("ConstantConditions")
    void verifyResourceActions(Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        User user = null;
        verifyResourceActions(user, scope, id, operations, expectedActions);
    }

    @SuppressWarnings("ConstantConditions")
    void verifyResourceActions(Scope scope, Long id, Map<Action, Runnable> operations, Collection<Action> expectedActions) {
        User user = null;
        verifyResourceActions(user, scope, id, operations, expectedActions.toArray(new Action[expectedActions.size()]));
    }

    void verifyResourceActions(User user, Scope scope, Long id, Map<Action, Runnable> operations, Collection<Action> expectedActions) {
        verifyResourceActions(user, scope, id, operations, expectedActions.toArray(new Action[expectedActions.size()]));
    }

    void verifyResourceActions(Collection<User> users, Scope scope, Long id, Map<Action, Runnable> operations, Collection<Action> expectedActions) {
        users.forEach(user -> verifyResourceActions(user, scope, id, operations, expectedActions.toArray(new Action[expectedActions.size()])));
    }

    void verifyResourceActions(Collection<User> users, Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        users.forEach(user -> verifyResourceActions(user, scope, id, operations, expectedActions));
    }

    void listenForNewActivities(Long userId) {
        testUserService.setAuthentication(userId);
        Assert.assertTrue(activityService.getActivities(userId).isEmpty());
        testWebSocketService.handleSessionConnectedEvent(
            new SessionConnectedEvent(this, new GenericMessage<>("CONNECTED".getBytes()), new AuthenticationToken(userId)));
    }

    void verifyActivitiesEmpty(Long userId) {
        testUserService.setAuthentication(userId);
        Assert.assertTrue(activityService.getActivities(userId).isEmpty());
        testWebSocketService.verify(userId);
    }

    @SafeVarargs
    final <T> void verifyContains(List<T> actuals, T... expectations) {
        for (T expectation : expectations) {
            if (!actuals.contains(expectation)) {
                Assert.fail(actuals.stream().map(Object::toString).collect(Collectors.joining(", ")) + " does not contain " + expectation.toString());
            }
        }
    }

    private void verifyResourceActions(User user, Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        Resource resource = resourceService.getResource(user, scope, id);
        if (ArrayUtils.isEmpty(expectedActions)) {
            Assert.assertNull(resource.getActions());
        } else {
            assertThat(resource.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(Collectors.toList()), Matchers.containsInAnyOrder(expectedActions));
        }

        for (Action action : Action.values()) {
            if (!ArrayUtils.contains(expectedActions, action)) {
                transactionTemplate.execute(status -> {
                    Runnable operation = operations.get(action);
                    if (operation != null) {
                        ExceptionCode exceptionCode;
                        if (user == null) {
                            testUserService.unauthenticate();
                            if (action == Action.VIEW) {
                                exceptionCode = ExceptionCode.FORBIDDEN_ACTION;
                            } else {
                                exceptionCode = ExceptionCode.UNAUTHENTICATED_USER;
                            }
                        } else {
                            testUserService.setAuthentication(user.getId());
                            exceptionCode = ExceptionCode.FORBIDDEN_ACTION;
                        }

                        LOGGER.info("Verifying forbidden action: " + action.name().toLowerCase());
                        ExceptionUtils.verifyException(BoardForbiddenException.class, operation, exceptionCode, status);
                    }

                    return null;
                });
            }
        }
    }

}
