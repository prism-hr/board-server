package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Collections;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
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

    @Test
    public void executeAction_departmentAdministratorActionsOnDepartment() {
        User user = setUpUser("department", "department", "department@prism.hr");
        Department department = setupDepartment(user);

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT),
                    new ActionRepresentation().setAction(EDIT).setState(DRAFT),
                    new ActionRepresentation().setAction(EXTEND).setState(ACCEPTED),
                    new ActionRepresentation().setAction(SUBSCRIBE).setState(ACCEPTED));

        verify(user, department, expectations);
    }

    private Department setupDepartment(User user) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return departmentService.createDepartment(1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("department summary"));
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

    private void verify(User user, Resource resource, Expectations expectations) {
        Stream.of(State.values()).forEach(state -> {
            resourceService.updateState(resource, state);
            Stream.of(Action.values()).forEach(action -> {
                Resource testResource = testResource(user, resource, action);

                ActionRepresentation expected = expectations.expected(state, action);
                if (expected == null) {
                    assertThatThrownBy(() -> actionService.executeAction(user, resource, action, () -> testResource))
                        .isExactlyInstanceOf(BoardForbiddenException.class)
                        .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
                } else {
                    Resource newResource =
                        actionService.executeAction(user, resource, action, () -> testResource);
                    assertEquals(expected.getState(), newResource.getState());
                }
            });
        });

        expectations.verify();
    }

    private Resource testResource(User user, Resource resource, Action action) {
        if (action == EXTEND) {
            Scope scope = resource.getScope();
            switch (resource.getScope()) {
                case DEPARTMENT:
                    return setupBoard(user, resource.getId());
                case BOARD:
                    return resource;
                default:
                    throw new Error("Cannot extend: " + scope);
            }
        }

        return resource;
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

            expectations.remove(state, action);
            return expected;
        }

        private void verify() {
            assertThat(expectations.keySet()).isEmpty();
        }

    }

}
