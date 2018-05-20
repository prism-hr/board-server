package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.service.ServiceHelper.ResourceModifier;
import hr.prism.board.service.ServiceHelper.Scenarios;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/newActionService_setUp.sql")
@Sql(scripts = "classpath:data/actionService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ActionServiceIT {

    private static final Logger LOGGER = getLogger(ActionServiceIT.class);

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserService userService;

    @Inject
    private ServiceHelper serviceHelper;

    private User administrator;

    private User author;

    private User member;

    private User postAdministrator;

    private University university;

    private Department department;

    private Board board;

    private Post post;

    @Test
    public void executeAction_successWhenDepartmentAndDepartmentAdministrator() {
        User administrator = userService.getById(1L);
        Department department = (Department) resourceService.getById(2L);
        Board board = (Board) resourceService.getById(3L);

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new Expectation(VIEW, DRAFT),
                    new Expectation(EDIT, DRAFT),
                    new Expectation(EXTEND, ACCEPTED),
                    new Expectation(SUBSCRIBE, ACCEPTED))
                .expect(PENDING,
                    new Expectation(VIEW, PENDING),
                    new Expectation(EDIT, PENDING),
                    new Expectation(EXTEND, ACCEPTED),
                    new Expectation(SUBSCRIBE, ACCEPTED))
                .expect(ACCEPTED,
                    new Expectation(VIEW, ACCEPTED),
                    new Expectation(EDIT, ACCEPTED),
                    new Expectation(EXTEND, ACCEPTED),
                    new Expectation(SUBSCRIBE, ACCEPTED),
                    new Expectation(UNSUBSCRIBE, ACCEPTED))
                .expect(REJECTED,
                    new Expectation(VIEW, REJECTED),
                    new Expectation(EDIT, REJECTED),
                    new Expectation(SUBSCRIBE, ACCEPTED));

        verify(administrator, department, board, expectations);
    }

    @Test
    public void executeAction_successWhenDepartmentAndUnprivileged() {
        Department department = (Department) resourceService.getById(2L);
        Board board = (Board) resourceService.getById(3L);

        Scenarios scenarios = new Scenarios()
            .scenario(userService.getById(2L), "other department administrator")
            .scenario(userService.getById(3L), "department author")
            .scenario(userService.getById(4L), "other department author")
            .scenario(userService.getById(5L), "department member")
            .scenario(userService.getById(6L), "other department member")
            .scenario(userService.getById(11L), "department post administrator")
            .scenario(userService.getById(12L), "other department post administrator")
            .scenario(userService.getById(13L), "no roles")
            .scenario(null, "anonymous");

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new Expectation(VIEW, DRAFT))
                .expect(PENDING,
                    new Expectation(VIEW, PENDING))
                .expect(ACCEPTED,
                    new Expectation(VIEW, ACCEPTED));

        verify(scenarios, department, board, expectations);
    }

    @Test
    public void executeAction_successWhenBoardAndDepartmentAdministrator() {
        User administrator = userService.getById(1L);
        Board board = (Board) resourceService.getById(3L);
        Post post = (Post) resourceService.getById(4L);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(EXTEND, PENDING),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(RESTORE, ACCEPTED));

        verify(administrator, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardAndDepartmentAuthor() {
        User author = userService.getById(3L);
        Board board = (Board) resourceService.getById(3L);
        Post post = (Post) resourceService.getById(4L);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EXTEND, PENDING));

        verify(author, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardAndUnprivileged() {
        Board board = (Board) resourceService.getById(3L);
        Post post = (Post) resourceService.getById(4L);

        Scenarios scenarios = new Scenarios()
            .scenario(userService.getById(2L), "other department administrator")
            .scenario(userService.getById(4L), "other department author")
            .scenario(userService.getById(5L), "department member")
            .scenario(userService.getById(6L), "other department member")
            .scenario(userService.getById(11L), "department post administrator")
            .scenario(userService.getById(12L), "other department post administrator")
            .scenario(userService.getById(13L), "no roles")
            .scenario(null, "anonymous");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EXTEND, DRAFT));

        verify(scenarios, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardDepartmentAdministratorAndDepartmentRejected() {
        User administrator = userService.getById(1L);
        Board board = (Board) resourceService.getById(22L);
        Post post = (Post) resourceService.getById(23L);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(RESTORE, ACCEPTED));

        verify(administrator, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardDepartmentAuthorAndDepartmentRejected() {
        User author = userService.getById(4L);
        Board board = (Board) resourceService.getById(22L);
        Post post = (Post) resourceService.getById(23L);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(author, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardUnprivilegedAndDepartmentRejected() {
        Board board = (Board) resourceService.getById(22L);
        Post post = (Post) resourceService.getById(23L);

        Scenarios scenarios = new Scenarios()
            .scenario(userService.getById(4L), "other department author")
            .scenario(userService.getById(5L), "department member")
            .scenario(userService.getById(6L), "other department member")
            .scenario(userService.getById(11L), "department post administrator")
            .scenario(userService.getById(12L), "other department post administrator")
            .scenario(userService.getById(13L), "no roles")
            .scenario(null, "anonymous");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(scenarios, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenPostAndDepartmentAdministrator() {
        User administrator = userService.getById(1L);
        Post post = (Post) resourceService.getById(4L);

        ResourceModifier postPendingModifier = (resource) -> serviceHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> serviceHelper.setPostExpired((Post) resource);

        Expectations expectations = new Expectations()
            .expect(DRAFT, postPendingModifier,
                new Expectation(VIEW, DRAFT),
                new Expectation(EDIT, DRAFT),
                new Expectation(ACCEPT, PENDING),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(PENDING, postPendingModifier,
                new Expectation(VIEW, PENDING),
                new Expectation(EDIT, PENDING),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(ACCEPTED, postPendingModifier,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(PURSUE, ACCEPTED),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(EXPIRED, postExpiredModifier,
                new Expectation(VIEW, EXPIRED),
                new Expectation(EDIT, EXPIRED),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(SUSPENDED, postPendingModifier,
                new Expectation(VIEW, SUSPENDED),
                new Expectation(EDIT, SUSPENDED),
                new Expectation(ACCEPT, PENDING),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED, postPendingModifier,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(ACCEPT, PENDING),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(RESTORE, PREVIOUS))
            .expect(WITHDRAWN, postPendingModifier,
                new Expectation(VIEW, WITHDRAWN),
                new Expectation(EDIT, WITHDRAWN))
            .expect(ARCHIVED, postPendingModifier,
                new Expectation(VIEW, ARCHIVED),
                new Expectation(EDIT, ARCHIVED),
                new Expectation(RESTORE, PREVIOUS));

        verify(administrator, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostAndDepartmentMember() {
        Post post = (Post) resourceService.getById(4L);

        Scenarios scenarios = new Scenarios()
            .scenario(userService.getById(5L), "accepted department member")
            .scenario(userService.getById(7L), "pending department member");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(PURSUE, ACCEPTED));

        verify(scenarios, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPost() {
        executeAction_successWhenPostAndDepartmentMember();
        executeAction_successWhenPostAndPostAdministrator();
        executeAction_successWhenPostAndUnprivileged();

        resourceService.updateState(department, REJECTED);
        executeAction_successWhenPostAndParentRejected();

        resourceService.updateState(board, REJECTED);
        executeAction_successWhenPostAndParentRejected();

        resourceService.updateState(department, ACCEPTED);
        executeAction_successWhenPostAndParentRejected();
    }

    private void executeAction_successWhenPostAndParentRejected() {
        executeAction_successWhenPostDepartmentAdministratorAndParentRejected();
        executeAction_successWhenPostDepartmentMemberAndParentRejected();
        executeAction_successWhenPostPostAdministratorAndParentRejected();
        executeAction_successWhenPostUnprivilegedAndParentRejected();
    }

    private void executeAction_successWhenPostAndPostAdministrator() {
        ResourceModifier postPendingModifier = (resource) -> serviceHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> serviceHelper.setPostExpired((Post) resource);

        Expectations expectations = new Expectations()
            .expect(DRAFT, postPendingModifier,
                new Expectation(VIEW, DRAFT),
                new Expectation(EDIT, DRAFT),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(PENDING, postPendingModifier,
                new Expectation(VIEW, PENDING),
                new Expectation(EDIT, PENDING),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(ACCEPTED, postPendingModifier,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(PURSUE, ACCEPTED),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(EXPIRED, postExpiredModifier,
                new Expectation(VIEW, EXPIRED),
                new Expectation(EDIT, EXPIRED),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(SUSPENDED, postPendingModifier,
                new Expectation(VIEW, SUSPENDED),
                new Expectation(EDIT, SUSPENDED),
                new Expectation(CORRECT, DRAFT),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(REJECTED, postPendingModifier,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(WITHDRAWN, postPendingModifier,
                new Expectation(VIEW, WITHDRAWN),
                new Expectation(EDIT, WITHDRAWN),
                new Expectation(RESTORE, PREVIOUS))
            .expect(ARCHIVED, postPendingModifier,
                new Expectation(VIEW, ARCHIVED),
                new Expectation(EDIT, ARCHIVED),
                new Expectation(RESTORE, PREVIOUS));

        verify(postAdministrator, post, null, expectations);
    }

    private void executeAction_successWhenPostAndUnprivileged() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(department, AUTHOR));

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(scenarios, post, null, expectations);
    }

    private void executeAction_successWhenPostDepartmentAdministratorAndParentRejected() {
        ResourceModifier postPendingModifier = (resource) -> serviceHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> serviceHelper.setPostExpired((Post) resource);

        Expectations expectations = new Expectations()
            .expect(DRAFT, postPendingModifier,
                new Expectation(VIEW, DRAFT),
                new Expectation(EDIT, DRAFT),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(ACCEPT, PENDING),
                new Expectation(REJECT, REJECTED))
            .expect(PENDING, postPendingModifier,
                new Expectation(VIEW, PENDING),
                new Expectation(EDIT, PENDING),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(ACCEPTED, postPendingModifier,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(EXPIRED, postExpiredModifier,
                new Expectation(VIEW, EXPIRED),
                new Expectation(EDIT, EXPIRED),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(REJECT, REJECTED))
            .expect(SUSPENDED, postPendingModifier,
                new Expectation(VIEW, SUSPENDED),
                new Expectation(EDIT, SUSPENDED),
                new Expectation(ACCEPT, PENDING),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED, postPendingModifier,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(SUSPEND, SUSPENDED),
                new Expectation(ACCEPT, PENDING),
                new Expectation(RESTORE, PREVIOUS))
            .expect(WITHDRAWN, postPendingModifier,
                new Expectation(VIEW, WITHDRAWN),
                new Expectation(EDIT, WITHDRAWN))
            .expect(ARCHIVED, postPendingModifier,
                new Expectation(VIEW, ARCHIVED),
                new Expectation(EDIT, ARCHIVED),
                new Expectation(RESTORE, PREVIOUS));

        verify(administrator, post, null, expectations);
    }

    private void executeAction_successWhenPostDepartmentMemberAndParentRejected() {
        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(member, post, null, expectations);
    }

    private void executeAction_successWhenPostPostAdministratorAndParentRejected() {
        ResourceModifier postPendingModifier = (resource) -> serviceHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> serviceHelper.setPostExpired((Post) resource);

        Expectations expectations = new Expectations()
            .expect(DRAFT, postPendingModifier,
                new Expectation(VIEW, DRAFT),
                new Expectation(EDIT, DRAFT),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(PENDING, postPendingModifier,
                new Expectation(VIEW, PENDING),
                new Expectation(EDIT, PENDING),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(ACCEPTED, postPendingModifier,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(EXPIRED, postExpiredModifier,
                new Expectation(VIEW, EXPIRED),
                new Expectation(EDIT, EXPIRED),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(SUSPENDED, postPendingModifier,
                new Expectation(VIEW, SUSPENDED),
                new Expectation(EDIT, SUSPENDED),
                new Expectation(CORRECT, DRAFT),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(REJECTED, postPendingModifier,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(WITHDRAW, WITHDRAWN))
            .expect(WITHDRAWN, postPendingModifier,
                new Expectation(VIEW, WITHDRAWN),
                new Expectation(EDIT, WITHDRAWN),
                new Expectation(RESTORE, PREVIOUS))
            .expect(ARCHIVED, postPendingModifier,
                new Expectation(VIEW, ARCHIVED),
                new Expectation(EDIT, ARCHIVED),
                new Expectation(RESTORE, PREVIOUS));

        verify(postAdministrator, post, null, expectations);
    }

    private void executeAction_successWhenPostUnprivilegedAndParentRejected() {
        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
            .scenarios(serviceHelper.setUpUnprivilegedUsers(department, AUTHOR));

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(scenarios, post, null, expectations);
    }

    private void verify(Scenarios scenarios, Resource resource, Resource extendResource,
                        Expectations expectations) {
        scenarios.forEach(scenario -> {
            LOGGER.info("Verifying actions: " + scenario.description);
            verify(scenario.user, resource, extendResource, expectations);
        });
    }

    private void verify(User user, Resource resource, Resource extendResource, Expectations expectations) {
        for (State state : new State[]{DRAFT, SUSPENDED, PENDING, ACCEPTED, EXPIRED, REJECTED, WITHDRAWN, ARCHIVED}) {
            resourceService.updateState(resource, state);
            Optional.ofNullable(expectations.getModifier(state))
                .ifPresent((modifier) -> modifier.modify(resource));

            Resource testResource = resourceService.getResource(user, resource.getScope(), resource.getId());
            for (Action action : Action.values()) {
                String userString = user == null ? "Anonymous" : user.toString();
                LOGGER.info(action + " on " + testResource.getScope() + " in " + state + " as " + userString);
                Resource executeResource = action == EXTEND ? extendResource : testResource;

                Expectation expectation = expectations.expected(state, action);
                if (expectation == null) {
                    verifyForbidden(user, testResource, action, executeResource);
                } else {
                    verifyPermitted(user, testResource, action, executeResource, expectation);
                }
            }
        }
    }

    private void verifyForbidden(User user, Resource testResource, Action action, Resource executeResource) {
        assertThatThrownBy(
            () -> actionService.executeAction(user, testResource, action, () -> executeResource))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
    }

    private void verifyPermitted(User user, Resource testResource, Action action, Resource executeResource,
                                 Expectation expectation) {
        State expectedState = expectation.state;
        State previousState = testResource.getPreviousState();
        Resource newResource = actionService.executeAction(user, testResource, action, () -> executeResource);
        if (expectedState == PREVIOUS) {
            assertEquals(previousState, newResource.getState());
        } else {
            assertEquals(expectedState, newResource.getState());
        }
    }

    private static class Expectations {

        private ArrayListMultimap<State, Expectation> expectations = ArrayListMultimap.create();

        private Map<State, ResourceModifier> modifiers = new HashMap<>();

        private Expectations expect(State state, Expectation... expectations) {
            Stream.of(expectations).forEach(action -> this.expectations.put(state, action));
            return this;
        }

        private Expectations expect(State state, ResourceModifier modifier, Expectation... expectations) {
            Stream.of(expectations).forEach(action -> this.expectations.put(state, action));
            modifiers.put(state, modifier);
            return this;
        }

        private Expectation expected(State state, Action action) {
            return expectations.get(state)
                .stream()
                .filter(expectation -> expectation.equals(new Expectation(action)))
                .findFirst()
                .orElse(null);
        }

        private ResourceModifier getModifier(State state) {
            return modifiers.get(state);
        }

    }

    private static class Expectation {

        private Action action;

        private State state;

        private Expectation(Action action) {
            this.action = action;
        }

        private Expectation(Action action, State state) {
            this.action = action;
            this.state = state;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(action)
                .toHashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            Expectation that = (Expectation) other;
            return new EqualsBuilder()
                .append(action, that.action)
                .isEquals();
        }

    }

}
