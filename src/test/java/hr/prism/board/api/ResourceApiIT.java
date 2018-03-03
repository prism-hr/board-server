package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.utils.BoardUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@TestContext
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
public class ResourceApiIT extends AbstractIT {

    @Test
    public void shouldAddAndRemoveRoles() {
        User currentUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        addAndRemoveUserRoles(currentUser, Scope.DEPARTMENT, departmentId);
    }

    @Test
    public void shouldNotRemoveLastAdminRole() {
        User creator = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.postDepartment(universityId, new DepartmentDTO().setName("last-admin-role").setSummary("last-admin-role summary")).getId();

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
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.updateResourceUser(Scope.DEPARTMENT, departmentId, boardManager.getUser().getId(),
                new UserRoleDTO().setUser(newUserDTO).setRole(Role.MEMBER).setMemberCategory(MemberCategory.MASTER_STUDENT)),
            ExceptionCode.IRREMOVABLE_USER_ROLE);

        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(BoardUtils.obfuscateEmail(newUserDTO.getEmail()))).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldNotRemoveLastAdminUser() {
        User creator = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.postDepartment(universityId, new DepartmentDTO().setName("last-admin-user").setSummary("last-admin-user summary")).getId();
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.deleteResourceUser(Scope.DEPARTMENT, departmentId, creator.getId()), ExceptionCode.IRREMOVABLE_USER);

        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(creator.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldNotAddUserWithNotExistingMemberCategory() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(departmentId, boardDTO);

        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");

        // try to add a user to a board
        departmentApi.patchDepartment(departmentId,
            new DepartmentPatchDTO().setMemberCategories(Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))));
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.createResourceUser(Scope.BOARD, boardR.getId(),
                new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER).setMemberCategory(MemberCategory.RESEARCH_STUDENT)),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try to add a user to a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.createResourceUser(Scope.DEPARTMENT, boardR.getDepartment().getId(),
                new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER).setMemberCategory(MemberCategory.RESEARCH_STUDENT)),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
    }

    @Test
    public void shouldNotAddUserRoleWithUnactivatedMemberCategory() {
        User user = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        BoardDTO boardDTO = TestHelper.sampleBoard();
        BoardRepresentation boardR = boardApi.postBoard(departmentId, boardDTO);

        // try with a board
        departmentApi.patchDepartment(departmentId,
            new DepartmentPatchDTO().setMemberCategories(Optional.of(Arrays.asList(MemberCategory.UNDERGRADUATE_STUDENT, MemberCategory.MASTER_STUDENT))));
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.updateResourceUser(Scope.BOARD, boardR.getId(), user.getId(),
                new UserRoleDTO().setRole(Role.MEMBER).setMemberCategory(MemberCategory.RESEARCH_STUDENT)),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);

        // try with a department
        ExceptionUtils.verifyException(BoardException.class,
            () -> resourceApi.updateResourceUser(Scope.DEPARTMENT, boardR.getDepartment().getId(), user.getId(),
                new UserRoleDTO().setRole(Role.MEMBER).setMemberCategory(MemberCategory.RESEARCH_STUDENT)),
            ExceptionCode.INVALID_USER_ROLE_MEMBER_CATEGORIES, null);
    }

    @Test
    @Sql("classpath:data/user_autosuggest_setup.sql")
    public void shouldGetSimilarUsers() {
        testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();
        Long departmentId = departmentApi.postDepartment(universityId, new DepartmentDTO().setName("department").setSummary("department summary")).getId();
        Long boardId = boardApi.postBoard(departmentId, TestHelper.sampleBoard()).getId();

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
    @Sql("classpath:data/resource_filter_setup.sql")
    public void shouldListAndFilterResources() {
        resourceRepository.findAll().stream().sorted((resource1, resource2) -> ObjectUtils.compare(resource1.getId(), resource2.getId()))
            .forEach(resource -> {
                if (Arrays.asList(Scope.UNIVERSITY, Scope.DEPARTMENT, Scope.BOARD).contains(resource.getScope())) {
                    resourceService.setIndexDataAndQuarter(resource);
                } else {
                    postService.setIndexDataAndQuarter((Post) resource);
                }

                resourceRepository.update(resource);
            });

        Long userId = userCacheService.findByEmail("department@administrator.com").getId();
        testUserService.setAuthentication(userId);

        List<BoardRepresentation> boardRs = boardApi.getBoards(null, false, null, null, null);
        Assert.assertEquals(2, boardRs.size());

        List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities", "Housing");

        boardRs = boardApi.getBoards(null, false, null, null, "student");
        Assert.assertEquals(2, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities", "Housing");

        boardRs = boardApi.getBoards(null, false, null, null, "promote work experience");
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        userId = userCacheService.findByEmail("department@author.com").getId();
        testUserService.setAuthentication(userId);

        boardRs = boardApi.getBoards(null, false, null, null, null);
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        boardRs = boardApi.getBoards(null, false, null, null, "student");
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        List<PostRepresentation> postRs = postApi.getPosts(null, false, null, null, null);
        Assert.assertEquals(2, postRs.size());

        List<String> postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer");

        postRs = postApi.getPosts(null, false, null, null, "optimise");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        postRs = postApi.getPosts(null, false, State.REJECTED, null, null);
        Assert.assertEquals(0, postRs.size());

        userId = userCacheService.findByEmail("department@member.com").getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(null, false, null, null, null);
        Assert.assertEquals(3, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");

        postRs = postApi.getPosts(null, false, null, null, "london");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        testUserService.unauthenticate();
        Long boardId = boardService.getBoard("ed/cs/opportunities").getId();

        postRs = postApi.getPosts(boardId, true, null, null, null);
        Assert.assertEquals(3, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");

        postRs = postApi.getPosts(boardId, true, null, null, "london");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        userId = userCacheService.findByEmail("post@administrator.com").getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(boardId, false, null, null, null);
        Assert.assertEquals(7, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Technical Analyst", "Scrum Leader", "Product Manager", "Test Engineer");

        postRs = postApi.getPosts(boardId, false, null, null, "madrid krakow");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Technical Analyst");

        userId = userCacheService.findByEmail("department@administrator.com").getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(boardId, false, null, null, null);
        Assert.assertEquals(9, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Database Engineer", "Java Web Developer", "Technical Analyst", "Scrum Leader",
            "Product Manager", "Test Engineer");

        postRs = postApi.getPosts(boardId, false, State.ACCEPTED, null, "service");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Java Web Developer");

        postRs = postApi.getPosts(boardId, false, State.ACCEPTED, null, "madrid krakow");
        Assert.assertEquals(2, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Java Web Developer", "Technical Analyst");

        ExceptionUtils.verifyException(BoardException.class,
            () -> postApi.getPosts(null, false, State.ARCHIVED, null, null), ExceptionCode.INVALID_RESOURCE_FILTER, null);

        List<String> archiveQuarters = resourceApi.getResourceArchiveQuarters(Scope.POST, boardId);
        verifyContains(archiveQuarters, "20164", "20171");

        postRs = postApi.getPosts(null, false, State.ARCHIVED, "20164", null);
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Software Architect");

        postRs = postApi.getPosts(boardId, false, State.ARCHIVED, "20171", "nuts");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Business Analyst");

        postRs = postApi.getPosts(boardId, false, State.ARCHIVED, "20171", "guru");
        Assert.assertEquals(0, postRs.size());
    }

    @Test
    @Sql("classpath:data/user_role_filter_setup.sql")
    public void shouldListAndFilterUserRoles() {
        for (User user : userRepository.findAll()) {
            userCacheService.setIndexData(user);
            userRepository.update(user);
        }

        Long userId = userCacheService.findByEmail("alastair@knowles.com").getId();
        testUserService.setAuthentication(userId);

        Long departmentId = resourceRepository.findByHandle("cs").getId();
        UserRolesRepresentation userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null);
        Assert.assertEquals(2, userRoles.getUsers().size());
        Assert.assertEquals(2, userRoles.getMembers().size());
        Assert.assertEquals(2, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"), BoardUtils.obfuscateEmail("jakub@fibinger.com"));

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "alastair");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"));

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "alister");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"));

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "beatriz");
        Assert.assertEquals(0, userRoles.getUsers().size());
        Assert.assertEquals(1, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("beatriz@rodriguez.com"));

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "felipe");
        Assert.assertEquals(0, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(1, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getMemberRequests().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("felipe@ieder.com"));

        testUserService.unauthenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null), ExceptionCode.UNAUTHENTICATED_USER, null);

        testUserService.setAuthentication(userId);
        Long boardId = resourceRepository.findByHandle("cs/opportunities").getId();
        userRoles = resourceApi.getUserRoles(Scope.BOARD, boardId, null);
        Assert.assertEquals(4, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("alastair@knowles.com"), BoardUtils.obfuscateEmail("jakub@fibinger.com"),
            BoardUtils.obfuscateEmail("jon@wheatley.com"), BoardUtils.obfuscateEmail("toby@godfrey.com"));

        userRoles = resourceApi.getUserRoles(Scope.BOARD, boardId, "toby godfrey");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            BoardUtils.obfuscateEmail("toby@godfrey.com"));
    }

    private void addAndRemoveUserRoles(User user, Scope scope, Long resourceId) {
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // add ADMINISTRATOR role
        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation resourceManager = resourceApi.createResourceUser(scope, resourceId, new UserRoleDTO().setUser(newUser).setRole(Role.ADMINISTRATOR));
        users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(BoardUtils.obfuscateEmail("board@mail.com"))).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // replace with MEMBER role
        UserRoleRepresentation resourceUser = resourceApi.updateResourceUser(scope, resourceId, resourceManager.getUser().getId(),
            new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER).setMemberCategory(MemberCategory.MASTER_STUDENT));
        verifyContains(Collections.singletonList(resourceUser), new UserRoleRepresentation().setUser(
            new UserRepresentation().setEmail(BoardUtils.obfuscateEmail("board@mail.com"))).setRole(Role.MEMBER).setState(State.ACCEPTED));

        // remove from resource
        resourceApi.deleteResourceUser(scope, resourceId, resourceManager.getUser().getId());
        users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation()
            .setEmail(user.getEmailDisplay())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
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
