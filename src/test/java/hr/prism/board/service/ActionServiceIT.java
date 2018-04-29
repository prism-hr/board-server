package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.DBTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardForbiddenException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
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
    private ResourceService resourceService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private DataHelper dataHelper;

    @Test
    public void executeAction_departmentAdministratorActionsOnDepartment() {
        User departmentAdministrator =
            dataHelper.setUpUser(
                "administrator", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

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

        verify(departmentAdministrator, department, board, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnDepartment() {
        User departmentAdministrator = dataHelper.setUpUser(
            "administrator", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User departmentAuthor = dataHelper.setUpUser(
            "department", "author", "department@pauthor.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User departmentMember = dataHelper.setUpUser(
            "department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User postAdministrator = dataHelper.setUpUser(
            "post", "administrator", "post@administrator.hr");
        dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        User otherDepartmentAdministrator = dataHelper.setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment =
            dataHelper.setUpDepartment(otherDepartmentAdministrator, 1L,"other-department");
        Board otherBoard = dataHelper.setUpBoard(
            otherDepartmentAdministrator, otherDepartment.getId(), "other-board");

        User otherDepartmentAuthor =
            dataHelper.setUpUser("other-department", "author", "other-department@pauthor.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            dataHelper.setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherPostAdministrator = dataHelper.setUpUser(
            "other-post", "administrator", "other-post@padministrator.hr");
        dataHelper.setUpPost(otherPostAdministrator, otherBoard.getId(), "other-post");

        User userWithoutRoles = dataHelper.setUpUser("without", "roles", "without@roles.hr");

        Scenarios scenarios =
            new Scenarios()
                .scenario(departmentAuthor, "Department author")
                .scenario(departmentMember, "Department member")
                .scenario(postAdministrator, "Post administrator")
                .scenario(otherDepartmentAdministrator, "Other department administrator")
                .scenario(otherDepartmentAuthor, "Other department author")
                .scenario(otherDepartmentMember, "Other department member")
                .scenario(otherPostAdministrator, "Other post administrator")
                .scenario(userWithoutRoles, "User without roles")
                .scenario(null, "Public user");

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
    public void executeAction_departmentAdministratorActionsOnBoard() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");
        Post post = dataHelper.setUpPost(departmentAdministrator, board.getId(), "post");

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

        verify(departmentAdministrator, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAdministratorActionsOnBoardWhenDepartmentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");
        Post post = dataHelper.setUpPost(departmentAdministrator, board.getId(), "post");

        resourceService.updateState(department, REJECTED);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(RESTORE, ACCEPTED));

        verify(departmentAdministrator, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAuthorActionsOnBoard() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");
        Post post = dataHelper.setUpPost(departmentAdministrator, board.getId(), "post");

        User departmentAuthor =
            dataHelper.setUpUser("department", "author", "department@author.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EXTEND, PENDING));

        verify(departmentAuthor, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAuthorActionsOnBoardWhenDepartmentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");
        Post post = dataHelper.setUpPost(departmentAdministrator, board.getId(), "post");

        resourceService.updateState(department, REJECTED);

        User departmentAuthor =
            dataHelper.setUpUser("department", "author", "department@author.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(departmentAuthor, board, post, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnBoard() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");

        Long departmentId = department.getId();
        Board board = dataHelper.setUpBoard(departmentAdministrator, departmentId, "board");
        Board otherBoard = dataHelper.setUpBoard(departmentAdministrator, departmentId, "other-board");

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        User otherBoardPostAdministrator = dataHelper.setUpUser(
            "other-board-post", "administrator", "other-board-post@administrator.hr");
        dataHelper.setUpPost(otherBoardPostAdministrator, otherBoard.getId(), "other-board-post");

        User departmentMember =
            dataHelper.setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User otherDepartmentAdministrator = dataHelper.setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment =
            dataHelper.setUpDepartment(otherDepartmentAdministrator, 1L,"other-department");
        Board otherDepartmentBoard =
            dataHelper.setUpBoard(otherDepartmentAdministrator, otherDepartment.getId(), "other-board");

        User otherDepartmentAuthor =
            dataHelper.setUpUser("other-department", "author", "other-department@author.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            dataHelper.setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = dataHelper.setUpUser("other-department-post",
            "administrator", "other-department-post@administrator.hr");
        dataHelper.setUpPost(
            otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), "other-department-post");

        User userWithoutRoles = dataHelper.setUpUser("without", "roles", "without@roles.hr");

        Scenarios scenarios = new Scenarios()
            .scenario(departmentMember, "Department member")
            .scenario(postAdministrator, "Post administrator")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentAdministrator, "Other department post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EXTEND, DRAFT));

        verify(scenarios, board, post, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnBoardWhenDepartmentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");

        Long departmentId = department.getId();
        Board board = dataHelper.setUpBoard(departmentAdministrator, departmentId, "board");
        Board otherBoard = dataHelper.setUpBoard(departmentAdministrator, departmentId, "other-board");

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        User otherBoardPostAdministrator = dataHelper.setUpUser(
            "other-board-post", "administrator", "other-board-post@administrator.hr");
        dataHelper.setUpPost(otherBoardPostAdministrator, otherBoard.getId(), "other-board-post");

        User departmentMember =
            dataHelper.setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User otherDepartmentAdministrator = dataHelper.setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment =
            dataHelper.setUpDepartment(otherDepartmentAdministrator, 1L,"other-department");
        Board otherDepartmentBoard = dataHelper.setUpBoard(
            otherDepartmentAdministrator, otherDepartment.getId(), "other-department-board");

        User otherDepartmentAuthor =
            dataHelper.setUpUser("other-department", "author", "other-department@author.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            dataHelper.setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = dataHelper.setUpUser("other-department-post",
            "administrator", "other-department-post@administrator.hr");
        dataHelper.setUpPost(
            otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), "other-department-post");

        User userWithoutRoles = dataHelper.setUpUser("without", "roles", "without@roles.hr");

        Scenarios scenarios = new Scenarios()
            .scenario(departmentMember, "Department member")
            .scenario(postAdministrator, "Post administrator")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentPostAdministrator, "Other department post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        resourceService.updateState(department, REJECTED);
        verify(scenarios, board, post, expectations);
    }

    @Test
    public void executeAction_departmentAdministratorActionsOnPost() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        ResourceModifier postPendingModifier = (resource) -> dataHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> dataHelper.setPostExpired((Post) resource);

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

        verify(departmentAdministrator, post, null, expectations);
    }

    @Test
    public void executeAction_departmentAdministratorActionsOnPostWhenParentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        ResourceModifier postPendingModifier = (resource) -> dataHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> dataHelper.setPostExpired((Post) resource);

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

        resourceService.updateState(department, REJECTED);
        verify(departmentAdministrator, post, null, expectations);

        resourceService.updateState(board, REJECTED);
        verify(departmentAdministrator, post, null, expectations);

        resourceService.updateState(department, ACCEPTED);
        verify(departmentAdministrator, post, null, expectations);
    }

    @Test
    public void executeAction_departmentMemberActionsOnPost() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User departmentMember =
            dataHelper.setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(PURSUE, ACCEPTED));

        verify(departmentMember, post, null, expectations);
    }

    @Test
    public void executeAction_departmentMemberActionsOnPostWhenDepartmentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User departmentMember =
            dataHelper.setUpUser("department", "member", "department@member.hr");
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        resourceService.updateState(department, REJECTED);
        verify(departmentMember, post, null, expectations);

        resourceService.updateState(board, REJECTED);
        verify(departmentMember, post, null, expectations);

        resourceService.updateState(department, ACCEPTED);
        verify(departmentMember, post, null, expectations);
    }

    @Test
    public void executeAction_postAdministratorActionsOnPost() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        ResourceModifier postPendingModifier = (resource) -> dataHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> dataHelper.setPostExpired((Post) resource);

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

    @Test
    public void executeAction_postAdministratorActionsOnPostWhenDepartmentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");
        Board board = dataHelper.setUpBoard(departmentAdministrator, department.getId(), "board");

        User postAdministrator =
            dataHelper.setUpUser("post", "administrator", "post@administrator.hr");
        Post post = dataHelper.setUpPost(postAdministrator, board.getId(), "post");

        ResourceModifier postPendingModifier = (resource) -> dataHelper.setPostPending((Post) resource);
        ResourceModifier postExpiredModifier = (resource) -> dataHelper.setPostExpired((Post) resource);

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

        resourceService.updateState(department, REJECTED);
        verify(postAdministrator, post, null, expectations);

        resourceService.updateState(board, REJECTED);
        verify(postAdministrator, post, null, expectations);

        resourceService.updateState(department, ACCEPTED);
        verify(postAdministrator, post, null, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnPost() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");

        Long departmentId = department.getId();
        Board board = dataHelper.setUpBoard(departmentAdministrator, departmentId, "board");
        Board otherBoard = dataHelper.setUpBoard(departmentAdministrator, departmentId, "other-board");

        Post post = dataHelper.setUpPost(departmentAdministrator, board.getId(), "post");
        User otherBoardPostAdministrator = dataHelper.setUpUser(
            "other-board-post", "administrator", "other-board-post@administrator.hr");
        dataHelper.setUpPost(otherBoardPostAdministrator, otherBoard.getId(), "other-board-post");

        User departmentAuthor =
            dataHelper.setUpUser("department", "author", "department@author.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User otherDepartmentAdministrator = dataHelper.setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment =
            dataHelper.setUpDepartment(otherDepartmentAdministrator, 1L,"other-department");
        Board otherDepartmentBoard = dataHelper.setUpBoard(
            otherDepartmentAdministrator, otherDepartment.getId(), "other-department-board");

        User otherDepartmentAuthor =
            dataHelper.setUpUser("other-department", "author", "other-department@author.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            dataHelper.setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = dataHelper.setUpUser("other-department-post",
            "administrator", "other-department-post@administrator.hr");
        dataHelper.setUpPost(
            otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), "other-department-post");

        User userWithoutRoles = dataHelper.setUpUser("without", "roles", "without@roles.hr");

        Scenarios scenarios = new Scenarios()
            .scenario(departmentAuthor, "Department author")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentPostAdministrator, "Other department post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(scenarios, post, null, expectations);
    }

    @Test
    public void executeAction_unprivilegedActionsOnPostWhenParentRejected() {
        User departmentAdministrator =
            dataHelper.setUpUser("department", "administrator", "department@administrator.hr");
        Department department = dataHelper.setUpDepartment(departmentAdministrator, 1L,"department");

        Long departmentId = department.getId();
        Board board = dataHelper.setUpBoard(departmentAdministrator, departmentId, "board");
        Board otherBoard = dataHelper.setUpBoard(departmentAdministrator, departmentId, "other-board");

        Post post = dataHelper.setUpPost(departmentAdministrator, board.getId(), "post");
        User otherBoardPostAdministrator = dataHelper.setUpUser(
            "other-board-post", "administrator", "other-board-post@administrator.hr");
        dataHelper.setUpPost(otherBoardPostAdministrator, otherBoard.getId(), "other-board-post");

        User departmentAuthor =
            dataHelper.setUpUser("department", "author", "department@author.hr");
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User otherDepartmentAdministrator = dataHelper.setUpUser(
            "other-department", "administrator", "other-department@administrator.hr");
        Department otherDepartment =
            dataHelper.setUpDepartment(otherDepartmentAdministrator, 1L,"other-department");
        Board otherDepartmentBoard = dataHelper.setUpBoard(
            otherDepartmentAdministrator, otherDepartment.getId(), "other-department-board");

        User otherDepartmentAuthor =
            dataHelper.setUpUser("other-department", "author", "other-department@author.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember =
            dataHelper.setUpUser("other-department", "member", "other-department@member.hr");
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = dataHelper.setUpUser("other-department-post",
            "administrator", "other-department-post@administrator.hr");
        dataHelper.setUpPost(
            otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), "other-department-post");

        User userWithoutRoles = dataHelper.setUpUser("without", "roles", "without@roles.hr");

        Scenarios scenarios = new Scenarios()
            .scenario(departmentAuthor, "Department author")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentPostAdministrator, "Other department post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        resourceService.updateState(department, REJECTED);
        verify(scenarios, post, null, expectations);

        resourceService.updateState(board, REJECTED);
        verify(scenarios, post, null, expectations);

        resourceService.updateState(department, ACCEPTED);
        verify(scenarios, post, null, expectations);
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
            Optional.ofNullable(expectations.getModifier(state))
                .ifPresent((modifier) -> modifier.modify(resource));

            Resource testResource = resourceService.getResource(user, resource.getScope(), resource.getId());
            for (Action action : Action.values()) {
                LOGGER.info("Executing " + action + " on " + testResource.getScope() + " in " + state);
                Resource executeResource = action == EXTEND ? extendResource : testResource;

                Expectation expectation = expectations.expected(state, action);
                if (expectation == null) {
                    verifyForbiden(user, testResource, action, executeResource);
                } else {
                    verifyPermitted(user, testResource, action, executeResource, expectation);
                }
            }
        }
    }

    private void verifyForbiden(User user, Resource testResource, Action action, Resource executeResource) {
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

    private static class Scenarios {

        private List<Scenario> scenarios = new ArrayList<>();

        private Scenarios scenario(User user, String description) {
            scenarios.add(new Scenario(user, description));
            return this;
        }

        private void forEach(Consumer<Scenario> consumer) {
            scenarios.forEach(consumer);
        }

    }

    private static class Scenario {

        private User user;

        private String description;

        private Scenario(User user, String description) {
            this.user = user;
            this.description = description;
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

    private interface ResourceModifier {

        void modify(Resource resource);

    }

}
