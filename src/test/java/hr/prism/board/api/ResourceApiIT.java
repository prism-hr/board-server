package hr.prism.board.api;

import com.google.common.collect.ImmutableSet;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.ResourceUserBulkDTO;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ResourceUserRepresentation;
import hr.prism.board.service.UserService;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@TestContext
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
public class ResourceApiIT extends AbstractIT {

    @Inject
    private ResourceApi resourceApi;

    @Inject
    private BoardApi boardApi;

    @Inject
    private UserService userService;

    @Test
    public void shouldAddAndRemoveRoles() {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        addAndRemoveUserRoles(currentUser, Scope.BOARD, boardR.getId());
        addAndRemoveUserRoles(currentUser, Scope.DEPARTMENT, boardR.getDepartment().getId());
    }

    private void addAndRemoveUserRoles(User currentUser, Scope scope, Long resourceId) {
        List<ResourceUserRepresentation> resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR)));

        // add resource user with two roles
        UserDTO newUser = new UserDTO().setEmail("board-manager@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.addResourceUser(scope, resourceId,
            new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(Role.MEMBER, Role.AUTHOR)));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR),
            resourceUserMatcher("board-manager@mail.com", Role.MEMBER, Role.AUTHOR)));

        // remove one role
        resourceApi.removeUserRole(scope, resourceId, boardManager.getUser().getId(), Role.AUTHOR);
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR),
            resourceUserMatcher("board-manager@mail.com", Role.MEMBER)));

        // add one role
        resourceApi.addUserRole(scope, resourceId, boardManager.getUser().getId(), Role.ADMINISTRATOR);
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR),
            resourceUserMatcher("board-manager@mail.com", Role.MEMBER, Role.ADMINISTRATOR)));

        // remove user from resource
        resourceApi.removeResourceUser(scope, resourceId, boardManager.getUser().getId());
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR)));
    }

    @Test
    public void shouldNotRemoveLastRole() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        UserDTO newUser = new UserDTO().setEmail("last-role@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.addResourceUser(Scope.BOARD, boardR.getId(),
            new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(Role.MEMBER)));

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () -> resourceApi.removeUserRole(Scope.BOARD, boardR.getId(), boardManager.getUser().getId(), Role.MEMBER),
                ExceptionCode.IRREMOVABLE_USER_ROLE, status);
            return null;
        });
    }

    @Test
    public void shouldNotRemoveNonExistingRole() {
        testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        UserDTO newUser = new UserDTO().setEmail("non-existing-role@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.addResourceUser(Scope.BOARD, boardR.getId(),
            new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(Role.MEMBER, Role.AUTHOR)));

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () -> resourceApi.removeUserRole(Scope.BOARD, boardR.getId(), boardManager.getUser().getId(), Role
                .ADMINISTRATOR), ExceptionCode.NONEXISTENT_USER_ROLE, status);
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
        ResourceUserRepresentation boardManager = resourceApi.addResourceUser(Scope.DEPARTMENT, departmentId,
            new ResourceUserDTO().setUser(newUserDTO).setRoles(ImmutableSet.of(Role.ADMINISTRATOR, Role.MEMBER)));

        // remove current user as administrator
        resourceApi.removeResourceUser(Scope.DEPARTMENT, departmentId, creator.getId());

        // authenticate as another administrator
        User newUser = userService.findOne(boardManager.getUser().getId());
        testUserService.setAuthentication(newUser.getId());

        // try to remove yourself as administrator
        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () -> resourceApi.removeUserRole(Scope.DEPARTMENT, departmentId, boardManager.getUser().getId(), Role
                .ADMINISTRATOR), ExceptionCode.IRREMOVABLE_USER_ROLE, status);
            return null;
        });

        List<ResourceUserRepresentation> users = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentId);
        Assert.assertThat(users, contains(resourceUserMatcher(newUserDTO.getEmail(), Role.ADMINISTRATOR, Role.MEMBER)));
    }

    @Test
    public void shouldNotRemoveLastAdminUser() {
        User creator = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        boardDTO.getDepartment().setName("last-admin-user");
        BoardRepresentation boardR = boardApi.postBoard(boardDTO);
        Long departmentId = boardR.getDepartment().getId();

        transactionTemplate.execute(status -> {
            ExceptionUtils.verifyApiException(ApiException.class, () -> resourceApi.removeResourceUser(Scope.DEPARTMENT, departmentId, creator.getId()), ExceptionCode
                .IRREMOVABLE_USER, status);
            return null;
        });

        List<ResourceUserRepresentation> users = resourceApi.getResourceUsers(Scope.DEPARTMENT, departmentId);
        Assert.assertThat(users, contains(resourceUserMatcher(creator.getEmail(), Role.ADMINISTRATOR)));
    }

    @Test
    public void shouldAddUsersInBulk() {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        List<ResourceUserRepresentation> resourceUsers = resourceApi.getResourceUsers(Scope.BOARD, boardR.getId());
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR)));

        // add 200 members
        ResourceUserBulkDTO bulkDTO = new ResourceUserBulkDTO();
        bulkDTO.setUsers(new LinkedList<>());
        for (int i = 1; i <= 200; i++) {
            bulkDTO.getUsers().add(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"));
        }
        bulkDTO.setRoles(ImmutableSet.of(Role.MEMBER));

        resourceApi.addResourceUsers(Scope.BOARD, boardR.getId(), bulkDTO);

        int usersCount = resourceApi.getResourceUsers(Scope.BOARD, boardR.getId()).size();
        assertEquals(201, usersCount);

        bulkDTO = new ResourceUserBulkDTO();
        bulkDTO.setUsers(new LinkedList<>());
        for (int i = 101; i <= 300; i++) {
            bulkDTO.getUsers().add(new UserDTO().setEmail("bulk" + i + "@mail.com").setGivenName("Bulk" + i).setSurname("User"));
        }
        bulkDTO.setRoles(ImmutableSet.of(Role.MEMBER, Role.AUTHOR));

        resourceApi.addResourceUsers(Scope.BOARD, boardR.getId(), bulkDTO);

        usersCount = resourceApi.getResourceUsers(Scope.BOARD, boardR.getId()).size();
        assertEquals(301, usersCount);
    }

    public Matcher<ResourceUserRepresentation> resourceUserMatcher(String email, Role... roles) {
        Matcher<Object> emailMatcher = hasProperty("user", hasProperty("email", equalTo(email)));
        return allOf(emailMatcher, hasProperty("roles", containsInAnyOrder(roles)));
    }

}
