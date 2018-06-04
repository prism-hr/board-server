package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.OrganizationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.validation.PostValidator;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.workflow.Execution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.ResourceTask.POST_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.enums.State.PENDING;
import static hr.prism.board.exception.ExceptionCode.*;
import static java.math.BigDecimal.ONE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:data/postService_setUp.sql")
@Sql(scripts = "classpath:data/postService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class PostServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceIT.class);

    @Inject
    private PostService postService;

    @Inject
    private PostValidator postValidator;

    @Inject
    private ServiceHelper serviceHelper;

    @SpyBean
    private ActionService actionService;

    @SpyBean
    private OrganizationService organizationService;

    @SpyBean
    private LocationService locationService;

    @SpyBean
    private UserService userService;

    @SpyBean
    private ResourceService resourceService;

    @SpyBean
    private UserRoleService userRoleService;

    @SpyBean
    private ResourceTaskService resourceTaskService;

    @SpyBean
    private DocumentService documentService;

    @Before
    public void setUp() {
        reset(postValidator, actionService, resourceService);
    }

    @After
    public void tearDown() {
        reset(postValidator, actionService, resourceService);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostDraftAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 4L, board,
            "department-accepted-board-accepted-post-draft",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostDraftAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 4L, board,
            "department-accepted-board-accepted-post-draft",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostDraftAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(4L);

        verifyGetByIdFailure(users, 4L, post);
        verifyGetByIdFailure((User) null, 4L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 5L, board,
            "department-accepted-board-accepted-post-pending",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostPendingAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 5L, board,
            "department-accepted-board-accepted-post-pending",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostPendingAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(5L);

        verifyGetByIdFailure(users, 5L, post);
        verifyGetByIdFailure((User) null, 5L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 6L, board,
            "department-accepted-board-accepted-post-accepted",
            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostAcceptedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 6L, board,
            "department-accepted-board-accepted-post-accepted",
            new Action[]{VIEW, EDIT, PURSUE, WITHDRAW});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostAcceptedAndDepartmentMember() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 6L, board,
            "department-accepted-board-accepted-post-accepted", new Action[]{VIEW, PURSUE});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostAcceptedAndUnprivileged() {
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

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 6L, board,
            "department-accepted-board-accepted-post-accepted", new Action[]{VIEW});
        verifyGetById((User) null, 6L, board,
            "department-accepted-board-accepted-post-accepted", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostExpiredAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 7L, board,
            "department-accepted-board-accepted-post-expired",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostExpiredAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 7L, board,
            "department-accepted-board-accepted-post-expired",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostExpiredAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(7L);

        verifyGetByIdFailure(users, 7L, post);
        verifyGetByIdFailure((User) null, 7L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostSuspendedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 8L, board,
            "department-accepted-board-accepted-post-suspended",
            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostSuspendedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 8L, board,
            "department-accepted-board-accepted-post-suspended",
            new Action[]{VIEW, EDIT, CORRECT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostSuspendedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(8L);

        verifyGetByIdFailure(users, 8L, post);
        verifyGetByIdFailure((User) null, 8L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 9L, board,
            "department-accepted-board-accepted-post-rejected",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostRejectedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 9L, board,
            "department-accepted-board-accepted-post-rejected",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(9L);

        verifyGetByIdFailure(users, 9L, post);
        verifyGetByIdFailure((User) null, 9L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostWithdrawnAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 10L, board,
            "department-accepted-board-accepted-post-withdrawn",
            new Action[]{VIEW, EDIT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostWithdrawnAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 10L, board,
            "department-accepted-board-accepted-post-withdrawn",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostWithdrawnAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(10L);

        verifyGetByIdFailure(users, 10L, post);
        verifyGetByIdFailure((User) null, 10L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostArchivedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 11L, board,
            "department-accepted-board-accepted-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostArchivedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(user, 11L, board,
            "department-accepted-board-accepted-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardAcceptedAndPostArchivedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(11L);

        verifyGetByIdFailure(users, 11L, post);
        verifyGetByIdFailure((User) null, 11L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostDraftAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 13L, board,
            "department-accepted-board-rejected-post-draft",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostDraftAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 13L, board,
            "department-accepted-board-rejected-post-draft",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostDraftAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(13L);

        verifyGetByIdFailure(users, 13L, post);
        verifyGetByIdFailure((User) null, 13L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 14L, board,
            "department-accepted-board-rejected-post-pending",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostPendingAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 14L, board,
            "department-accepted-board-rejected-post-pending",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostPendingAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(14L);

        verifyGetByIdFailure(users, 14L, post);
        verifyGetByIdFailure((User) null, 14L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 15L, board,
            "department-accepted-board-rejected-post-accepted",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostAcceptedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 15L, board,
            "department-accepted-board-rejected-post-accepted",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 15L, board,
            "department-accepted-board-rejected-post-accepted", new Action[]{VIEW});
        verifyGetById((User) null, 15L, board,
            "department-accepted-board-rejected-post-accepted", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostExpiredAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 16L, board,
            "department-accepted-board-rejected-post-expired",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostExpiredAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 16L, board,
            "department-accepted-board-rejected-post-expired",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostExpiredAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(16L);

        verifyGetByIdFailure(users, 16L, post);
        verifyGetByIdFailure((User) null, 16L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostSuspendedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 17L, board,
            "department-accepted-board-rejected-post-suspended",
            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostSuspendedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 17L, board,
            "department-accepted-board-rejected-post-suspended",
            new Action[]{VIEW, EDIT, CORRECT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostSuspendedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(17L);

        verifyGetByIdFailure(users, 17L, post);
        verifyGetByIdFailure((User) null, 17L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 18L, board,
            "department-accepted-board-rejected-post-rejected",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostRejectedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 18L, board,
            "department-accepted-board-rejected-post-rejected",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(18L);

        verifyGetByIdFailure(users, 18L, post);
        verifyGetByIdFailure((User) null, 18L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostWithdrawnAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 19L, board,
            "department-accepted-board-rejected-post-withdrawn",
            new Action[]{VIEW, EDIT});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostWithdrawnAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 19L, board,
            "department-accepted-board-rejected-post-withdrawn",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostWithdrawnAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(19L);

        verifyGetByIdFailure(users, 19L, post);
        verifyGetByIdFailure((User) null, 19L, post);
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostArchivedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(users, 20L, board,
            "department-accepted-board-rejected-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentAcceptedBoardRejectedAndPostArchivedAndPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-rejected");
        verifyGetById(user, 20L, board,
            "department-accepted-board-rejected-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAcceptedBoardRejectedAndPostArchivedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-post-administrator@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(20L);

        verifyGetByIdFailure(users, 20L, post);
        verifyGetByIdFailure((User) null, 20L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostDraftAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 23L, board,
            "department-rejected-board-accepted-post-draft",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostDraftAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 23L, board,
            "department-rejected-board-accepted-post-draft",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostDraftAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(23L);

        verifyGetByIdFailure(users, 23L, post);
        verifyGetByIdFailure((User) null, 23L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 24L, board,
            "department-rejected-board-accepted-post-pending",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostPendingAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 24L, board,
            "department-rejected-board-accepted-post-pending",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostPendingAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(24L);

        verifyGetByIdFailure(users, 24L, post);
        verifyGetByIdFailure((User) null, 24L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 25L, board,
            "department-rejected-board-accepted-post-accepted",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostAcceptedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 25L, board,
            "department-rejected-board-accepted-post-accepted",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 25L, board,
            "department-rejected-board-accepted-post-accepted", new Action[]{VIEW});
        verifyGetById((User) null, 25L, board,
            "department-rejected-board-accepted-post-accepted", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostExpiredAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 26L, board,
            "department-rejected-board-accepted-post-expired",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostExpiredAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 26L, board,
            "department-rejected-board-accepted-post-expired",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostExpiredAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(26L);

        verifyGetByIdFailure(users, 26L, post);
        verifyGetByIdFailure((User) null, 26L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostSuspendedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 27L, board,
            "department-rejected-board-accepted-post-suspended",
            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostSuspendedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 27L, board,
            "department-rejected-board-accepted-post-suspended",
            new Action[]{VIEW, EDIT, CORRECT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostSuspendedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(27L);

        verifyGetByIdFailure(users, 27L, post);
        verifyGetByIdFailure((User) null, 27L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 28L, board,
            "department-rejected-board-accepted-post-rejected",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostRejectedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 28L, board,
            "department-rejected-board-accepted-post-rejected",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(28L);

        verifyGetByIdFailure(users, 28L, post);
        verifyGetByIdFailure((User) null, 28L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostWithdrawnAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 29L, board,
            "department-rejected-board-accepted-post-withdrawn",
            new Action[]{VIEW, EDIT});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostWithdrawnAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 29L, board,
            "department-rejected-board-accepted-post-withdrawn",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostWithdrawnAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(29L);

        verifyGetByIdFailure(users, 29L, post);
        verifyGetByIdFailure((User) null, 29L, post);
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostArchivedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(users, 30L, board,
            "department-rejected-board-accepted-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentRejectedBoardAcceptedAndPostArchivedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-accepted");
        verifyGetById(user, 30L, board,
            "department-rejected-board-accepted-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentRejectedBoardAcceptedAndPostArchivedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(30L);

        verifyGetByIdFailure(users, 30L, post);
        verifyGetByIdFailure((User) null, 30L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostDraftAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 32L, board,
            "department-rejected-board-rejected-post-draft",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostDraftAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 32L, board,
            "department-rejected-board-rejected-post-draft",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostDraftAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(32L);

        verifyGetByIdFailure(users, 32L, post);
        verifyGetByIdFailure((User) null, 32L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 33L, board,
            "department-rejected-board-rejected-post-pending",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostPendingAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 33L, board,
            "department-rejected-board-rejected-post-pending",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostPendingAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(33L);

        verifyGetByIdFailure(users, 33L, post);
        verifyGetByIdFailure((User) null, 33L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostAcceptedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 34L, board,
            "department-rejected-board-rejected-post-accepted",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostAcceptedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 34L, board,
            "department-rejected-board-rejected-post-accepted",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostAcceptedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 34L, board,
            "department-rejected-board-rejected-post-accepted", new Action[]{VIEW});
        verifyGetById((User) null, 34L, board,
            "department-rejected-board-rejected-post-accepted", new Action[]{VIEW});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostExpiredAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 35L, board,
            "department-rejected-board-rejected-post-expired",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostExpiredAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 35L, board,
            "department-rejected-board-rejected-post-expired",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostExpiredAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(35L);

        verifyGetByIdFailure(users, 35L, post);
        verifyGetByIdFailure((User) null, 35L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostSuspendedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 36L, board,
            "department-rejected-board-rejected-post-suspended",
            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostSuspendedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 36L, board,
            "department-rejected-board-rejected-post-suspended",
            new Action[]{VIEW, EDIT, CORRECT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostSuspendedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(36L);

        verifyGetByIdFailure(users, 36L, post);
        verifyGetByIdFailure((User) null, 36L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostRejectedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 37L, board,
            "department-rejected-board-rejected-post-rejected",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostRejectedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 37L, board,
            "department-rejected-board-rejected-post-rejected",
            new Action[]{VIEW, EDIT, WITHDRAW});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostRejectedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(37L);

        verifyGetByIdFailure(users, 37L, post);
        verifyGetByIdFailure((User) null, 37L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostWithdrawnAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 38L, board,
            "department-rejected-board-rejected-post-withdrawn",
            new Action[]{VIEW, EDIT});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostWithdrawnAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 38L, board,
            "department-rejected-board-rejected-post-withdrawn",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostWithdrawnAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(38L);

        verifyGetByIdFailure(users, 38L, post);
        verifyGetByIdFailure((User) null, 38L, post);
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostArchivedAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-rejected-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(users, 39L, board,
            "department-rejected-board-rejected-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_successWhenDepartmentAndBoardRejectedAndPostArchivedAndPostAdministrator() {
        User user = userService.getByEmail("department-rejected-post-administrator@prism.hr");

        Board board = (Board) resourceService.getByHandle("university/department-rejected/board-rejected");
        verifyGetById(user, 39L, board,
            "department-rejected-board-rejected-post-archived",
            new Action[]{VIEW, EDIT, RESTORE});
    }

    @Test
    public void getById_failureWhenDepartmentAndBoardRejectedAndPostArchivedAndUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-pending@prism.hr"),
            userService.getByEmail("department-accepted-member-accepted@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-post-administrator@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-pending@prism.hr"),
            userService.getByEmail("department-rejected-member-accepted@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        Post post = new Post();
        post.setId(39L);

        verifyGetByIdFailure(users, 39L, post);
        verifyGetByIdFailure((User) null, 39L, post);
    }

    @Test
    public void createPost_successWhenApplyWebsite() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");
        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        OrganizationDTO organizationDTO = makeOrganizationDTO();
        LocationDTO locationDTO = makeLocationDTO();

        Post createdPost = setUpPost(user, board.getId(),
            organizationDTO, locationDTO, "http://www.google.co.uk", null, null);

        Organization organization = makeOrganization();
        Location location = makeLocation();

        Post selectedPost = postService.getById(user, createdPost.getId(), "ip", true);
        Stream.of(createdPost, selectedPost).forEach(post ->
            verifyCreatePost(post, board, organization, location,
                "http://www.google.co.uk", null, null));

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyInvocations(user, createdPost, board, department, organizationDTO, organization, locationDTO, location);
    }

    @Test
    public void createPost_successWhenApplyDocument() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");
        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        OrganizationDTO organizationDTO = makeOrganizationDTO();
        LocationDTO locationDTO = makeLocationDTO();

        DocumentDTO documentApplyDTO =
            new DocumentDTO()
                .setCloudinaryId("cloudinary id")
                .setCloudinaryUrl("cloudinary url")
                .setFileName("file name");

        Post createdPost = setUpPost(user, board.getId(),
            organizationDTO, locationDTO, null, documentApplyDTO, null);

        Post selectedPost = postService.getById(user, createdPost.getId(), "ip", true);

        Organization organization = makeOrganization();
        Location location = makeLocation();

        Document documentApply = new Document();
        documentApply.setCloudinaryId("cloudinary id");

        Stream.of(createdPost, selectedPost).forEach(post ->
            verifyCreatePost(post, board, organization, location,
                null, documentApply, null));

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyInvocations(user, createdPost, board, department, organizationDTO, organization, locationDTO, location);
        verify(documentService, times(1)).getOrCreateDocument(documentApplyDTO);
    }

    @Test
    public void createPost_successWhenApplyEmail() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");
        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        OrganizationDTO organizationDTO = makeOrganizationDTO();
        LocationDTO locationDTO = makeLocationDTO();

        Post createdPost = setUpPost(user, board.getId(),
            organizationDTO, locationDTO, null, null, "author@prism.hr");

        Organization organization = makeOrganization();
        Location location = makeLocation();

        Post selectedPost = postService.getById(user, createdPost.getId(), "ip", true);
        Stream.of(createdPost, selectedPost).forEach(post ->
            verifyCreatePost(post, board, organization, location,
                null, null, "author@prism.hr"));

        Department department = (Department) resourceService.getByHandle("university/department-accepted");
        verifyInvocations(user, createdPost, board, department, organizationDTO, organization, locationDTO, location);
    }

    @Test
    public void getPosts_successWhenDepartmentAdministrator() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter());
        assertThat(posts).hasSize(32);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(0, 8),
            departmentRejectedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(8, 16),
            departmentRejectedBoardAccepted, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(16, 24),
            departmentAcceptedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(24, 32),
            departmentAcceptedBoardAccepted, new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT});
    }

    @Test
    public void getPosts_successWhenDepartmentAdministratorAndState() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setState(PENDING));
        assertThat(posts).hasSize(4);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyPost(posts.get(0), departmentRejectedBoardRejected,
            "department-rejected-board-rejected-post-pending", new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyPost(posts.get(1), departmentRejectedBoardAccepted,
            "department-rejected-board-accepted-post-pending", new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyPost(posts.get(2), departmentAcceptedBoardRejected,
            "department-accepted-board-rejected-post-pending", new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyPost(posts.get(3), departmentAcceptedBoardAccepted,
            "department-accepted-board-accepted-post-pending", new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getPosts_successWhenDepartmentAdministratorAndDepartment() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setParentId(21L));
        assertThat(posts).hasSize(16);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(0, 8),
            departmentRejectedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(8, 16),
            departmentRejectedBoardAccepted, new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getPosts_successWhenDepartmentAdministratorAndBoard() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setParentId(31L));
        assertThat(posts).hasSize(8);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts, departmentRejectedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});
    }

    @Test
    public void getPosts_successWhenDepartmentAdministratorAndAction() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setAction(PURSUE));
        assertThat(posts).hasSize(1);

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyPost(posts.get(0), departmentAcceptedBoardAccepted,
            "department-accepted-board-accepted-post-accepted",
            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT});
    }

    @Test
    public void getPosts_successWhenDepartmentAdministratorAndSearchTerm() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setSearchTerm("rejected"));
        assertThat(posts).hasSize(25);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(0, 8),
            departmentRejectedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(8, 16),
            departmentRejectedBoardAccepted, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(16, 24),
            departmentAcceptedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyPost(posts.get(24), departmentAcceptedBoardAccepted,
            "department-accepted-board-accepted-post-rejected",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
    }

    @Test
    public void getPosts_successWhenDepartmentAdministratorAndSearchTermTypo() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setSearchTerm("rIJECT"));
        assertThat(posts).hasSize(25);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(0, 8),
            departmentRejectedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(8, 16),
            departmentRejectedBoardAccepted, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(16, 24),
            departmentAcceptedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyPost(posts.get(24), departmentAcceptedBoardAccepted,
            "department-accepted-board-accepted-post-rejected",
            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
    }

    @Test
    public void getPosts_failureWhenDepartmentAdministratorAndSearchTermTypo() {
        User user = userService.getByEmail("department-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter().setSearchTerm("xyz"));
        assertThat(posts).hasSize(0);
    }

    @Test
    public void getPosts_successWhenOtherDepartmentAdministrator() {
        User user = userService.getByEmail("department-rejected-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter());
        assertThat(posts).hasSize(18);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(0, 8),
            departmentRejectedBoardRejected, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyGetBoardPostsDepartmentAdministrator(posts.subList(8, 16),
            departmentRejectedBoardAccepted, new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyPost(posts.get(16), departmentAcceptedBoardRejected,
            "department-accepted-board-rejected-post-accepted", new Action[]{VIEW});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyPost(posts.get(17), departmentAcceptedBoardAccepted,
            "department-accepted-board-accepted-post-accepted", new Action[]{VIEW});
    }

    @Test
    public void getPosts_successWhenPostAdministrator() {
        User user = userService.getByEmail("department-accepted-post-administrator@prism.hr");
        List<Post> posts = postService.getPosts(user, new ResourceFilter());
        assertThat(posts).hasSize(18);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyPost(posts.get(0), departmentRejectedBoardRejected,
            "department-rejected-board-rejected-post-accepted", new Action[]{VIEW});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyPost(posts.get(1), departmentRejectedBoardAccepted,
            "department-rejected-board-accepted-post-accepted", new Action[]{VIEW});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyGetBoardPostsPostAdministrator(posts.subList(2, 10),
            departmentAcceptedBoardRejected, new Action[]{VIEW, EDIT, WITHDRAW});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyGetBoardPostsPostAdministrator(posts.subList(10, 18),
            departmentAcceptedBoardAccepted, new Action[]{VIEW, EDIT, PURSUE, WITHDRAW});
    }

    @Test
    public void getPosts_successWhenDepartmentMember() {
        User[] users = new User[]{
            userService.getByEmail("department-member-pending@prism.hr"),
            userService.getByEmail("department-member-accepted@prism.hr")};

        verifyGetPosts(users, new Action[]{VIEW, PURSUE});
    }

    @Test
    public void getPosts_successWhenUnprivileged() {
        User[] users = new User[]{
            userService.getByEmail("department-author@prism.hr"),
            userService.getByEmail("department-member-rejected@prism.hr"),
            userService.getByEmail("department-accepted-author@prism.hr"),
            userService.getByEmail("department-accepted-member-rejected@prism.hr"),
            userService.getByEmail("department-rejected-author@prism.hr"),
            userService.getByEmail("department-rejected-member-rejected@prism.hr"),
            userService.getByEmail("no-roles@prism.hr")};

        verifyGetPosts(users, new Action[]{VIEW});
        verifyGetPosts((User) null, new Action[]{VIEW});
    }

    private void verifyGetById(User[] users, Long id, Board expectedBoard, String expectedName,
                               Action[] expectedActions) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetById(user, id, expectedBoard, expectedName, expectedActions);
        });
    }

    private void verifyGetById(User user, Long id, Board expectedBoard, String expectedName,
                               Action[] expectedActions) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by id: " + id + ": " + userGivenName);

        Post post = postService.getById(user, id, "ip", true);

        verifyPost(post, expectedBoard, expectedName, expectedActions);
        verifyInvocations(user, id, post);
    }

    private void verifyGetByIdFailure(User[] users, Long id, Post post) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetByIdFailure(user, id, post);
        });
    }

    private void verifyGetByIdFailure(User user, Long id, Post post) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Get by id: " + id + ": " + userGivenName);

        assertThatThrownBy(() -> postService.getById(user, id, "ip", true))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);

        verifyInvocations(user, id, post);
    }

    private void verifyPost(Post post, Board expectedBoard, String expectedName,
                            Action[] expectedActions) {
        serviceHelper.verifyIdentity(post, expectedBoard, expectedName);
        serviceHelper.verifyActions(post, expectedActions);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyCreatePost(Post post, Board expectedBoard, Organization expectedOrganization,
                                  Location expectedLocation, String expectedApplyWebsite,
                                  Document expectedApplyDocument, String expectApplyEmail) {
        verifyPost(post, expectedBoard,
            "department-accepted-board-accepted-post", new Action[]{VIEW, EDIT, WITHDRAW});

        assertEquals("summary", post.getSummary());
        assertEquals("description", post.getDescription());
        assertEquals(expectedOrganization, post.getOrganization());
        assertEquals(expectedLocation, post.getLocation());
        assertEquals(expectedApplyWebsite, post.getApplyWebsite());
        assertEquals(expectedApplyDocument, post.getApplyDocument());
        assertEquals(expectApplyEmail, post.getApplyEmail());
        assertEquals(STUDENT, post.getExistingRelation());
        assertEquals("{\n  \"studyLevel\" : \"MASTER\"\n}", post.getExistingRelationExplanation());
        assertEquals(DRAFT, post.getState());
        assertEquals(DRAFT, post.getPreviousState());

        assertEquals(
            LocalDateTime.of(2050, 5, 1, 0, 0, 0),
            post.getLiveTimestamp());

        assertEquals(
            LocalDateTime.of(2050, 5, 30, 0, 0, 0),
            post.getDeadTimestamp());

        assertEquals(ImmutableList.of("Employment", "Internship"), post.getPostCategoryStrings());
        assertEquals(
            toStrings(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT)), post.getMemberCategoryStrings());
    }

    @SuppressWarnings("Duplicates")
    private void verifyGetBoardPostsDepartmentAdministrator(List<Post> posts, Board expectedBoard,
                                                            Action[] expectedAcceptedActions) {
        String expectedBoardName = expectedBoard.getName();
        verifyPost(posts.get(0), expectedBoard,
            expectedBoardName + "-post-archived", new Action[]{VIEW, EDIT, RESTORE});

        verifyPost(posts.get(1), expectedBoard,
            expectedBoardName + "-post-withdrawn", new Action[]{VIEW, EDIT});

        verifyPost(posts.get(2), expectedBoard,
            expectedBoardName + "-post-rejected", new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});

        verifyPost(posts.get(3), expectedBoard,
            expectedBoardName + "-post-suspended", new Action[]{VIEW, EDIT, ACCEPT, REJECT});

        verifyPost(posts.get(4), expectedBoard,
            expectedBoardName + "-post-expired", new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        verifyPost(posts.get(5), expectedBoard,
            expectedBoardName + "-post-accepted", expectedAcceptedActions);

        verifyPost(posts.get(6), expectedBoard,
            expectedBoardName + "-post-pending", new Action[]{VIEW, EDIT, SUSPEND, REJECT});

        verifyPost(posts.get(7), expectedBoard,
            expectedBoardName + "-post-draft", new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
    }

    @SuppressWarnings("Duplicates")
    private void verifyGetBoardPostsPostAdministrator(List<Post> posts, Board expectedBoard,
                                                      Action[] expectedAcceptedActions) {
        String expectedBoardName = expectedBoard.getName();
        verifyPost(posts.get(0), expectedBoard,
            expectedBoardName + "-post-archived", new Action[]{VIEW, EDIT, RESTORE});

        verifyPost(posts.get(1), expectedBoard,
            expectedBoardName + "-post-withdrawn", new Action[]{VIEW, EDIT, RESTORE});

        verifyPost(posts.get(2), expectedBoard,
            expectedBoardName + "-post-rejected", new Action[]{VIEW, EDIT, WITHDRAW});

        verifyPost(posts.get(3), expectedBoard,
            expectedBoardName + "-post-suspended", new Action[]{VIEW, EDIT, CORRECT, WITHDRAW});

        verifyPost(posts.get(4), expectedBoard,
            expectedBoardName + "-post-expired", new Action[]{VIEW, EDIT, WITHDRAW});

        verifyPost(posts.get(5), expectedBoard,
            expectedBoardName + "-post-accepted", expectedAcceptedActions);

        verifyPost(posts.get(6), expectedBoard,
            expectedBoardName + "-post-pending", new Action[]{VIEW, EDIT, WITHDRAW});

        verifyPost(posts.get(7), expectedBoard,
            expectedBoardName + "-post-draft", new Action[]{VIEW, EDIT, WITHDRAW});
    }

    private void verifyGetPosts(User[] users, Action[] expectedAcceptedActions) {
        Stream.of(users).forEach(user -> {
            assertNotNull(user);
            verifyGetPosts(user, expectedAcceptedActions);
        });
    }

    private void verifyGetPosts(User user, Action[] expectedAcceptedActions) {
        String userGivenName = serviceHelper.getUserGivenName(user);
        LOGGER.info("Getting posts: " + userGivenName);

        List<Post> posts = postService.getPosts(user, new ResourceFilter());
        assertThat(posts).hasSize(4);

        Board departmentRejectedBoardRejected =
            (Board) resourceService.getByHandle("university/department-rejected/board-rejected");

        verifyPost(posts.get(0), departmentRejectedBoardRejected,
            "department-rejected-board-rejected-post-accepted", new Action[]{VIEW});

        Board departmentRejectedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-rejected/board-accepted");

        verifyPost(posts.get(1), departmentRejectedBoardAccepted,
            "department-rejected-board-accepted-post-accepted", new Action[]{VIEW});

        Board departmentAcceptedBoardRejected =
            (Board) resourceService.getByHandle("university/department-accepted/board-rejected");

        verifyPost(posts.get(2), departmentAcceptedBoardRejected,
            "department-accepted-board-rejected-post-accepted", new Action[]{VIEW});

        Board departmentAcceptedBoardAccepted =
            (Board) resourceService.getByHandle("university/department-accepted/board-accepted");

        verifyPost(posts.get(3), departmentAcceptedBoardAccepted,
            "department-accepted-board-accepted-post-accepted", expectedAcceptedActions);
    }

    private Post setUpPost(User user, Long boardId, OrganizationDTO organizationDTO, LocationDTO locationDTO,
                           String applyWebsite, DocumentDTO applyDocument, String applyEmail) {
        return postService.createPost(user, boardId,
            new PostDTO()
                .setName("department-accepted-board-accepted-post")
                .setSummary("summary")
                .setDescription("description")
                .setOrganization(organizationDTO)
                .setLocation(locationDTO)
                .setApplyWebsite(applyWebsite)
                .setApplyDocument(applyDocument)
                .setApplyEmail(applyEmail)
                .setPostCategories(ImmutableList.of("Employment", "Internship"))
                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))
                .setExistingRelation(STUDENT)
                .setExistingRelationExplanation(ImmutableMap.of("studyLevel", "MASTER"))
                .setLiveTimestamp(LocalDateTime.of(2050, 5, 1, 0, 0, 0))
                .setDeadTimestamp(LocalDateTime.of(2050, 5, 30, 0, 0, 0)));
    }

    private Organization makeOrganization() {
        Organization expectedOrganization = new Organization();
        expectedOrganization.setName("organization");
        return expectedOrganization;
    }

    private OrganizationDTO makeOrganizationDTO() {
        return new OrganizationDTO()
            .setName("organization");
    }

    private Location makeLocation() {
        Location expectedLocation = new Location();
        expectedLocation.setGoogleId("google");
        return expectedLocation;
    }

    private LocationDTO makeLocationDTO() {
        return new LocationDTO()
            .setName("london")
            .setDomicile("uk")
            .setGoogleId("google")
            .setLatitude(ONE)
            .setLongitude(ONE);
    }

    private void verifyInvocations(User user, Long id, Post post) {
        verify(resourceService, times(1))
            .getResource(user, POST, id);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(VIEW), any(Execution.class));
    }

    private void verifyInvocations(User user, Post post, Board board, Department department,
                                   OrganizationDTO organizationDTO, Organization organization, LocationDTO locationDTO,
                                   Location location) {
        verify(actionService, times(1))
            .executeAction(eq(user), eq(board), eq(EXTEND), any(Execution.class));

        verify(organizationService, times(1)).getOrCreateOrganization(organizationDTO);
        verify(locationService, times(1)).getOrCreateLocation(locationDTO);

        verify(userService, times(1))
            .updateUserOrganizationAndLocation(user, organization, location);

        verify(postValidator, times(1)).checkApply(post);
        verify(resourceService, times(1)).createResourceRelation(board, post);

        List<String> postCategories = ImmutableList.of("Employment", "Internship");
        verify(postValidator, times(1)).checkCategories(
            postCategories, emptyList(),
            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES);

        verify(resourceService, times(1))
            .updateCategories(post, CategoryType.POST, postCategories);

        List<String> memberCategories = toStrings(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT));
        verify(postValidator, times(1)).checkCategories(
            memberCategories, emptyList(),
            FORBIDDEN_MEMBER_CATEGORIES, MISSING_MEMBER_CATEGORIES, INVALID_MEMBER_CATEGORIES);
        verify(resourceService, times(1)).updateCategories(post, MEMBER, memberCategories);

        verify(resourceService, times(1)).setIndexDataAndQuarter(post);
        verify(userRoleService, times(1)).createUserRole(post, user, ADMINISTRATOR);
        verify(resourceTaskService, times(1)).completeTasks(department, POST_TASKS);
        verify(postValidator, times(1)).checkExistingRelation(post);
    }

}
