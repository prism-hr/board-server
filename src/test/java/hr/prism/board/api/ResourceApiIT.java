package hr.prism.board.api;

import com.google.common.collect.ImmutableSet;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.util.BoardUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@TestContext
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
public class ResourceApiIT extends AbstractIT {

    @Test
    public void shouldAddAndRemoveRoles() {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        addAndRemoveUserRoles(currentUser, Scope.BOARD, boardR.getId());
        addAndRemoveUserRoles(currentUser, Scope.DEPARTMENT, boardR.getDepartment().getId());
    }

    @Test
    public void shouldNotRemoveLastRole() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        UserDTO newUser = new UserDTO().setEmail("last-role@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.createResourceUser(Scope.BOARD, boardR.getId(),
            new ResourceUserDTO().setUser(newUser).setRoles(
                ImmutableSet.of(new UserRoleDTO().setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)))));

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class,
                () -> resourceApi.updateResourceUser(Scope.BOARD, boardR.getId(), boardManager.getUser().getId(),
                    new ResourceUserDTO().setUser(newUser).setRoles(Collections.emptySet())),
                ExceptionCode.IRREMOVABLE_USER_ROLE, status);
            return null;
        });
    }

    @Test
    public void shouldNotRemoveLastAdminRole() {
        User creator = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        boardDTO.getDepartment().setName("last-admin-role");
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);
        Long departmentId = boardR.getDepartment().getId();

        // add another administrator
        UserDTO newUserDTO = new UserDTO().setEmail("last-admin-role@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(newUserDTO)
                .setRoles(ImmutableSet.of(new UserRoleDTO().setRole(Role.ADMINISTRATOR),
                    new UserRoleDTO().setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)))));

        // remove current user as administrator
        resourceApi.deleteResourceUser(Scope.DEPARTMENT, departmentId, creator.getId());

        // authenticate as another administrator
        User newUser = userCacheService.findOneFresh(boardManager.getUser().getId());
        testUserService.setAuthentication(newUser.getId());

        // try to remove yourself as administrator
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class,
                () -> resourceApi.updateResourceUser(Scope.DEPARTMENT, departmentId, boardManager.getUser().getId(),
                    new ResourceUserDTO().setUser(newUserDTO)
                        .setRoles(ImmutableSet.of(new UserRoleDTO().setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT))))),
                ExceptionCode.IRREMOVABLE_USER_ROLE, status);
            return null;
        });

        List<ResourceUserRepresentation> users = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentId);
        Assert.assertThat(users, contains(resourceUserMatcher(newUserDTO.getEmail(),
            new UserRoleDTO(Role.ADMINISTRATOR), new UserRoleDTO(Role.MEMBER, null, MemberCategory.MASTER_STUDENT))));
    }

    @Test
    public void shouldNotRemoveLastAdminUser() {
        User creator = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        boardDTO.getDepartment().setName("last-admin-user");
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);
        Long departmentId = boardR.getDepartment().getId();

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class, () -> resourceApi.deleteResourceUser(Scope.DEPARTMENT, departmentId, creator.getId()), ExceptionCode
                .IRREMOVABLE_USER, status);
            return null;
        });

        List<ResourceUserRepresentation> users = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentId);
        Assert.assertThat(users, contains(resourceUserMatcher(creator.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));
    }

    @Test
    public void shouldAddUsersInBulk() throws InterruptedException {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        BoardRepresentation boardR = boardApi.postBoard(boardDTO);
        List<ResourceUserRepresentation> resourceUsers = resourceApi.getResourceUsers(Scope.BOARD, boardR.getId());
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));

        // add 200 members
        ResourceUsersDTO bulkDTO = new ResourceUsersDTO();
        bulkDTO.setUsers(new LinkedList<>());
        for (int i = 1; i <= 200; i++) {
            bulkDTO.getUsers().add(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"));
        }

        bulkDTO.setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT))));
        resourceApi.createResourceUsers(Scope.BOARD, boardR.getId(), bulkDTO);

        int usersCount = resourceApi.getResourceUsers(Scope.BOARD, boardR.getId()).size();
        assertEquals(201, usersCount);

        bulkDTO = new ResourceUsersDTO();
        bulkDTO.setUsers(new LinkedList<>());
        for (int i = 101; i <= 300; i++) {
            bulkDTO.getUsers().add(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"));
        }

        bulkDTO.setRoles(ImmutableSet.of(new UserRoleDTO().setRole(Role.MEMBER).setCategories(
            Collections.singletonList(MemberCategory.MASTER_STUDENT)), new UserRoleDTO().setRole(Role.AUTHOR)));
        resourceApi.createResourceUsers(Scope.BOARD, boardR.getId(), bulkDTO);

        usersCount = resourceApi.getResourceUsers(Scope.BOARD, boardR.getId()).size();
        assertEquals(301, usersCount);
    }

    @Test
    public void shouldNotAddUserWithNotExistingMemberCategory() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        UserDTO newUser = new UserDTO().setEmail("board-manager@mail.com").setGivenName("Sample").setSurname("User");

        // try to add a user to a board
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.createResourceUser(Scope.BOARD, boardR.getId(),
                new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(new UserRoleDTO().setRole(Role.MEMBER)
                    .setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT)), new UserRoleDTO().setRole(Role.AUTHOR)))),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try to add a user to a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.createResourceUser(Scope.DEPARTMENT, boardR.getDepartment().getId(),
                new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(new UserRoleDTO().setRole(Role.MEMBER)
                    .setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT)), new UserRoleDTO().setRole(Role.AUTHOR)))),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
    }

    @Test
    public void shouldNotAddUserRoleWithUnactivatedMemberCategory() {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        // try with a board
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.updateResourceUser(Scope.BOARD, boardR.getId(), currentUser.getId(),
                new ResourceUserDTO().setRoles(ImmutableSet.of(new UserRoleDTO(Role.MEMBER, null, MemberCategory.RESEARCH_STUDENT)))),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try with a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.updateResourceUser(Scope.DEPARTMENT, boardR.getDepartment().getId(), currentUser.getId(),
                new ResourceUserDTO().setRoles(ImmutableSet.of(new UserRoleDTO(Role.MEMBER, null, MemberCategory.RESEARCH_STUDENT)))),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
    }

    @Test
    @Sql("classpath:data/user_autosuggest_setup.sql")
    public void shouldGetSimilarUsers() {
        testUserService.authenticate();
        BoardRepresentation boardR = boardApi.postBoard(TestHelper.sampleBoard());
        Long departmentId = boardR.getDepartment().getId();
        Long boardId = boardR.getId();

        List<UserRepresentation> userRs = resourceApi.getSimilarUsers(Scope.DEPARTMENT, departmentId, "alas");
        Assert.assertEquals(3, userRs.size());
        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(1));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(2));

        userRs = resourceApi.getSimilarUsers(Scope.BOARD, boardId, "knowles");
        Assert.assertEquals(3, userRs.size());
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));
        verifySuggestedUser("jakub", "knowles", "jakub@knowles.com", userRs.get(2));

        userRs = resourceApi.getSimilarUsers(Scope.DEPARTMENT, departmentId, "alastair fib");
        Assert.assertEquals(1, userRs.size());
        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));

        userRs = resourceApi.getSimilarUsers(Scope.BOARD, boardId, "alastair knowles");
        Assert.assertEquals(2, userRs.size());
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));

        userRs = resourceApi.getSimilarUsers(Scope.DEPARTMENT, departmentId, "alastair@kno");
        Assert.assertEquals(2, userRs.size());
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.com", userRs.get(0));
        verifySuggestedUser("alastair", "knowles", "alastair@knowles.net", userRs.get(1));

        userRs = resourceApi.getSimilarUsers(Scope.BOARD, boardId, "alastair@fib");
        Assert.assertEquals(1, userRs.size());
        verifySuggestedUser("alastair", "fibinger", "alastair@fibinger.com", userRs.get(0));

        userRs = resourceApi.getSimilarUsers(Scope.DEPARTMENT, departmentId, "min");
        Assert.assertEquals(1, userRs.size());
        verifySuggestedUser("juan", "mingo", "juan@mingo.com", userRs.get(0));

        userRs = resourceApi.getSimilarUsers(Scope.BOARD, boardId, "xavier");
        Assert.assertEquals(0, userRs.size());

        testUserService.authenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> resourceApi.getSimilarUsers(Scope.DEPARTMENT, departmentId, "alastair"), ExceptionCode.FORBIDDEN_ACTION, null);

        testUserService.unauthenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> resourceApi.getSimilarUsers(Scope.BOARD, boardId, "alastair"), ExceptionCode.UNAUTHENTICATED_USER, null);
    }

    @Test
    @Transactional
    public void shouldLookupOrganizations() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        postApi.postPost(boardR.getId(), TestHelper.samplePost().setOrganizationName("lookupOrganization1"));
        postApi.postPost(boardR.getId(), TestHelper.samplePost().setOrganizationName("otherOrganization"));
        postApi.postPost(boardR.getId(), TestHelper.samplePost().setOrganizationName("lookupOrganization2"));

        List<String> results = resourceApi.lookupOrganizations("loo");
        assertThat(results, containsInAnyOrder("lookupOrganization1", "lookupOrganization2"));
    }

    public Matcher<ResourceUserRepresentation> resourceUserMatcher(String email, UserRoleDTO... roleDTOs) {
        Matcher<Object> emailMatcher = hasProperty("user", hasProperty("email", equalTo(email)));
        List<Matcher<? super Object>> roleMatchers = Stream.of(roleDTOs)
            .map(roleDTO -> Matchers.allOf(
                hasProperty("role", equalTo(roleDTO.getRole())),
                hasProperty("expiryDate", equalTo(roleDTO.getExpiryDate())),
                hasProperty("categories", equalTo(roleDTO.getCategories() != null ? roleDTO.getCategories() : Collections.emptyList()))))
            .collect(Collectors.toList());
        return allOf(emailMatcher, hasProperty("roles", containsInAnyOrder(roleMatchers)));
    }

    private void addAndRemoveUserRoles(User currentUser, Scope scope, Long resourceId) {
        List<ResourceUserRepresentation> resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));

        // add resource user with two roles
        UserDTO newUser = new UserDTO().setEmail("board-manager@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.createResourceUser(scope, resourceId,
            new ResourceUserDTO().setUser(newUser).setRoles(
                ImmutableSet.of(new UserRoleDTO(Role.MEMBER, null, MemberCategory.UNDERGRADUATE_STUDENT), new UserRoleDTO(Role.AUTHOR))));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR)),
            resourceUserMatcher("board-manager@mail.com",
                new UserRoleDTO(Role.MEMBER, null, MemberCategory.UNDERGRADUATE_STUDENT), new UserRoleDTO(Role.AUTHOR))));

        // remove one role
        resourceApi.updateResourceUser(scope, resourceId, boardManager.getUser().getId(),
            new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(new UserRoleDTO(Role.AUTHOR))));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR)),
            resourceUserMatcher("board-manager@mail.com", new UserRoleDTO(Role.AUTHOR))));

        // add one role
        resourceApi.updateResourceUser(scope, resourceId, boardManager.getUser().getId(),
            new ResourceUserDTO().setUser(newUser).setRoles(
                ImmutableSet.of(new UserRoleDTO(Role.MEMBER, null, MemberCategory.MASTER_STUDENT), new UserRoleDTO(Role.AUTHOR))));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR)),
            resourceUserMatcher("board-manager@mail.com", new UserRoleDTO(Role.AUTHOR), new UserRoleDTO(Role.MEMBER, null, MemberCategory.MASTER_STUDENT))));

        // remove user from resource
        resourceApi.deleteResourceUser(scope, resourceId, boardManager.getUser().getId());
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));
    }

    private void verifySuggestedUser(String expectedGivenName, String expectedSurname, String expectedEmail, UserRepresentation userR) {
        Assert.assertEquals(expectedGivenName, userR.getGivenName());
        Assert.assertEquals(expectedSurname, userR.getSurname());
        Assert.assertEquals(BoardUtils.obfuscateEmail(expectedEmail), userR.getEmail());

        String userIdString = userR.getId().toString();
        DocumentRepresentation documentImageR = userR.getDocumentImage();
        Assert.assertEquals(userIdString, documentImageR.getCloudinaryId());
        Assert.assertEquals(userIdString, documentImageR.getCloudinaryUrl());
        Assert.assertEquals(userIdString, documentImageR.getFileName());
    }

}
