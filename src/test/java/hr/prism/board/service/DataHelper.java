package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.State;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.repository.UserRoleRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.MASTER_STUDENT;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.State.ACCEPTED;
import static java.math.BigDecimal.ONE;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Component
public class DataHelper {

    private final ResourceRepository resourceRepository;

    private final UserRoleRepository userRoleRepository;

    private final DepartmentService departmentService;

    private final BoardService boardService;

    private final PostService postService;

    private final ResourceService resourceService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    @Inject
    public DataHelper(ResourceRepository resourceRepository, UserRoleRepository userRoleRepository,
                      DepartmentService departmentService, BoardService boardService, PostService postService,
                      ResourceService resourceService, UserService userService, UserRoleService userRoleService) {
        this.resourceRepository = resourceRepository;
        this.userRoleRepository = userRoleRepository;
        this.departmentService = departmentService;
        this.boardService = boardService;
        this.postService = postService;
        this.resourceService = resourceService;
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @SuppressWarnings("SameParameterValue")
    Department setUpDepartment(User user, Long universityId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return departmentService.createDepartment(universityId,
            new DepartmentDTO()
                .setName(name)
                .setSummary(name + " summary"));
    }

    Board setUpBoard(User user, Long departmentId, String name) {
        getContext().setAuthentication(new AuthenticationToken(user));
        return boardService.createBoard(departmentId,
            new BoardDTO()
                .setName(name)
                .setPostCategories(ImmutableList.of("Employment", "Internship")));
    }

    Post setUpPost(User user, Long boardId, String name) {
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

        State state = department.getState();
        department.setState(ACCEPTED);
        resourceRepository.save(department);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board board = setUpBoard(departmentAdministrator, department.getId(), randomUUID().toString());

        User postAdministrator = setUpUser();
        setUpPost(postAdministrator, board.getId(), randomUUID().toString());

        department.setState(state);
        resourceRepository.save(department);

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

        State state = department.getState();
        department.setState(ACCEPTED);
        resourceRepository.save(department);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board otherBoard = setUpBoard(departmentAdministrator, department.getId(), randomUUID().toString());

        User postAdministrator = setUpUser();
        setUpPost(postAdministrator, board.getId(), randomUUID().toString());

        User otherBoardPostAdministrator = setUpUser();
        setUpPost(otherBoardPostAdministrator, otherBoard.getId(), randomUUID().toString());

        department.setState(state);
        resourceRepository.save(department);

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

        State state = department.getState();
        department.setState(ACCEPTED);
        resourceRepository.save(department);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board otherBoard = setUpBoard(departmentAdministrator, department.getId(), randomUUID().toString());

        department.setState(state);
        resourceRepository.save(department);

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

    private User getDepartmentAdministrator(Department department) {
        return userRoleRepository.findByResourceAndRole(department, ADMINISTRATOR)
            .stream()
            .map(UserRole::getUser)
            .findFirst()
            .orElseThrow(() -> new Error(department + " has no administrator"));
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
