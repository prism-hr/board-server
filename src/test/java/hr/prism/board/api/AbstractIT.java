package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.definition.DocumentDefinition;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.repository.*;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.service.*;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
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

    private TransactionTemplate transactionTemplate;

    @Value("${server.url}")
    String serverUrl;

    @Value("${resource.archive.duration.seconds}")
    Long resourceArchiveDurationSeconds;

    @Inject
    DepartmentApi departmentApi;

    @Inject
    DepartmentPaymentApi departmentPaymentApi;

    @Inject
    DepartmentUserApi departmentUserApi;

    @Inject
    BoardApi boardApi;

    @Inject
    PostApi postApi;

    @Inject
    PostResponseApi postResponseApi;

    @Inject
    UserApi userApi;

    @Inject
    UserActivityApi userActivityApi;

    @Inject
    UserNotificationSuppressionApi userNotificationSuppressionApi;

    @Inject
    AuthenticationApi authenticationApi;

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
    UserRoleService userRoleService;

    @Inject
    PostService postService;

    @Inject
    UniversityService universityService;

    @Inject
    TestUserService testUserService;

    @Inject
    ResourceEventService resourceEventService;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ResourceRelationRepository resourceRelationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ResourceEventRepository resourceEventRepository;

    @Inject
    UniversityRepository universityRepository;

    @Inject
    TestActivityService testActivityService;

    @Inject
    TestNotificationService testNotificationService;

    @Inject
    MockMvc mockMvc;

    @Inject
    ObjectMapper objectMapper;

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
        transactionTemplate.execute(status -> {
            Query removeForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();

            @SuppressWarnings("unchecked")
            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();
            tablesNames.stream().filter(tableName -> !Arrays.asList("flyway_schema_history", "workflow").contains(tableName)).forEach(tableName -> {
                Query emptyTable = entityManager.createNativeQuery("DELETE FROM " + tableName);
                emptyTable.executeUpdate();
            });

            Query restoreForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();

            universityService.getOrCreateUniversity("University College London", "ucl");
            return null;
        });
    }

    LinkedHashMap<Scope, User> makeUnprivilegedUsers(int departmentSuffix) {
        LinkedHashMap<Scope, User> unprivilegedUsers = new LinkedHashMap<>();
        unprivilegedUsers.put(Scope.DEPARTMENT, testUserService.authenticate());

        Long universityId = transactionTemplate.execute(status -> universityService.getOrCreateUniversity("University College London", "ucl").getId());
        Long otherDepartmentId = transactionTemplate.execute(status -> departmentApi.createDepartment(
            universityId,
            new DepartmentDTO()
                .setName("department" + departmentSuffix)
                .setSummary("department summary")).getId());

        transactionTemplate.execute(status -> boardApi.createBoard(
            otherDepartmentId,
            new BoardDTO()
                .setName("board" + departmentSuffix)).getId());

        return unprivilegedUsers;
    }

    LinkedHashMap<Scope, User> makeUnprivilegedUsers(Long boardId, int departmentSuffix, PostDTO samplePost) {
        LinkedHashMap<Scope, User> unprivilegedUsers = makeUnprivilegedUsers(departmentSuffix);
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
        verifyResourceActions(user, scope, id, operations, expectedActions.toArray(new Action[0]));
    }

    void verifyResourceActions(User user, Scope scope, Long id, Map<Action, Runnable> operations, Collection<Action> expectedActions) {
        verifyResourceActions(user, scope, id, operations, expectedActions.toArray(new Action[0]));
    }

    void verifyResourceActions(Collection<User> users, Scope scope, Long id, Map<Action, Runnable> operations, Collection<Action> expectedActions) {
        users.forEach(user -> verifyResourceActions(user, scope, id, operations, expectedActions.toArray(new Action[0])));
    }

    void verifyResourceActions(Collection<User> users, Scope scope, Long id, Map<Action, Runnable> operations, Action... expectedActions) {
        users.forEach(user -> verifyResourceActions(user, scope, id, operations, expectedActions));
    }

    void listenForActivities(User user) {
        testUserService.setAuthentication(user);
        Assert.assertTrue(userActivityApi.getActivities().isEmpty());
    }

    void verifyActivitiesEmpty(User user) {
        testUserService.setAuthentication(user);
        Assert.assertTrue(activityService.getActivities(user.getId()).isEmpty());
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
                            testUserService.setAuthentication(user);
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
