package hr.prism.board.api;

import com.google.common.collect.Streams;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.representation.*;
import hr.prism.board.util.BoardUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@TestContext
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
public class ResourceApiIT extends AbstractIT {

    @Inject
    private ResourceRepository resourceRepository;

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

        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(newUserDTO.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
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

        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(creator.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldAddUsersInBulk() throws InterruptedException {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        DepartmentRepresentation departmentR = boardApi.postBoard(boardDTO).getDepartment();
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(currentUser.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

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
        UserRolesRepresentation response = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null);
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
        response = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null);
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

        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");

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

    @Test
    @Sql("classpath:data/filter_setup.sql")
    public void shouldSupportFilters() {
        transactionTemplate.execute(status -> {
            Streams.stream(resourceRepository.findAll()).sorted((resource1, resource2) -> ObjectUtils.compare(resource1.getId(), resource2.getId())).forEach(resource -> {
                if (Arrays.asList(Scope.DEPARTMENT, Scope.BOARD).contains(resource.getScope())) {
                    resourceService.setIndexDataAndQuarter(resource);
                } else {
                    postService.setIndexDataAndQuarter((Post) resource);
                }
            });

            return null;
        });

        Long userId = transactionTemplate.execute(status -> userCacheService.findByEmail("administrator@administrator.com")).getId();
        testUserService.setAuthentication(userId);

        List<BoardRepresentation> boardRs = boardApi.getBoards(false, null, null, null);
        Assert.assertEquals(3, boardRs.size());

        boardRs = boardApi.getBoards(false, null, null, "student");
        Assert.assertEquals(3, boardRs.size());

        boardRs = boardApi.getBoards(false, null, null, "promote work experience");
        Assert.assertEquals(1, boardRs.size());

        userId = transactionTemplate.execute(status -> userCacheService.findByEmail("author@author.com")).getId();
        testUserService.setAuthentication(userId);

        boardRs = boardApi.getBoards(false, null, null, null);
        Assert.assertEquals(1, boardRs.size());

        boardRs = boardApi.getBoards(false, null, null, "student");
        Assert.assertEquals(1, boardRs.size());
    }

    private void addAndRemoveUserRoles(User user, Scope scope, Long resourceId) {
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // add resource user a role
        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation resourceManager = resourceApi.createResourceUser(scope, resourceId, new UserRoleDTO().setUser(newUser).setRole(Role.ADMINISTRATOR));
        users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail("board@mail.com")).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // replace it with MEMBER role
        UserRoleRepresentation resourceUser = resourceApi.updateResourceUser(scope, resourceId, resourceManager.getUser().getId(),
            new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));
        verifyContains(Collections.singletonList(resourceUser), new UserRoleRepresentation().setUser(
            new UserRepresentation().setEmail("board@mail.com")).setRole(Role.MEMBER).setState(State.ACCEPTED));

        // remove user from resource
        resourceApi.deleteResourceUser(scope, resourceId, resourceManager.getUser().getId());
        users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
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

    private void verifyContains(List<UserRoleRepresentation> responses, UserRoleRepresentation expectedToContain) {
        boolean passed = false;
        for (UserRoleRepresentation response : responses) {
            if (response.equals(expectedToContain)) {
                passed = true;
            }
        }

        Assert.assertTrue(passed);
    }

}
