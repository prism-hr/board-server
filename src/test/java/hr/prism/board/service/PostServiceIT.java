package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.validation.PostValidator;
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
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private BoardService boardService;

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

    private void verifyInvocations(User user, Long id, Post post) {
        verify(resourceService, times(1))
            .getResource(user, POST, id);

        verify(actionService, times(1))
            .executeAction(eq(user), eq(post), eq(VIEW), any(Execution.class));
    }

//    @Test
//    public void createPost_successWhenApplyWebsite() {
//        Board board = departmentAcceptedBoards.get(0);
//        Post createdPost = setUpPost(board, "http://www.google.co.uk", null, null);
//
//        Post selectedPost = postService.getById(administrator, createdPost.getId(), "ip", true);
//        Stream.of(createdPost, selectedPost).forEach(post ->
//            verifyPost(post, board, "post", new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW},
//                "http://www.google.co.uk", null, null));
//
//        verifyInvocations(createdPost, board);
//    }
//
//    @Test
//    public void createPost_successWhenApplyDocument() {
//        DocumentDTO documentDTO =
//            new DocumentDTO()
//                .setCloudinaryId("cloudinary id")
//                .setCloudinaryUrl("cloudinary url")
//                .setFileName("file name");
//
//        Board board = departmentAcceptedBoards.get(0);
//        Post createdPost = setUpPost(board, null, documentDTO, null);
//
//        Post selectedPost = postService.getById(administrator, createdPost.getId(), "ip", true);
//
//        Document expectedDocument = new Document();
//        expectedDocument.setCloudinaryId("cloudinary id");
//
//        Stream.of(createdPost, selectedPost).forEach(post ->
//            verifyPost(post, board, "post", new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW},
//                null, expectedDocument, null));
//
//        verifyInvocations(createdPost, board);
//        verify(documentService, times(1)).getOrCreateDocument(documentDTO);
//    }
//
//    @Test
//    public void createPost_successWhenApplyEmail() {
//        Board board = departmentAcceptedBoards.get(0);
//        Post createdPost = setUpPost(board, null, null, "author@prism.hr");
//        Post selectedPost = postService.getById(administrator, createdPost.getId(), "ip", true);
//
//        Stream.of(createdPost, selectedPost).forEach(post ->
//            verifyPost(post, board, "post", new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW},
//                null, null, "author@prism.hr"));
//
//        verifyInvocations(createdPost, board);
//    }
//
//    @Test
//    public void getPosts_success() {
//        getPosts_successWhenAdministrator();
//        getPosts_successWhenAdministratorAndState();
//        getPosts_successWhenAdministratorAndDepartment();
//        getPosts_successWhenAdministratorAndBoard();
//        getPosts_successWhenAction();
//        getPosts_successWhenDepartmentAndSearchTerm();
//        getPosts_successWhenDepartmentAndSearchTermTypo();
//        getPosts_successWhenSearchTermWithoutResults();
//        getPosts_successWhenOtherAdministrator();
//        getPosts_successWhenPostAdministrator();
//        getPosts_successWhenMember();
//        getPosts_successWhenOtherMember();
//        getPosts_successWhenUnprivileged();
//    }
//
//    private void getPosts_successWhenAdministrator() {
//        List<Post> posts = postService.getPosts(administrator, new ResourceFilter());
//        assertThat(posts).hasSize(64);
//
//        verifyAdministratorPosts(
//            posts.subList(0, 16),
//            departmentRejectedBoards.get(1),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAdministratorPosts(
//            posts.subList(16, 32),
//            departmentRejectedBoards.get(0),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAdministratorPosts(
//            posts.subList(32, 48),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAdministratorPosts(
//            posts.subList(48, 64),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void getPosts_successWhenAdministratorAndState() {
//        List<Post> posts = postService.getPosts(administrator, new ResourceFilter().setState(ACCEPTED));
//        assertThat(posts).hasSize(8);
//
//        verifyAcceptedPosts(
//            posts.subList(0, 2),
//            departmentRejectedBoards.get(1),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAcceptedPosts(
//            posts.subList(2, 4),
//            departmentRejectedBoards.get(0),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAcceptedPosts(
//            posts.subList(4, 6),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAcceptedPosts(
//            posts.subList(6, 8),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void getPosts_successWhenAdministratorAndDepartment() {
//        List<Post> posts = postService.getPosts(administrator,
//            new ResourceFilter().setParentId(departmentAccepted.getId()));
//        assertThat(posts).hasSize(32);
//
//        verifyAdministratorPosts(
//            posts.subList(0, 16),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAdministratorPosts(
//            posts.subList(16, 32),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void getPosts_successWhenAdministratorAndBoard() {
//        List<Post> posts = postService.getPosts(administrator,
//            new ResourceFilter().setParentId(departmentAcceptedBoards.get(0).getId()));
//        assertThat(posts).hasSize(16);
//
//        verifyAdministratorPosts(
//            posts.subList(0, 16),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void getPosts_successWhenAction() {
//        List<Post> posts = postService.getPosts(administrator, new ResourceFilter().setAction(PURSUE));
//        assertThat(posts).hasSize(2);
//
//        verifyAcceptedPosts(
//            posts.subList(0, 2),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, PURSUE, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void getPosts_successWhenDepartmentAndSearchTerm() {
//        List<Post> posts = postService.getPosts(administrator,
//            new ResourceFilter().setParentId(departmentRejected.getId()).setSearchTerm("ACCEPTED"));
//        assertThat(posts).hasSize(4);
//        verifyAdministratorAcceptedSearchPosts(posts);
//    }
//
//    private void getPosts_successWhenDepartmentAndSearchTermTypo() {
//        List<Post> posts = postService.getPosts(administrator,
//            new ResourceFilter().setParentId(departmentRejected.getId()).setSearchTerm("aCEPTED"));
//        assertThat(posts).hasSize(4);
//        verifyAdministratorAcceptedSearchPosts(posts);
//    }
//
//    private void getPosts_successWhenSearchTermWithoutResults() {
//        List<Post> posts = postService.getPosts(administrator, new ResourceFilter().setSearchTerm("xyz"));
//        assertThat(posts).hasSize(0);
//    }
//
//    private void getPosts_successWhenOtherAdministrator() {
//        List<Post> posts = postService.getPosts(otherAdministrator, new ResourceFilter());
//        assertThat(posts).hasSize(36);
//
//        verifyOtherAdministratorPosts(
//            posts.subList(0, 16),
//            departmentRejectedBoards.get(1));
//
//        verifyOtherAdministratorPosts(
//            posts.subList(16, 32),
//            departmentRejectedBoards.get(0));
//
//        verifyAcceptedPosts(
//            posts.subList(32, 34),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(34, 36),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW});
//    }
//
//    private void getPosts_successWhenPostAdministrator() {
//        List<Post> posts = postService.getPosts(postAdministrator, new ResourceFilter());
//        assertThat(posts).hasSize(36);
//
//        verifyPostAdministratorPosts(
//            posts.subList(0, 9),
//            departmentRejectedBoards.get(1),
//            new Action[]{VIEW, EDIT, WITHDRAW},
//            new Action[]{VIEW});
//
//        verifyPostAdministratorPosts(
//            posts.subList(9, 18),
//            departmentRejectedBoards.get(0),
//            new Action[]{VIEW, EDIT, WITHDRAW},
//            new Action[]{VIEW});
//
//        verifyPostAdministratorPosts(
//            posts.subList(18, 27),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW, EDIT, WITHDRAW},
//            new Action[]{VIEW});
//
//        verifyPostAdministratorPosts(
//            posts.subList(27, 36),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, EDIT, PURSUE, WITHDRAW},
//            new Action[]{VIEW});
//    }
//
//    @SuppressWarnings("Duplicates")
//    private void getPosts_successWhenMember() {
//        List<Post> posts = postService.getPosts(member, new ResourceFilter());
//        assertThat(posts).hasSize(8);
//
//        verifyAcceptedPosts(
//            posts.subList(0, 2),
//            departmentRejectedBoards.get(1),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(2, 4),
//            departmentRejectedBoards.get(0),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(4, 6),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(6, 8),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW, PURSUE});
//    }
//
//    @SuppressWarnings("Duplicates")
//    private void getPosts_successWhenOtherMember() {
//        List<Post> posts = postService.getPosts(otherMember, new ResourceFilter());
//        assertThat(posts).hasSize(8);
//        verifyAcceptedPosts(posts);
//    }
//
//    private void getPosts_successWhenUnprivileged() {
//        Scenarios scenarios = serviceHelper.setUpUnprivilegedUsers(university)
//            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentAccepted, AUTHOR))
//            .scenarios(serviceHelper.setUpUnprivilegedUsers(departmentRejected, AUTHOR));
//
//        scenarios.forEach(scenario -> {
//            User user = scenario.user;
//            LOGGER.info("Verifying resources: " + scenario.description + " (" + user + ")");
//
//            List<Post> posts =
//                postService.getPosts(otherMember, new ResourceFilter())
//                    .stream()
//                    .filter(post -> departmentAcceptedPosts.contains(post) || departmentRejectedPosts.contains(post))
//                    .collect(toList());
//
//            assertThat(posts).hasSize(8);
//            verifyAcceptedPosts(posts);
//        });
//    }
//
//    private Post setUpPost(Board board, String applyWebsite, DocumentDTO applyDocument, String applyEmail) {
//        return postService.createPost(administrator, board.getId(),
//            new PostDTO()
//                .setName("post")
//                .setSummary("post summary")
//                .setDescription("post description")
//                .setOrganization(makeOrganizationDTO())
//                .setLocation(makeLocationDTO())
//                .setApplyWebsite(applyWebsite)
//                .setApplyDocument(applyDocument)
//                .setApplyEmail(applyEmail)
//                .setPostCategories(ImmutableList.of("Employment", "Internship"))
//                .setMemberCategories(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT))
//                .setExistingRelation(STUDENT)
//                .setExistingRelationExplanation(ImmutableMap.of("studyLevel", "MASTER"))
//                .setLiveTimestamp(LocalDateTime.of(2050, 5, 1, 0, 0, 0))
//                .setDeadTimestamp(LocalDateTime.of(2050, 5, 30, 0, 0, 0)));
//    }
//
//    private Organization makeOrganization() {
//        Organization expectedOrganization = new Organization();
//        expectedOrganization.setName("organization");
//        return expectedOrganization;
//    }
//
//    private OrganizationDTO makeOrganizationDTO() {
//        return new OrganizationDTO()
//            .setName("organization");
//    }
//
//    private Location makeLocation() {
//        Location expectedLocation = new Location();
//        expectedLocation.setGoogleId("google");
//        return expectedLocation;
//    }
//
//    private LocationDTO makeLocationDTO() {
//        return new LocationDTO()
//            .setName("london")
//            .setDomicile("uk")
//            .setGoogleId("google")
//            .setLatitude(ONE)
//            .setLongitude(ONE);
//    }
//
//    private void verifyGetById(Board board, Scenarios scenarios, User member, Action[] expectedAdministratorActions,
//                               Action[] expectedMemberActions, Action[] expectedUnprivilegedActions) {
//        Post createdPost = serviceHelper.setUpPost(administrator, board, "post");
//        Long createdPostId = createdPost.getId();
//
//        Runnable memberScenario =
//            () -> verifyGetById(member, createdPost, createdPostId);
//
//        Consumer<Scenario> unprivilegedScenario =
//            scenario -> {
//                User user = scenario.user;
//                verifyGetById(user, createdPost, createdPostId);
//            };
//
//        verifyGetById(createdPost, board, DRAFT, scenarios,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT, WITHDRAW},
//            memberScenario, unprivilegedScenario);
//
//        verifyGetById(createdPost, board, PENDING, scenarios,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW},
//            memberScenario, unprivilegedScenario);
//
//        verifyGetById(createdPost, board, ACCEPTED, scenarios,
//            expectedAdministratorActions,
//            () -> {
//                Post selectedPost = postService.getById(member, createdPostId, "ip", true);
//                serviceHelper.verifyActions(selectedPost, expectedMemberActions);
//                verifyInvocations(member, createdPostId, selectedPost);
//            },
//            scenario -> {
//                User user = scenario.user;
//                Post selectedPost = postService.getById(user, createdPostId, "ip", true);
//                assertEquals(createdPost, selectedPost);
//
//                verifyPost(selectedPost, board, "post", expectedUnprivilegedActions);
//                verifyInvocations(user, createdPostId, selectedPost);
//            });
//
//        verifyGetById(createdPost, board, EXPIRED, scenarios,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW},
//            memberScenario, unprivilegedScenario);
//
//        verifyGetById(createdPost, board, SUSPENDED, scenarios,
//            new Action[]{VIEW, EDIT, CORRECT, ACCEPT, REJECT, WITHDRAW},
//            memberScenario, unprivilegedScenario);
//
//        verifyGetById(createdPost, board, REJECTED, scenarios,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE, WITHDRAW},
//            memberScenario, unprivilegedScenario);
//
//        verifyGetById(createdPost, board, WITHDRAWN, scenarios,
//            new Action[]{VIEW, EDIT, RESTORE},
//            memberScenario, unprivilegedScenario);
//
//        verifyGetById(createdPost, board, ARCHIVED, scenarios,
//            new Action[]{VIEW, EDIT, RESTORE},
//            memberScenario, unprivilegedScenario);
//    }
//
//    private void verifyGetById(Post createdPost, Board board, State state, Scenarios scenarios,
//                               Action[] expectedAdministratorActions, Runnable memberScenario,
//                               Consumer<Scenario> unprivilegedScenario) {
//        reset(resourceService, actionService);
//        resourceService.updateState(createdPost, state);
//
//        Long createdBoardId = createdPost.getId();
//        Post selectedPost = postService.getById(administrator, createdBoardId, "ip", true);
//        assertEquals(createdPost, selectedPost);
//
//        verifyPost(selectedPost, board, "post", expectedAdministratorActions);
//        verifyInvocations(administrator, createdBoardId, selectedPost);
//
//        memberScenario.run();
//        scenarios.forEach(unprivilegedScenario);
//    }
//
//    private void verifyGetById(User user, Post createdPost, Long createdPostId) {
//        assertThatThrownBy(() -> postService.getById(user, createdPostId, "ip", true))
//            .isExactlyInstanceOf(BoardForbiddenException.class)
//            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
//
//        verifyInvocations(user, createdPostId, createdPost);
//    }
//
//    private void verifyInvocations(User user, Long createdPostId, Post selectedPost) {
//        verify(resourceService, atLeastOnce())
//            .getResource(user, Scope.POST, createdPostId);
//
//        verify(actionService, atLeastOnce())
//            .executeAction(eq(user), eq(selectedPost), eq(VIEW), any(Execution.class));
//    }
//
//    private void verifyPost(Post post, Board expectedBoard, String expectedName, Action[] expectedActions) {
//        serviceHelper.verifyIdentity(post, expectedBoard, expectedName);
//        serviceHelper.verifyActions(post, expectedActions);
//        serviceHelper.verifyTimestamps(post, baseline);
//    }
//
//    @SuppressWarnings("SameParameterValue")
//    private void verifyPost(Post post, Board expectedBoard, String expectedName, Action[] expectedActions,
//                            String expectedApplyWebsite, Document expectedApplyDocument, String expectApplyEmail) {
//        verifyPost(post, expectedBoard, expectedName, expectedActions);
//        assertEquals("post summary", post.getSummary());
//        assertEquals("post description", post.getDescription());
//        assertEquals(makeOrganization(), post.getOrganization());
//        assertEquals(makeLocation(), post.getLocation());
//        assertEquals(expectedApplyWebsite, post.getApplyWebsite());
//        assertEquals(expectedApplyDocument, post.getApplyDocument());
//        assertEquals(expectApplyEmail, post.getApplyEmail());
//        assertEquals(STUDENT, post.getExistingRelation());
//        assertEquals("{\n  \"studyLevel\" : \"MASTER\"\n}", post.getExistingRelationExplanation());
//        assertEquals(PENDING, post.getState());
//        assertEquals(PENDING, post.getPreviousState());
//        assertEquals(LocalDateTime.of(2050, 5, 1, 0, 0, 0), post.getLiveTimestamp());
//        assertEquals(LocalDateTime.of(2050, 5, 30, 0, 0, 0), post.getDeadTimestamp());
//        assertEquals(ImmutableList.of("Employment", "Internship"), post.getPostCategoryStrings());
//        assertEquals(
//            toStrings(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT)), post.getMemberCategoryStrings());
//    }
//
//    private void verifyInvocations(Post post, Board board) {
//        verify(actionService, times(1))
//            .executeAction(eq(administrator), eq(board), eq(EXTEND), any(Execution.class));
//
//        verify(organizationService, times(1)).getOrCreateOrganization(makeOrganizationDTO());
//        verify(locationService, times(1)).getOrCreateLocation(makeLocationDTO());
//        verify(userService, times(1))
//            .updateUserOrganizationAndLocation(administrator, makeOrganization(), makeLocation());
//
//        verify(postValidator, times(1)).checkApply(post);
//        verify(resourceService, times(1)).createResourceRelation(board, post);
//
//        List<String> postCategories = ImmutableList.of("Employment", "Internship");
//        verify(postValidator, times(1)).checkCategories(
//            postCategories, board.getPostCategoryStrings(),
//            FORBIDDEN_POST_CATEGORIES, MISSING_POST_CATEGORIES, INVALID_POST_CATEGORIES);
//        verify(resourceService, times(1)).updateCategories(post, POST, postCategories);
//
//        List<String> memberCategories = toStrings(ImmutableList.of(UNDERGRADUATE_STUDENT, MASTER_STUDENT));
//        verify(postValidator, times(1)).checkCategories(
//            memberCategories, departmentAccepted.getMemberCategoryStrings(),
//            FORBIDDEN_MEMBER_CATEGORIES, MISSING_MEMBER_CATEGORIES, INVALID_MEMBER_CATEGORIES);
//        verify(resourceService, times(1)).updateCategories(post, MEMBER, memberCategories);
//
//        verify(resourceService, times(1)).setIndexDataAndQuarter(post);
//        verify(userRoleService, times(1)).createUserRole(post, administrator, ADMINISTRATOR);
//        verify(resourceTaskService, times(1)).completeTasks(departmentAccepted, POST_TASKS);
//        verify(postValidator, times(1)).checkExistingRelation(post);
//    }
//
//    private void verifyAdministratorPosts(List<Post> posts, Board expectedBoard,
//                                          Action[] expectedPostAdministratorAcceptedActions,
//                                          Action[] expectedAdministratorAcceptedActions) {
//        Long administratorId = administrator.getId();
//        Long postAdministratorId = postAdministrator.getId();
//
//        verifyPost(posts.get(0), expectedBoard,
//            "post ARCHIVED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(1), expectedBoard,
//            "post ARCHIVED" + administratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(2), expectedBoard,
//            "post WITHDRAWN" + postAdministratorId,
//            new Action[]{VIEW, EDIT});
//
//        verifyPost(posts.get(3), expectedBoard,
//            "post WITHDRAWN" + administratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(4), expectedBoard,
//            "post REJECTED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
//
//        verifyPost(posts.get(5), expectedBoard,
//            "post REJECTED" + administratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE, WITHDRAW});
//
//        verifyPost(posts.get(6), expectedBoard,
//            "post SUSPENDED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
//
//        verifyPost(posts.get(7), expectedBoard,
//            "post SUSPENDED" + administratorId,
//            new Action[]{VIEW, EDIT, CORRECT, ACCEPT, REJECT, WITHDRAW});
//
//        verifyPost(posts.get(8), expectedBoard,
//            "post EXPIRED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(9), expectedBoard,
//            "post EXPIRED" + administratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyPost(posts.get(10), expectedBoard,
//            "post ACCEPTED" + postAdministratorId,
//            expectedPostAdministratorAcceptedActions);
//
//        verifyPost(posts.get(11), expectedBoard,
//            "post ACCEPTED" + administratorId,
//            expectedAdministratorAcceptedActions);
//
//        verifyPost(posts.get(12), expectedBoard,
//            "post PENDING" + postAdministratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(13), expectedBoard,
//            "post PENDING" + administratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyPost(posts.get(14), expectedBoard,
//            "post DRAFT" + postAdministratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(15), expectedBoard,
//            "post DRAFT" + administratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void verifyAdministratorAcceptedSearchPosts(List<Post> posts) {
//        verifyAcceptedPosts(
//            posts.subList(0, 2),
//            departmentRejectedBoards.get(1),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//
//        verifyAcceptedPosts(
//            posts.subList(2, 4),
//            departmentRejectedBoards.get(0),
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT},
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT, WITHDRAW});
//    }
//
//    private void verifyOtherAdministratorPosts(List<Post> posts, Board expectedBoard) {
//        Long administratorId = administrator.getId();
//        Long postAdministratorId = postAdministrator.getId();
//
//        verifyPost(posts.get(0), expectedBoard,
//            "post ARCHIVED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(1), expectedBoard,
//            "post ARCHIVED" + administratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(2), expectedBoard,
//            "post WITHDRAWN" + postAdministratorId,
//            new Action[]{VIEW, EDIT});
//
//        verifyPost(posts.get(3), expectedBoard,
//            "post WITHDRAWN" + administratorId,
//            new Action[]{VIEW, EDIT});
//
//        verifyPost(posts.get(4), expectedBoard,
//            "post REJECTED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
//
//        verifyPost(posts.get(5), expectedBoard,
//            "post REJECTED" + administratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, RESTORE});
//
//        verifyPost(posts.get(6), expectedBoard,
//            "post SUSPENDED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
//
//        verifyPost(posts.get(7), expectedBoard,
//            "post SUSPENDED" + administratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, REJECT});
//
//        verifyPost(posts.get(8), expectedBoard,
//            "post EXPIRED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(9), expectedBoard,
//            "post EXPIRED" + administratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(10), expectedBoard,
//            "post ACCEPTED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(11), expectedBoard,
//            "post ACCEPTED" + administratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(12), expectedBoard,
//            "post PENDING" + postAdministratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(13), expectedBoard,
//            "post PENDING" + administratorId,
//            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(14), expectedBoard,
//            "post DRAFT" + postAdministratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
//
//        verifyPost(posts.get(15), expectedBoard,
//            "post DRAFT" + administratorId,
//            new Action[]{VIEW, EDIT, ACCEPT, SUSPEND, REJECT});
//    }
//
//    private void verifyPostAdministratorPosts(List<Post> posts, Board expectedBoard,
//                                              Action[] expectedPostAdministratorAcceptedActions,
//                                              Action[] expectedAdministratorAcceptedActions) {
//        Long postAdministratorId = postAdministrator.getId();
//
//        verifyPost(posts.get(0), expectedBoard,
//            "post ARCHIVED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(1), expectedBoard,
//            "post WITHDRAWN" + postAdministratorId,
//            new Action[]{VIEW, EDIT, RESTORE});
//
//        verifyPost(posts.get(2), expectedBoard,
//            "post REJECTED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, WITHDRAW});
//
//        verifyPost(posts.get(3), expectedBoard,
//            "post SUSPENDED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, CORRECT, WITHDRAW});
//
//        verifyPost(posts.get(4), expectedBoard,
//            "post EXPIRED" + postAdministratorId,
//            new Action[]{VIEW, EDIT, WITHDRAW});
//
//        verifyPost(posts.get(5), expectedBoard,
//            "post ACCEPTED" + postAdministratorId,
//            expectedPostAdministratorAcceptedActions);
//
//        verifyPost(posts.get(6), expectedBoard,
//            "post ACCEPTED" + administrator.getId(),
//            expectedAdministratorAcceptedActions);
//
//        verifyPost(posts.get(7), expectedBoard,
//            "post PENDING" + postAdministratorId,
//            new Action[]{VIEW, EDIT, WITHDRAW});
//
//        verifyPost(posts.get(8), expectedBoard,
//            "post DRAFT" + postAdministratorId,
//            new Action[]{VIEW, EDIT, WITHDRAW});
//    }
//
//    private void verifyAcceptedPosts(List<Post> posts) {
//        verifyAcceptedPosts(
//            posts.subList(0, 2),
//            departmentRejectedBoards.get(1),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(2, 4),
//            departmentRejectedBoards.get(0),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(4, 6),
//            departmentAcceptedBoards.get(1),
//            new Action[]{VIEW});
//
//        verifyAcceptedPosts(
//            posts.subList(6, 8),
//            departmentAcceptedBoards.get(0),
//            new Action[]{VIEW});
//    }
//
//    private void verifyAcceptedPosts(List<Post> posts, Board expectedBoard, Action[] expectedActions) {
//        verifyAcceptedPosts(posts, expectedBoard, expectedActions, expectedActions);
//    }
//
//    private void verifyAcceptedPosts(List<Post> posts, Board expectedBoard, Action[] expectedPostAdministratorActions,
//                                     Action[] expectedAdministratorActions) {
//        Long administratorId = administrator.getId();
//        Long postAdministratorId = postAdministrator.getId();
//
//        verifyPost(posts.get(0), expectedBoard,
//            "post ACCEPTED" + postAdministratorId,
//            expectedPostAdministratorActions);
//
//        verifyPost(posts.get(1), expectedBoard,
//            "post ACCEPTED" + administratorId,
//            expectedAdministratorActions);
//    }

}
