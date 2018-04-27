package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.MASTER_STUDENT;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.math.BigDecimal.ONE;
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
    private PostService postService;

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
    public void executeAction_departmentAuthorActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        User author = setUpUser("author", "author", "author@prism.hr");
        userRoleService.createUserRole(department, author, AUTHOR);

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(user, author, department, expectations);
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

    @Test
    public void executeAction_otherDepartmentAdministratorActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        User otherUser = setUpUser("other", "other", "other@prism.hr");
        setupDepartment(otherUser, "other");

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(user, otherUser, department, expectations);
    }

    @Test
    public void executeAction_otherDepartmentAuthorActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        User otherUser = setUpUser("other", "other", "other@prism.hr");
        Department otherDepartment = setupDepartment(otherUser, "other");

        User otherAuthor = setUpUser("other-author", "other-author", "other-author@prism.hr");
        userRoleService.createUserRole(otherDepartment, otherAuthor, AUTHOR);

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(user, otherAuthor, department, expectations);
    }

    @Test
    public void executeAction_otherDepartmentMemberActionsOnDepartment() {
        User user = setUpUser("administrator", "administrator", "administrator@prism.hr");
        Department department = setupDepartment(user, "department");

        User otherUser = setUpUser("other", "other", "other@prism.hr");
        Department otherDepartment = setupDepartment(otherUser, "other");

        User otherMember = setUpUser("other-member", "other-member", "other-member@prism.hr");
        userRoleService.createUserRole(otherDepartment, otherMember, AUTHOR);

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(user, otherMember, department, expectations);
    }

    private Department setupDepartment(User user, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return departmentService.createDepartment(1L,
            new DepartmentDTO()
                .setName(name)
                .setSummary(name + " summary"));
    }

    private Board setupBoard(User user, Long departmentId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return boardService.createBoard(departmentId,
            new BoardDTO()
                .setName(name)
                .setPostCategories(ImmutableList.of("Employment", "Internship")));
    }

    private Post setupPost(User user, Long boardId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return postService.createPost(boardId,
            new PostDTO()
                .setName(name)
                .setSummary(name + " summary")
                .setOrganization(
                    new OrganizationDTO()
                        .setName("organization"))
                .setLocation(new LocationDTO()
                    .setName("london")
                    .setDomicile("uk")
                    .setGoogleId("google")
                    .setLatitude(ONE)
                    .setLongitude(ONE))
                .setApplyWebsite("http://www.google.co.uk")
                .setPostCategories(ImmutableList.of("Employment", "Internship"))
                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))
                .setExistingRelation(STUDENT)
                .setExistingRelationExplanation(ImmutableMap.of("studyLevel", "MASTER"))
                .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS)));
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
                Resource executeResource;
                if (action == EXTEND) {
                    extendResource = extendResource == null ? extendResource(admin, testResource) : extendResource;
                    executeResource = extendResource;
                } else {
                    executeResource = testResource;
                }

                ActionRepresentation expected = expectations.expected(state, action);
                if (expected == null) {
                    verifyForbidden(user, testResource, action, executeResource);
                } else {
                    verifyPermitted(user, testResource, action, executeResource, expected);
                }
            }
        }

        expectations.verify();
    }

    private Resource extendResource(User user, Resource resource) {
        Scope scope = resource.getScope();
        try {
            switch (resource.getScope()) {
                case DEPARTMENT:
                    return setupBoard(user, resource.getId(), "extend");
                case BOARD:
                    return setupPost(user, resource.getId(), "extend");
                default:
                    throw new Error("Cannot extend: " + scope);
            }
        } catch (BoardForbiddenException e) {
            Scope childScope = Scope.values()[scope.ordinal() + 1];
            LOGGER.info("Mocking create " + childScope + " for " + scope + " in " + resource.getState());
            return new Resource();
        }
    }

    private void verifyForbidden(User user, Resource testResource, Action action, Resource executeResource) {
        assertThatThrownBy(
            () -> actionService.executeAction(user, testResource, action, () -> executeResource))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
    }

    private void verifyPermitted(User user, Resource testResource, Action action, Resource executeResource,
                                 ActionRepresentation expected) {
        Resource newResource = actionService.executeAction(user, testResource, action, () -> executeResource);
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

            if (expected != null) {
                expectations.remove(state, expected);
            }

            return expected;
        }

        private void verify() {
            assertThat(expectations.keySet()).isEmpty();
        }

    }

}
