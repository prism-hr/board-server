package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
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
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.enums.State.*;
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

    private static List<State> POST_STATES = ImmutableList.of(
        DRAFT, PENDING, ACCEPTED, EXPIRED, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED);

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
    public void getById_successWhenDepartmentAcceptedBoardAcceptedAndPostPendingAndDepartmentAdministrator() {
        User[] users = new User[]{
            userService.getByEmail("department-administrator@prism.hr"),
            userService.getByEmail("department-accepted-administrator@prism.hr")};

        Board board = (Board) resourceService.getByHandle("university/department-accepted/board-accepted");
        verifyGetById(users, 5L, board,
            "department-accepted-board-accepted-post-pending",
            new Action[]{VIEW, EDIT, SUSPEND, REJECT});
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

        Post post = postService.getById(user, id);

        verifyPost(post, expectedBoard, expectedName, expectedActions);
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
