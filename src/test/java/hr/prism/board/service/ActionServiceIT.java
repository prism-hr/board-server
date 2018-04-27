package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.representation.ActionRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/actionService_setUp.sql")
@Sql(scripts = "classpath:data/actionService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ActionServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionServiceIT.class);

    private static final List<State> ASSIGNABLE_STATES =
        Stream.of(State.values()).filter(state -> !state.equals(PREVIOUS)).collect(toList());

    @Inject
    private UserService userService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private BoardService boardService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserRoleService userRoleService;

    @Test
    public void executeAction_departmentAdministratorActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT),
                    new ActionRepresentation().setAction(EDIT).setState(DRAFT),
                    new ActionRepresentation().setAction(EXTEND).setState(ACCEPTED),
                    new ActionRepresentation().setAction(SUBSCRIBE).setState(ACCEPTED))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING),
                    new ActionRepresentation().setAction(EDIT).setState(PENDING),
                    new ActionRepresentation().setAction(EXTEND).setState(ACCEPTED),
                    new ActionRepresentation().setAction(SUBSCRIBE).setState(ACCEPTED))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED),
                    new ActionRepresentation().setAction(EDIT).setState(ACCEPTED),
                    new ActionRepresentation().setAction(EXTEND).setState(ACCEPTED),
                    new ActionRepresentation().setAction(SUBSCRIBE).setState(ACCEPTED),
                    new ActionRepresentation().setAction(UNSUBSCRIBE).setState(ACCEPTED))
                .expect(REJECTED,
                    new ActionRepresentation().setAction(VIEW).setState(REJECTED),
                    new ActionRepresentation().setAction(EDIT).setState(REJECTED),
                    new ActionRepresentation().setAction(SUBSCRIBE).setState(ACCEPTED));

        verify(user, user, department, expectations);
    }

    @Test
    public void executeAction_departmentMemberActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        User member = setUpUser("member", "member", "member@prism.hr");
        userRoleService.createUserRole(department, member, MEMBER);

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(user, member, department, expectations);
    }

    @Test
    public void executeAction_publicActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(user, null, department, expectations);
    }

    private Department setupDepartment(User user, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return departmentService.createDepartment(1L,
            new DepartmentDTO()
                .setName(name)
                .setSummary(name + " summary"));
    }

    private Board setupBoard(User user, Long departmentId) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return boardService.createBoard(departmentId,
            new BoardDTO()
                .setName("board")
                .setPostCategories(singletonList("category")));
    }

    private Post setupPost(User user, Long boardId) {
        return null;
    }

    private User setUpUser(String givenName, String surname, String email) {
        return userService.createUser(
            new RegisterDTO()
                .setGivenName(givenName)
                .setSurname(surname)
                .setEmail(email)
                .setPassword("password"));
    }

    private void verify(User admin, User user, Resource resource, Expectations expectations) {
        Resource extendResource = null;
        for (State state : ASSIGNABLE_STATES) {
            resourceService.updateState(resource, state);
            Resource testResource = resourceService.getResource(user, resource.getScope(), resource.getId());

            for (Action action : Action.values()) {
                LOGGER.info("Executing " + action + " on " + testResource.getScope() + " in " + state);
                Resource mockResource;
                if (action == EXTEND) {
                    extendResource = extendResource == null ? extendResource(admin, testResource) : extendResource;
                    mockResource = extendResource;
                } else {
                    mockResource = testResource;
                }

                ActionRepresentation expected = expectations.expected(state, action);
                if (expected == null) {
                    verifyForbidden(user, testResource, action, mockResource);
                } else {
                    verifyPermitted(user, testResource, action, mockResource, expected);
                }
            }
        }

        expectations.verify();
    }

    private Resource extendResource(User admin, Resource resource) {
        Scope scope = resource.getScope();
        try {
            switch (resource.getScope()) {
                case DEPARTMENT:
                    return setupBoard(admin, resource.getId());
                case BOARD:
                    return resource;
                default:
                    throw new Error("Cannot extend: " + scope);
            }
        } catch (BoardForbiddenException e) {
            Scope childScope = Scope.values()[scope.ordinal() + 1];
            LOGGER.info("Mocking create " + childScope + " for " + scope + " in " + resource.getState());
            return new Resource();
        }
    }

    private void verifyForbidden(User user, Resource testResource, Action action, Resource mockResource) {
        assertThatThrownBy(
            () -> actionService.executeAction(user, testResource, action, () -> mockResource))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
    }

    private void verifyPermitted(User user, Resource testResource, Action action, Resource mockResource,
                                 ActionRepresentation expected) {
        Resource newResource = actionService.executeAction(user, testResource, action, () -> mockResource);
        assertEquals(expected.getState(), newResource.getState());
    }

    private static class Expectations {

        private ArrayListMultimap<State, ActionRepresentation> expectations = ArrayListMultimap.create();

        private Expectations expect(State state, ActionRepresentation... actions) {
            requireNonNull(actions, "actions cannot be null");
            Stream.of(actions).forEach(action -> expectations.put(state, action));
            return this;
        }

        private ActionRepresentation expected(State state, Action action) {
            ActionRepresentation expected =
                expectations.get(state)
                    .stream()
                    .filter(expectation -> expectation.equals(new ActionRepresentation().setAction(action)))
                    .findFirst()
                    .orElse(null);

            if (expected == null) {
                return null;
            }

            expectations.remove(state, expected);
            return expected;
        }

        private void verify() {
            assertThat(expectations.keySet()).isEmpty();
        }

    }

}
