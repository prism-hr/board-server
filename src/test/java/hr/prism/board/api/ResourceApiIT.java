package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.util.BoardUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

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
    public void shouldNotRemoveLastAdminRole() {
        User creator = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        boardDTO.getDepartment().setName("last-admin-role");
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);
        Long departmentId = boardR.getDepartment().getId();

        // add another administrator
        UserDTO newUserDTO = new UserDTO().setEmail("last-admin-role@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation boardManager = resourceApi.createResourceUser(Scope.DEPARTMENT, departmentId,
            new UserRoleDTO().setUser(newUserDTO).setRole(Role.ADMINISTRATOR));

        // remove current user as administrator
        resourceApi.deleteResourceUser(Scope.DEPARTMENT, departmentId, creator.getId());

        // authenticate as another administrator
        User newUser = userCacheService.findOneFresh(boardManager.getUser().getId());
        testUserService.setAuthentication(newUser.getId());

        // try to remove yourself as administrator
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyException(BoardException.class,
                () -> resourceApi.updateResourceUser(Scope.DEPARTMENT, departmentId, boardManager.getUser().getId(),
                    new UserRoleDTO().setUser(newUserDTO).setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT))),
                ExceptionCode.IRREMOVABLE_USER_ROLE, status);
            return null;
        });

        List<UserRoleRepresentation> users = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentId).getUsers();
        Assert.assertThat(users, contains(userRoleMatcher(newUserDTO.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));
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

        List<UserRoleRepresentation> users = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentId).getUsers();
        Assert.assertThat(users, contains(userRoleMatcher(creator.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));
    }

    @Test
    public void shouldAddUsersInBulk() throws InterruptedException {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        DepartmentRepresentation departmentR = boardApi.postBoard(boardDTO).getDepartment();
        List<UserRoleRepresentation> resourceUsers = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentR.getId()).getUsers();
        Assert.assertThat(resourceUsers, contains(userRoleMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));

        // add 200 members
        List<UserRoleDTO> userRoleDTOs1 = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            userRoleDTOs1.add(
                new UserRoleDTO()
                    .setUser(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"))
                    .setRole(Role.MEMBER)
                    .setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));
        }

        resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentR.getId(), userRoleDTOs1);
        UserRolesRepresentation response = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentR.getId());
        assertEquals(1, response.getUsers().size());
        assertEquals(200, response.getMembers().size());

        List<UserRoleDTO> userRoleDTOs2 = new ArrayList<>();
        for (int i = 101; i <= 300; i++) {
            userRoleDTOs2.add(
                new UserRoleDTO()
                    .setUser(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"))
                    .setRole(Role.MEMBER)
                    .setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));
        }

        resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentR.getId(), userRoleDTOs2);
        response = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentR.getId());
        assertEquals(300, response.getMembers().size());

        List<UserRoleDTO> userRoleDTOs3 = new ArrayList<>();
        userRoleDTOs3.add(
            new UserRoleDTO()
                .setUser(new UserDTO().setEmail("bulk301@mail.com").setGivenName("Bulk301").setSurname("User"))
                .setRole(Role.AUTHOR));
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentR.getId(), userRoleDTOs3), ExceptionCode.INVALID_RESOURCE_USER, null);
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
                new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER)
                    .setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT))),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try to add a user to a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.createResourceUser(Scope.DEPARTMENT, boardR.getDepartment().getId(),
                new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER)
                    .setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT))),
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
                new UserRoleDTO().setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT))),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try with a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.updateResourceUser(Scope.DEPARTMENT, boardR.getDepartment().getId(), currentUser.getId(),
                new UserRoleDTO().setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.RESEARCH_STUDENT))),
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

    public Matcher<UserRoleRepresentation> userRoleMatcher(String email, UserRoleDTO roleDTO) {
        Matcher<Object> emailMatcher = hasProperty("user", hasProperty("email", equalTo(email)));
        Matcher<? super Object> roleMatcher = Matchers.allOf(
            hasProperty("role", equalTo(roleDTO.getRole())),
            hasProperty("expiryDate", equalTo(roleDTO.getExpiryDate())),
            hasProperty("categories", equalTo(roleDTO.getCategories() != null ? roleDTO.getCategories() : Collections.emptyList())));
        return allOf(emailMatcher, roleMatcher);
    }

    private void addAndRemoveUserRoles(User currentUser, Scope scope, Long resourceId) {
        List<UserRoleRepresentation> resourceUsers = resourceApi.getResourceUsers(scope, resourceId).getUsers();
        Assert.assertThat(resourceUsers, contains(userRoleMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));

        // add resource user a role
        UserDTO newUser = new UserDTO().setEmail("board-manager@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation boardManager = resourceApi.createResourceUser(scope, resourceId, new UserRoleDTO().setUser(newUser).setRole(Role.AUTHOR));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId).getUsers();
        Assert.assertThat(resourceUsers, containsInAnyOrder(userRoleMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR)),
            userRoleMatcher("board-manager@mail.com", new UserRoleDTO(Role.AUTHOR))));

        // replace it with MEMBER role
        UserRoleRepresentation resourceUser = resourceApi.updateResourceUser(scope, resourceId, boardManager.getUser().getId(),
            new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));

        Assert.assertThat(resourceUser, userRoleMatcher("board-manager@mail.com", new UserRoleDTO(Role.MEMBER, null, MemberCategory.MASTER_STUDENT)));

        // replace it with ADMINISTRATOR role
        resourceApi.updateResourceUser(scope, resourceId, boardManager.getUser().getId(), new UserRoleDTO().setUser(newUser).setRole(Role.ADMINISTRATOR));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId).getUsers();
        Assert.assertThat(resourceUsers, containsInAnyOrder(userRoleMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR)),
            userRoleMatcher("board-manager@mail.com", new UserRoleDTO(Role.ADMINISTRATOR))));

        // remove user from resource
        resourceApi.deleteResourceUser(scope, resourceId, boardManager.getUser().getId());
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId).getUsers();
        Assert.assertThat(resourceUsers, contains(userRoleMatcher(currentUser.getEmail(), new UserRoleDTO(Role.ADMINISTRATOR))));
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
