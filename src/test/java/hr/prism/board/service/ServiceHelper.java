package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ActionRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.fromStrings;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.utils.ResourceUtils.getQuarter;
import static java.math.BigDecimal.ONE;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Component
public class ServiceHelper {

    private final ResourceRepository resourceRepository;

    private final UserRoleRepository userRoleRepository;

    private final DepartmentService departmentService;

    private final BoardService boardService;

    private final PostService postService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    @Inject
    public ServiceHelper(ResourceRepository resourceRepository, UserRoleRepository userRoleRepository,
                         DepartmentService departmentService, BoardService boardService, PostService postService,
                         UserService userService, UserRoleService userRoleService) {
        this.resourceRepository = resourceRepository;
        this.userRoleRepository = userRoleRepository;
        this.departmentService = departmentService;
        this.boardService = boardService;
        this.postService = postService;
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @SuppressWarnings("SameParameterValue")
    Department setUpDepartment(User user, Long universityId, String name) {
        return departmentService.createDepartment(user, universityId,
            new DepartmentDTO()
                .setName(name)
                .setSummary(name + " summary"));
    }

    Board setUpBoard(User user, Long departmentId, String name) {
        return boardService.createBoard(user, departmentId,
            new BoardDTO()
                .setName(name)
                .setPostCategories(ImmutableList.of("Employment", "Internship")));
    }

    Post setUpPost(User user, Long boardId, String name) {
        Board board = boardService.getById(user, boardId);
        Department department = departmentService.getById(user, board.getParent().getId());

        return postService.createPost(user, boardId,
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
                .setPostCategories(board.getPostCategoryStrings())
                .setMemberCategories(fromStrings(department.getMemberCategoryStrings()))
                .setExistingRelation(STUDENT)
                .setExistingRelationExplanation(ImmutableMap.of("studyLevel", "MASTER"))
                .setLiveTimestamp(LocalDateTime.now())
                .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L)));
    }

    void setPostPending(Post post) {
        post.setLiveTimestamp(LocalDateTime.now());
        post.setDeadTimestamp(LocalDateTime.now().plusWeeks(1L));
        resourceRepository.save(post);
    }

    void setPostExpired(Post post) {
        post.setLiveTimestamp(LocalDateTime.now().minusWeeks(1));
        post.setDeadTimestamp(LocalDateTime.now());
        resourceRepository.save(post);
    }

    User setUpUser() {
        String uuid = randomUUID().toString();
        return userService.createUser(
            new RegisterDTO()
                .setGivenName(uuid)
                .setSurname(uuid)
                .setEmail(uuid + "@prism.hr")
                .setPassword("password"));
    }

    Scenarios setUpUnprivilegedUsersForDepartment(Department department) {
        User departmentAuthor = setUpUser();
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User departmentMember = setUpUser();
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        State state = getStateThenSetState(department, ACCEPTED);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board board = setUpBoard(departmentAdministrator, department.getId(), randomUUID().toString());

        User postAdministrator = setUpUser();
        setUpPost(postAdministrator, board.getId(), randomUUID().toString());

        setState(department, state);

        User otherDepartmentAdministrator = setUpUser();
        Department otherDepartment =
            setUpDepartment(otherDepartmentAdministrator, 1L, randomUUID().toString());
        Board otherBoard = setUpBoard(otherDepartmentAdministrator, otherDepartment.getId(), randomUUID().toString());

        User otherDepartmentAuthor = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherPostAdministrator = setUpUser();
        setUpPost(otherPostAdministrator, otherBoard.getId(), "other-post");

        User userWithoutRoles = setUpUser();

        return new Scenarios()
            .scenario(departmentAuthor, "Department author")
            .scenario(departmentMember, "Department member")
            .scenario(postAdministrator, "Post administrator")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherPostAdministrator, "Other post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");
    }

    Scenarios setUpUnprivilegedUsersForBoard(Board board) {
        Department department = ofNullable((Department) board.getParent())
            .orElseThrow(() -> new Error("Board ID: " + board.getId() + " has no department"));

        State departmentState = getStateThenSetState(department, ACCEPTED);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board otherBoard = setUpBoard(departmentAdministrator, department.getId(), randomUUID().toString());

        User otherBoardPostAdministrator = setUpUser();
        setUpPost(otherBoardPostAdministrator, otherBoard.getId(), randomUUID().toString());

        setState(department, departmentState);

        State boardState = getStateThenSetState(board, ACCEPTED);

        User postAdministrator = setUpUser();
        setUpPost(postAdministrator, board.getId(), randomUUID().toString());

        setState(board, boardState);

        User departmentMember = setUpUser();
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User otherDepartmentAdministrator = setUpUser();
        Department otherDepartment =
            setUpDepartment(otherDepartmentAdministrator, 1L, randomUUID().toString());
        Board otherDepartmentBoard =
            setUpBoard(otherDepartmentAdministrator, otherDepartment.getId(), randomUUID().toString());

        User otherDepartmentAuthor = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = setUpUser();
        setUpPost(otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), randomUUID().toString());

        User userWithoutRoles = setUpUser();

        return new Scenarios()
            .scenario(departmentMember, "Department member")
            .scenario(postAdministrator, "Post administrator")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentAdministrator, "Other department post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");
    }

    Scenarios setUpUnprivilegedUsersForPost(Post post) {
        Board board = ofNullable((Board) post.getParent())
            .orElseThrow(() -> new Error("Post ID: " + post.getId() + " has no board"));

        Department department = ofNullable((Department) board.getParent())
            .orElseThrow(() -> new Error("Board ID: " + board.getId() + " has no department"));

        State departmentState = getStateThenSetState(department, ACCEPTED);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board otherBoard = setUpBoard(departmentAdministrator, department.getId(), randomUUID().toString());

        setState(department, departmentState);

        User otherBoardPostAdministrator = setUpUser();
        setUpPost(otherBoardPostAdministrator, otherBoard.getId(), randomUUID().toString());

        User departmentAuthor = setUpUser();
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User otherDepartmentAdministrator = setUpUser();
        Department otherDepartment =
            setUpDepartment(otherDepartmentAdministrator, 1L, randomUUID().toString());
        Board otherDepartmentBoard = setUpBoard(
            otherDepartmentAdministrator, otherDepartment.getId(), randomUUID().toString());

        User otherDepartmentAuthor = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = setUpUser();
        setUpPost(otherDepartmentPostAdministrator, otherDepartmentBoard.getId(), randomUUID().toString());

        User userWithoutRoles = setUpUser();

        return new Scenarios()
            .scenario(departmentAuthor, "Department author")
            .scenario(otherDepartmentAdministrator, "Other department administrator")
            .scenario(otherDepartmentAuthor, "Other department author")
            .scenario(otherDepartmentMember, "Other department member")
            .scenario(otherBoardPostAdministrator, "Other board post administrator")
            .scenario(otherDepartmentPostAdministrator, "Other department post administrator")
            .scenario(userWithoutRoles, "User without roles")
            .scenario(null, "Public user");
    }

    void verifyTimestamps(BoardEntity entity, LocalDateTime baseline) {
        assertThat(entity.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(entity.getUpdatedTimestamp()).isGreaterThan(baseline);
    }

    void verifyIdentity(Resource resource, Resource expectedParentResource, String expectedName) {
        assertNotNull(resource.getId());
        assertEquals(expectedParentResource, resource.getParent());
        assertEquals(expectedName, resource.getName());
    }

    void verifyActions(Resource resource, Action[] expectedActions) {
        assertThat(
            resource.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(expectedActions);
    }

    void verifyIndexDataAndQuarter(Resource resource, String expectedIndexData) {
        assertEquals(expectedIndexData, resource.getIndexData());
        assertEquals(getQuarter(resource.getCreatedTimestamp()), resource.getQuarter());
    }

    private User getDepartmentAdministrator(Department department) {
        return userRoleRepository.findByResourceAndRole(department, ADMINISTRATOR)
            .stream()
            .map(UserRole::getUser)
            .findFirst()
            .orElseThrow(() -> new Error(department + " has no administrator"));
    }

    @SuppressWarnings("SameParameterValue")
    private State getStateThenSetState(Resource resource, State newState) {
        State state = resource.getState();
        setState(resource, newState);
        return state;
    }

    private void setState(Resource resource, State state) {
        resource.setState(state);
        resourceRepository.save(resource);
    }

    static class Scenarios {

        private List<Scenario> scenarios = new ArrayList<>();

        Scenarios scenario(User user, String description) {
            scenarios.add(new Scenario(user, description));
            return this;
        }

        void forEach(Consumer<Scenario> consumer) {
            scenarios.forEach(consumer);
        }

    }

    static class Scenario {

        User user;

        String description;

        private Scenario(User user, String description) {
            this.user = user;
            this.description = description;
        }

    }

    interface ResourceModifier {

        void modify(Resource resource);

    }

}
