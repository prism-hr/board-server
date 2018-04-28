package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.DBTestContext;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
        User departmentAdministrator =
            setUpUser("administrator", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");
        Board board = setupBoard(departmentAdministrator, department.getId(), "board");

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

        verify(departmentAdministrator, department, board, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnDepartment() {
        User departmentAdministrator =
            setUpUser("administrator", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");
        Board board = setupBoard(departmentAdministrator, department.getId(), "board");

        User departmentAuthor = setUpUser("department", "author", "department@pauthor.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User departmentMember = setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User postAdministrator = setUpUser("post", "administrator", "post@administrator.hr");
        setupPost(postAdministrator, board.getId(), "post");

        User otherDepartmentAdministrator = setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment = setupDepartment(otherDepartmentAdministrator, "other-department");
        Board otherBoard = setupBoard(otherDepartmentAdministrator, otherDepartment.getId(), "other-board");

        User otherDepartmentAuthor =
            setUpUser("other-department", "author", "other-department@pauthor.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherPostAdministrator =
            setUpUser("other-post", "administrator", "other-post@padministrator.hr");
        setupPost(otherPostAdministrator, otherBoard.getId(), "other-post");

        Scenarios scenarios =
            new Scenarios()
                .scenario(departmentAuthor, "Department author")
                .scenario(departmentMember, "Department member")
                .scenario(postAdministrator, "Post administrator")
                .scenario(otherDepartmentAdministrator, "Other department administrator")
                .scenario(otherDepartmentAuthor, "Other department author")
                .scenario(otherDepartmentMember, "Other department member")
                .scenario(otherPostAdministrator, "Other post administrator")
                .scenario(null, "Public user");

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new ActionRepresentation().setAction(VIEW).setState(DRAFT))
                .expect(PENDING,
                    new ActionRepresentation().setAction(VIEW).setState(PENDING))
                .expect(ACCEPTED,
                    new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(scenarios, department, board, expectations);
    }

    @Test
    public void executeAction_departmentAdministratorActionsOnBoard() {
        User departmentAdministrator =
            setUpUser("department", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");
        Board board = setupBoard(departmentAdministrator, department.getId(), "board");
        Post post = setupPost(departmentAdministrator, board.getId(), "post");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new ActionRepresentation().setAction(VIEW).setState(ACCEPTED),
                new ActionRepresentation().setAction(EDIT).setState(ACCEPTED),
                new ActionRepresentation().setAction(EXTEND).setState(PENDING),
                new ActionRepresentation().setAction(REJECT).setState(REJECTED))
            .expect(REJECTED,
                new ActionRepresentation().setAction(VIEW).setState(REJECTED),
                new ActionRepresentation().setAction(EDIT).setState(REJECTED),
                new ActionRepresentation().setAction(RESTORE).setState(ACCEPTED));

        verify(departmentAdministrator, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAdministratorActionsOnBoardWhenDepartmentRejected() {
        User departmentAdministrator =
            setUpUser("department", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");
        Board board = setupBoard(departmentAdministrator, department.getId(), "board");
        Post post = setupPost(departmentAdministrator, board.getId(), "post");

        resourceService.updateState(department, REJECTED);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new ActionRepresentation().setAction(VIEW).setState(ACCEPTED),
                new ActionRepresentation().setAction(EDIT).setState(ACCEPTED),
                new ActionRepresentation().setAction(REJECT).setState(REJECTED))
            .expect(REJECTED,
                new ActionRepresentation().setAction(VIEW).setState(REJECTED),
                new ActionRepresentation().setAction(EDIT).setState(REJECTED),
                new ActionRepresentation().setAction(RESTORE).setState(ACCEPTED));

        verify(departmentAdministrator, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAuthorActionsOnBoard() {
        User departmentAdministrator =
            setUpUser("department", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");
        Board board = setupBoard(departmentAdministrator, department.getId(), "board");
        Post post = setupPost(departmentAdministrator, board.getId(), "post");

        User departmentAuthor = setUpUser("department", "author", "department@author.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new ActionRepresentation().setAction(VIEW).setState(ACCEPTED),
                new ActionRepresentation().setAction(EXTEND).setState(PENDING));

        verify(departmentAuthor, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAuthorActionsOnBoardWhenDepartmentRejected() {
        User departmentAdministrator =
            setUpUser("department", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");
        Board board = setupBoard(departmentAdministrator, department.getId(), "board");
        Post post = setupPost(departmentAdministrator, board.getId(), "post");

        resourceService.updateState(department, REJECTED);

        User departmentAuthor = setUpUser("department", "author", "department@author.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(departmentAuthor, board, post, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnBoard() {
        User departmentAdministrator =
            setUpUser("department", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");

        Long departmentId = department.getId();
        Board board = setupBoard(departmentAdministrator, departmentId, "board");
        Board otherBoard = setupBoard(departmentAdministrator, departmentId, "other-board");

        User postAdministrator = setUpUser("post", "administrator", "post@administrator.hr");
        Post post = setupPost(postAdministrator, board.getId(), "post");

        User otherBoardPostAdministrator = setUpUser(
            "other-board-post", "administrator", "other-board-post@administrator.hr");
        setupPost(otherBoardPostAdministrator, otherBoard.getId(), "other-board-post");

        User departmentMember = setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User otherDepartmentAdministrator = setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment = setupDepartment(otherDepartmentAdministrator, "other-department");
        Board otherDepartmentBoard =
            setupBoard(otherDepartmentAdministrator, otherDepartment.getId(), "other-board");

        User otherDepartmentAuthor =
            setUpUser("other-department", "author", "other-department@author.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = setUpUser("other-department-post",
            "administrator", "other-department-post@administrator.hr");
        setupPost(otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), "other-department-post");

        Scenarios scenarios = new Scenarios()
            .scenario(departmentMember, "Department member")
            .scenario(postAdministrator, "Post administrator")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentAdministrator, "Other department post administrator")
            .scenario(null, "Public user");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new ActionRepresentation().setAction(VIEW).setState(ACCEPTED),
                new ActionRepresentation().setAction(EXTEND).setState(DRAFT));

        verify(scenarios, board, post, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnBoardWhenDepartmentRejected() {
        User departmentAdministrator =
            setUpUser("department", "administrator", "department@administrator.hr");
        Department department = setupDepartment(departmentAdministrator, "department");

        Long departmentId = department.getId();
        Board board = setupBoard(departmentAdministrator, departmentId, "board");
        Board otherBoard = setupBoard(departmentAdministrator, departmentId, "other-board");

        User postAdministrator = setUpUser("post", "administrator", "post@administrator.hr");
        Post post = setupPost(postAdministrator, board.getId(), "post");

        User otherBoardPostAdministrator = setUpUser(
            "other-board-post", "administrator", "other-board-post@administrator.hr");
        setupPost(otherBoardPostAdministrator, otherBoard.getId(), "other-board-post");

        resourceService.updateState(department, REJECTED);

        User departmentMember = setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User otherDepartmentAdministrator = setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment = setupDepartment(otherDepartmentAdministrator, "other-department");
        Board otherDepartmentBoard =
            setupBoard(otherDepartmentAdministrator, otherDepartment.getId(), "other-department-board");

        User otherDepartmentAuthor =
            setUpUser("other-department", "author", "other-department@author.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = setUpUser("other-department-post",
            "administrator", "other-department-post@administrator.hr");
        setupPost(otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), "other-department-post");

        Scenarios scenarios = new Scenarios()
            .scenario(departmentMember, "Department member")
            .scenario(postAdministrator, "Post administrator")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentPostAdministrator, "Other department post administrator")
            .scenario(null, "Public user");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new ActionRepresentation().setAction(VIEW).setState(ACCEPTED));

        verify(scenarios, board, post, expectations);
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

    private void verify(Scenarios scenarios, Resource resource, Resource extendResource,
                        Expectations expectations) {
        scenarios.forEach(scenario -> {
            LOGGER.info("Verifying: " + scenario.description);
            verify(scenario.user, resource, extendResource, expectations);
        });
    }

    private void verify(User user, Resource resource, Resource extendResource, Expectations expectations) {
        for (State state : ASSIGNABLE_STATES) {
            resourceService.updateState(resource, state);
            Resource testResource = resourceService.getResource(user, resource.getScope(), resource.getId());

            for (Action action : Action.values()) {
                LOGGER.info("Executing " + action + " on " + testResource.getScope() + " in " + state);
                Resource executeResource = action == EXTEND ? extendResource : testResource;

                ActionRepresentation expected = expectations.expected(state, action);
                if (expected == null) {
                    verifyForbidden(user, testResource, action, executeResource);
                } else {
                    verifyPermitted(user, testResource, action, executeResource, expected);
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
                                 ActionRepresentation expected) {
        Resource newResource = actionService.executeAction(user, testResource, action, () -> executeResource);
        assertEquals(expected.getState(), newResource.getState());
    }

    private static class Scenarios {

        private List<Scenario> scenarios = new ArrayList<>();

        private Scenarios scenario(User user, String description) {
            scenarios.add(new Scenario(user, description));
            return this;
        }

        private void forEach(Consumer<Scenario> consumer) {
            scenarios.forEach(consumer);
        }

        private static class Scenario {

            private User user;

            private String description;

            private Scenario(User user, String description) {
                this.user = user;
                this.description = description;
            }
        }

    }

    private static class Expectations {

        private ArrayListMultimap<State, ActionRepresentation> expectations = ArrayListMultimap.create();

        private Expectations expect(State state, ActionRepresentation... actions) {
            requireNonNull(actions, "actions cannot be null");
            Stream.of(actions).forEach(action -> expectations.put(state, action));
            return this;
        }

        private ActionRepresentation expected(State state, Action action) {
            return expectations.get(state)
                .stream()
                .filter(expectation -> expectation.equals(new ActionRepresentation().setAction(action)))
                .findFirst()
                .orElse(null);
        }

    }

}
