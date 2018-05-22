package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.service.ServiceHelper.ResourceModifier;
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
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/actionService_setUp.sql")
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

    @Test
    public void executeAction_successWhenDepartmentAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

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

        verify(users, department, board, expectations);
    }

    @Test
    public void executeAction_successWhenDepartmentAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        Expectations expectations =
            new Expectations()
                .expect(DRAFT,
                    new Expectation(VIEW, DRAFT))
                .expect(PENDING,
                    new Expectation(VIEW, PENDING))
                .expect(ACCEPTED,
                    new Expectation(VIEW, ACCEPTED));

        verify(users, department, board, expectations);
        verify((User) null, department, board, expectations);
    }

    @Test
    public void executeAction_successWhenBoardAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(EXTEND, ACCEPTED),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(RESTORE, ACCEPTED));

        verify(users, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardAndDepartmentAuthor() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EXTEND, ACCEPTED));

        verify(users, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EXTEND, DRAFT));

        verify(users, board, post, expectations);
        verify((User) null, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardDepartmentAdministratorAndDepartmentRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(EDIT, ACCEPTED),
                new Expectation(REJECT, REJECTED))
            .expect(REJECTED,
                new Expectation(VIEW, REJECTED),
                new Expectation(EDIT, REJECTED),
                new Expectation(RESTORE, ACCEPTED));

        verify(users, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardDepartmentAuthorAndDepartmentRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenBoardUnprivilegedAndDepartmentRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, board, post, expectations);
        verify((User) null, board, post, expectations);
    }

    @Test
    public void executeAction_successWhenPostAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

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

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostAndDepartmentMember() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED),
                new Expectation(PURSUE, ACCEPTED));

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");
        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

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

        assertNotNull(user);
        verify(user, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
        verify((User) null, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostDepartmentAdministratorAndDepartmentRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

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

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostDepartmentMemberAndDepartmentRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostPostAdministratorAndDepartmentRejected() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");
        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

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

        assertNotNull(user);
        verify(user, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostUnprivilegedAndDepartmentRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-accepted/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
        verify((User) null, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostDepartmentAdministratorAndBoardRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-rejected/post-accepted");

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

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostDepartmentMemberAndBoardRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-rejected/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostPostAdministratorAndBoardRejected() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");
        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-rejected/post-accepted");

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

        assertNotNull(user);
        verify(user, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostUnprivilegedAndBoardRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-accepted/board-rejected/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
        verify((User) null, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostDepartmentAdministratorAndDepartmentAndBoardRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-rejected/post-accepted");

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

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostDepartmentMemberAndDepartmentAndBoardRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-rejected/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostPostAdministratorAndDepartmentAndBoardRejected() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");
        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-rejected/post-accepted");

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

        assertNotNull(user);
        verify(user, post, null, expectations);
    }

    @Test
    public void executeAction_successWhenPostUnprivilegedAndDepartmentAndBoardRejected() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = (Post) resourceService.getByHandle("university/department-rejected/board-rejected/post-accepted");

        Expectations expectations = new Expectations()
            .expect(ACCEPTED,
                new Expectation(VIEW, ACCEPTED));

        verify(users, post, null, expectations);
        verify((User) null, post, null, expectations);
    }

    private void verify(User[] users, Resource resource, Resource extendResource, Expectations expectations) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verify(user, resource, extendResource, expectations);
        });
    }

    private void verify(User user, Resource resource, Resource extendResource, Expectations expectations) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Verifying actions: " + userGivenName);

        for (State state : new State[]{DRAFT, SUSPENDED, PENDING, ACCEPTED, EXPIRED, REJECTED, WITHDRAWN, ARCHIVED}) {
            resourceService.updateState(resource, state);
            Optional.ofNullable(expectations.getModifier(state))
                .ifPresent((modifier) -> modifier.modify(resource));

            Resource testResource = resourceService.getResource(user, resource.getScope(), resource.getId());
            for (Action action : Action.values()) {
                LOGGER.info(action + " on " + testResource.getScope() + " in " + state + " as " + userGivenName);
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
