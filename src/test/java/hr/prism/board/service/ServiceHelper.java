package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.repository.DocumentRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.ActionRepresentation;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.fromStrings;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED;
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

    private final DocumentRepository documentRepository;

    private final ResourceDAO resourceDAO;

    private final DepartmentService departmentService;

    private final BoardService boardService;

    private final PostService postService;

    private final ResourceService resourceService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private final PlatformTransactionManager platformTransactionManager;

    @Inject
    public ServiceHelper(ResourceRepository resourceRepository, UserRoleRepository userRoleRepository,
                         DocumentRepository documentRepository, ResourceDAO resourceDAO,
                         DepartmentService departmentService, BoardService boardService, PostService postService,
                         ResourceService resourceService, UserService userService, UserRoleService userRoleService,
                         PlatformTransactionManager platformTransactionManager) {
        this.resourceRepository = resourceRepository;
        this.userRoleRepository = userRoleRepository;
        this.documentRepository = documentRepository;
        this.resourceDAO = resourceDAO;
        this.departmentService = departmentService;
        this.boardService = boardService;
        this.postService = postService;
        this.resourceService = resourceService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.platformTransactionManager = platformTransactionManager;
    }

    @SuppressWarnings("SameParameterValue")
    University setUpUniversity(String name) {
        return new TransactionTemplate(platformTransactionManager).execute(status -> {
            Document documentLogo = new Document();
            documentLogo.setCloudinaryId("cloudinary id");
            documentLogo.setCloudinaryUrl("cloudinary url");
            documentLogo.setFileName("file name");
            documentLogo = documentRepository.save(documentLogo);

            University university = new University();
            university.setName(name);
            university.setHandle(name);
            university.setDocumentLogo(documentLogo);
            university.setState(ACCEPTED);
            university = resourceRepository.save(university);

            resourceService.createResourceRelation(university, university);
            return university;
        });
    }

    @SuppressWarnings("SameParameterValue")
    Department setUpDepartment(User user, University university, String name) {
        return departmentService.createDepartment(user, university.getId(),
            new DepartmentDTO()
                .setName(name)
                .setSummary(name + " summary"));
    }

    @SuppressWarnings("SameParameterValue")
    Department setUpDepartment(User user, University university, String name, State state) {
        Department department = setUpDepartment(user, university, name);
        resourceService.updateState(department, state);
        return department;
    }

    Board setUpBoard(User user, Department department, String name) {
        State state = getStateThenSetState(department, ACCEPTED);

        Board board =
            boardService.createBoard(user, department.getId(),
                new BoardDTO()
                    .setName(name)
                    .setPostCategories(ImmutableList.of("Employment", "Internship")));

        setState(department, state);
        return board;
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    Board setUpBoard(User user, Department department, String name, State state) {
        Board board = setUpBoard(user, department, name);
        resourceService.updateState(board, state);
        return board;
    }

    Post setUpPost(User user, Board board, String name) {
        State boardState = getStateThenSetState(board, ACCEPTED);
        Department department = (Department) resourceDAO.getById(DEPARTMENT, board.getParent().getId());
        State departmentState = getStateThenSetState(department, ACCEPTED);

        Post post =
            postService.createPost(user, board.getId(),
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

        setState(board, boardState);
        setState(department, departmentState);
        return post;
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
        String uuid = randomName();
        return userService.createUser(
            new RegisterDTO()
                .setGivenName(uuid)
                .setSurname(uuid)
                .setEmail(uuid + "@prism.hr")
                .setPassword("password"));
    }

    Scenarios setUpUnprivilegedUsersForDepartment(Department department) {
        University university = ofNullable((University) department.getParent())
            .orElseThrow(() -> new Error("Department ID: " + department.getId() + " has no university"));

        User departmentAuthor = setUpUser();
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User departmentMember = setUpUser();
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        State state = getStateThenSetState(department, ACCEPTED);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board board = setUpBoard(departmentAdministrator, department, randomName());

        User postAdministrator = setUpUser();
        setUpPost(postAdministrator, board, randomName());

        setState(department, state);

        User otherDepartmentAdministrator = setUpUser();
        Department otherDepartment =
            setUpDepartment(otherDepartmentAdministrator, university, randomName());
        Board otherBoard = setUpBoard(otherDepartmentAdministrator, otherDepartment, randomName());

        User otherDepartmentAuthor = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherPostAdministrator = setUpUser();
        setUpPost(otherPostAdministrator, otherBoard, "other-post");

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

        University university = ofNullable((University) department.getParent())
            .orElseThrow(() -> new Error("Department ID: " + department.getId() + " has no university"));

        State departmentState = getStateThenSetState(department, ACCEPTED);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board otherBoard = setUpBoard(departmentAdministrator, department, randomName());

        User otherBoardPostAdministrator = setUpUser();
        setUpPost(otherBoardPostAdministrator, otherBoard, randomName());

        setState(department, departmentState);

        State boardState = getStateThenSetState(board, ACCEPTED);

        User postAdministrator = setUpUser();
        setUpPost(postAdministrator, board, randomName());

        setState(board, boardState);

        User departmentMember = setUpUser();
        userRoleService.createUserRole(department, departmentMember, MEMBER);

        User otherDepartmentAdministrator = setUpUser();
        Department otherDepartment =
            setUpDepartment(otherDepartmentAdministrator, university, randomName());
        Board otherDepartmentBoard =
            setUpBoard(otherDepartmentAdministrator, otherDepartment, randomName());

        User otherDepartmentAuthor = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = setUpUser();
        setUpPost(otherDepartmentPostAdministrator, otherDepartmentBoard, randomName());

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

        University university = ofNullable((University) department.getParent())
            .orElseThrow(() -> new Error("Department ID: " + department.getId() + " has no university"));

        State departmentState = getStateThenSetState(department, ACCEPTED);

        User departmentAdministrator = getDepartmentAdministrator(department);
        Board otherBoard = setUpBoard(departmentAdministrator, department, randomName());

        setState(department, departmentState);

        User otherBoardPostAdministrator = setUpUser();
        setUpPost(otherBoardPostAdministrator, otherBoard, randomName());

        User departmentAuthor = setUpUser();
        userRoleService.createUserRole(department, departmentAuthor, AUTHOR);

        User otherDepartmentAdministrator = setUpUser();
        Department otherDepartment =
            setUpDepartment(otherDepartmentAdministrator, university, randomName());
        Board otherDepartmentBoard = setUpBoard(
            otherDepartmentAdministrator, otherDepartment, randomName());

        User otherDepartmentAuthor = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentAuthor, AUTHOR);

        User otherDepartmentMember = setUpUser();
        userRoleService.createUserRole(otherDepartment, otherDepartmentMember, MEMBER);

        User otherDepartmentPostAdministrator = setUpUser();
        setUpPost(otherDepartmentPostAdministrator, otherDepartmentBoard, randomName());

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

    void verifyTimestamps(BoardEntity entity, LocalDateTime baseline) {
        assertThat(entity.getCreatedTimestamp()).isGreaterThanOrEqualTo(baseline);
        assertThat(entity.getUpdatedTimestamp()).isGreaterThanOrEqualTo(baseline);
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

    private String randomName() {
        return randomUUID().toString().replace("-", "");
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
