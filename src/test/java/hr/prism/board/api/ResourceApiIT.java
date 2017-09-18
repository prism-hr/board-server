package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.*;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.BoardUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
            ExceptionUtils.verifyException(BoardException.class,
                () -> resourceApi.deleteResourceUser(Scope.DEPARTMENT, departmentId, creator.getId()), ExceptionCode.IRREMOVABLE_USER, status);
            return null;
        });

        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(creator.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
    }

    @Test
    public void shouldAddUsersInBulk() throws InterruptedException {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        DepartmentRepresentation departmentR = boardApi.postBoard(boardDTO).getDepartment();
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentR.getId(), null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

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
    @Sql("classpath:data/resource_filter_setup.sql")
    public void shouldListAndFilterResources() {
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

        Long userId = transactionTemplate.execute(status -> userCacheService.findByEmail("department@administrator.com")).getId();
        testUserService.setAuthentication(userId);

        List<BoardRepresentation> boardRs = boardApi.getBoards(false, null, null, null);
        Assert.assertEquals(3, boardRs.size());

        List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Games", "Opportunities", "Housing");

        boardRs = boardApi.getBoards(false, null, null, "student");
        Assert.assertEquals(3, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Games", "Opportunities", "Housing");

        boardRs = boardApi.getBoards(false, null, null, "promote work experience");
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        userId = transactionTemplate.execute(status -> userCacheService.findByEmail("board@author.com")).getId();
        testUserService.setAuthentication(userId);

        boardRs = boardApi.getBoards(false, null, null, null);
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        boardRs = boardApi.getBoards(false, null, null, "student");
        Assert.assertEquals(1, boardRs.size());

        boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
        verifyContains(boardNames, "Opportunities");

        List<PostRepresentation> postRs = postApi.getPosts(false, null, null, null);
        Assert.assertEquals(2, postRs.size());

        List<String> postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer");

        postRs = postApi.getPosts(false, null, null, "optimise");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        postRs = postApi.getPosts(false, State.REJECTED, null, null);
        Assert.assertEquals(0, postRs.size());

        userId = transactionTemplate.execute(status -> userCacheService.findByEmail("department@member.com")).getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPosts(false, null, null, null);
        Assert.assertEquals(3, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");

        postRs = postApi.getPosts(false, null, null, "london");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        testUserService.unauthenticate();
        Long boardId = transactionTemplate.execute(status -> boardService.getBoard("cs/opportunities")).getId();

        postRs = postApi.getPostsByBoard(boardId, true, null, null, null);
        Assert.assertEquals(3, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer", "Java Web Developer", "Technical Analyst");

        postRs = postApi.getPostsByBoard(boardId, true, null, null, "london");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Database Engineer");

        userId = transactionTemplate.execute(status -> userCacheService.findByEmail("post@administrator.com")).getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPostsByBoard(boardId, false, null, null, null);
        Assert.assertEquals(7, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Technical Analyst", "Scrum Leader", "Product Manager", "Test Engineer");

        postRs = postApi.getPostsByBoard(boardId, false, null, null, "madrid krakow");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Technical Analyst");

        userId = transactionTemplate.execute(status -> userCacheService.findByEmail("board@administrator.com")).getId();
        testUserService.setAuthentication(userId);

        postRs = postApi.getPostsByBoard(boardId, false, null, null, null);
        Assert.assertEquals(9, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Support Engineer", "UX Designer", "Front-End Developer", "Database Engineer", "Java Web Developer", "Technical Analyst", "Scrum Leader",
            "Product Manager", "Test Engineer");

        postRs = postApi.getPostsByBoard(boardId, false, State.ACCEPTED, null, "service");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Java Web Developer");

        postRs = postApi.getPostsByBoard(boardId, false, State.ACCEPTED, null, "madrid krakow");
        Assert.assertEquals(2, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Java Web Developer", "Technical Analyst");

        ExceptionUtils.verifyException(BoardException.class,
            () -> postApi.getPosts(false, State.ARCHIVED, null, null), ExceptionCode.INVALID_RESOURCE_FILTER, null);

        List<String> archiveQuarters = resourceApi.getResourceArchiveQuarters(Scope.POST, boardId);
        verifyContains(archiveQuarters, "20164", "20171");

        postRs = postApi.getPosts(false, State.ARCHIVED, "20164", null);
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Software Architect");

        postRs = postApi.getPostsByBoard(boardId, false, State.ARCHIVED, "20171", "nuts");
        Assert.assertEquals(1, postRs.size());

        postNames = postRs.stream().map(PostRepresentation::getName).collect(Collectors.toList());
        verifyContains(postNames, "Business Analyst");

        postRs = postApi.getPostsByBoard(boardId, false, State.ARCHIVED, "20171", "guru");
        Assert.assertEquals(0, postRs.size());
    }

    @Test
    @Sql("classpath:data/user_role_filter_setup.sql")
    public void shouldListAndFilterUserRoles() {
        indexUserData();
        Long userId = transactionTemplate.execute(status -> userCacheService.findByEmail("alastair@knowles.com")).getId();
        testUserService.setAuthentication(userId);

        Long departmentId = transactionTemplate.execute(status -> resourceRepository.findByHandle("cs")).getId();
        UserRolesRepresentation userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null);
        Assert.assertEquals(2, userRoles.getUsers().size());
        Assert.assertEquals(2, userRoles.getMembers().size());
        Assert.assertEquals(2, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()), "alastair@knowles.com", "jakub@fibinger.com");

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "alastair");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()), "alastair@knowles.com");

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "alister");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()), "alastair@knowles.com");

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "beatriz");
        Assert.assertEquals(0, userRoles.getUsers().size());
        Assert.assertEquals(1, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()), "beatriz@rodriguez.com");

        userRoles = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, "felipe");
        Assert.assertEquals(0, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(1, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getMemberRequests().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()), "felipe@ieder.com");

        testUserService.unauthenticate();
        ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null), ExceptionCode.UNAUTHENTICATED_USER, null);

        testUserService.setAuthentication(userId);
        Long boardId = transactionTemplate.execute(status -> resourceRepository.findByHandle("cs/opportunities")).getId();
        userRoles = resourceApi.getUserRoles(Scope.BOARD, boardId, null);
        Assert.assertEquals(4, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()),
            "alastair@knowles.com", "jakub@fibinger.com", "jon@wheatley.com", "toby@godfrey.com");

        userRoles = resourceApi.getUserRoles(Scope.BOARD, boardId, "toby godfrey");
        Assert.assertEquals(1, userRoles.getUsers().size());
        Assert.assertEquals(0, userRoles.getMembers().size());
        Assert.assertEquals(0, userRoles.getMemberRequests().size());
        verifyContains(userRoles.getUsers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList()), "toby@godfrey.com");
    }

    @Test
    public void shouldUpdateUsersInBulk() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        Long departmentId = boardApi.postBoard(boardDTO).getDepartment().getId();
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        List<UserRoleDTO> userRoleDTOs = new ArrayList<>();
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@knowles.com"))
            .setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)));
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("jakub").setSurname("fibinger").setEmail("jakub@fibinger.com"))
            .setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT)));

        resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentId, userRoleDTOs);
        List<UserRoleRepresentation> members = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getMembers();

        verifyMember("jakub@fibinger.com", null, Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT), members.get(0));
        verifyMember("alastair@knowles.com", null, Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT), members.get(1));

        Long memberId = transactionTemplate.execute(status -> userRepository.findByEmail("alastair@knowles.com")).getId();
        testUserService.setAuthentication(memberId);

        userApi.updateUser(new UserPatchDTO().setEmail(Optional.of("alastair@alastair.com")));

        testUserService.setAuthentication(user.getId());
        userRoleDTOs = new ArrayList<>();
        userRoleDTOs.add(new UserRoleDTO().setUser(new UserDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@knowles.com"))
            .setRole(Role.MEMBER).setExpiryDate(LocalDate.of(2018, 7, 1)).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));

        resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentId, userRoleDTOs);
        members = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId, null).getMembers();

        verifyMember("jakub@fibinger.com", null, Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT), members.get(0));
        verifyMember("alastair@alastair.com", LocalDate.of(2018, 7, 1), Collections.singletonList(MemberCategory.MASTER_STUDENT), members.get(1));
    }

    @Test
    public void shouldReconcileAuthenticationsWithInvitations() {
        Long userId1 = testUserService.authenticate().getId();
        BoardRepresentation boardR1 = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()));
        DepartmentRepresentation departmentR1 = boardR1.getDepartment();
        Long departmentId1 = departmentR1.getId();
        Long boardId1 = boardR1.getId();

        transactionTemplate.execute(status -> resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentId1, Arrays.asList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member1").setSurname("member1").setEmail("member1@member1.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member2").setSurname("member2").setEmail("member2@member2.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member3").setSurname("member3").setEmail("member3@member3.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR1 = transactionTemplate.execute(status -> postApi.postPost(boardId1, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName1 = postR1.getName();
        String boardName1 = boardR1.getName();
        String departmentName1 = departmentR1.getName();

        User member1 = transactionTemplate.execute(status -> userRepository.findByEmail("member1@member1.com"));
        User member2 = transactionTemplate.execute(status -> userRepository.findByEmail("member2@member2.com"));
        User member3 = transactionTemplate.execute(status -> userRepository.findByEmail("member3@member3.com"));

        Resource department1 = transactionTemplate.execute(status -> resourceService.findOne(departmentId1));
        String department1memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department1, member1, Role.MEMBER)).getUuid();
        String department1memberRole2Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department1, member2, Role.MEMBER)).getUuid();
        String department1memberRole3Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department1, member3, Role.MEMBER)).getUuid();

        String parentRedirect1 = serverUrl + "/redirect?resource=" + boardId1;
        String resourceRedirect1 = serverUrl + "/redirect?resource=" + postR1.getId();
        User user1 = transactionTemplate.execute(status -> userCacheService.findOne(userId1));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user1,
                ImmutableMap.<String, String>builder().put("recipient", user1.getGivenName()).put("department", departmentName1).put("board", boardName1)
                    .put("post", postName1).put("resourceRedirect", resourceRedirect1).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member1,
                ImmutableMap.<String, String>builder().put("recipient", "member1").put("department", departmentName1).put("board", boardName1).put("post", postName1)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect1)
                    .put("invitationUuid",  department1memberRole1Uuid).put("modal", "register").put("parentRedirect", parentRedirect1)
                    .put("recipientUuid", member1.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member2,
                ImmutableMap.<String, String>builder().put("recipient", "member2").put("department", departmentName1).put("board", boardName1).put("post", postName1)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect1)
                    .put("invitationUuid",  department1memberRole2Uuid).put("modal", "register").put("parentRedirect", parentRedirect1)
                    .put("recipientUuid", member2.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member3,
                ImmutableMap.<String, String>builder().put("recipient", "member3").put("department", departmentName1).put("board", boardName1).put("post", postName1)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect1)
                    .put("invitationUuid",  department1memberRole3Uuid).put("modal", "register").put("parentRedirect", parentRedirect1)
                    .put("recipientUuid", member3.getUuid()).build()));

        Long postId1 = postR1.getId();
        transactionTemplate.execute(status -> authenticationApi.register(
            new RegisterDTO().setUuid(department1memberRole1Uuid).setGivenName("member1").setSurname("member1").setEmail("member1@member1.com").setPassword("password1")));
        testUserService.setAuthentication(member1.getId());

        postR1 = transactionTemplate.execute(status -> postApi.getPost(postId1, TestHelper.mockHttpServletRequest("ip1")));
        Assert.assertNotNull(postR1.getReferral());

        transactionTemplate.execute(status -> authenticationApi.register(
            new RegisterDTO().setUuid(department1memberRole2Uuid).setGivenName("member4").setSurname("member4").setEmail("member4@member4.com").setPassword("password4")));
        testUserService.setAuthentication(member2.getId());

        postR1 = transactionTemplate.execute(status -> postApi.getPost(postId1, TestHelper.mockHttpServletRequest("ip4")));
        Assert.assertNotNull(postR1.getReferral());

        testUserService.setAuthentication(userId1);
        List<String> emails1 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId1, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails1, "member1@member1.com", "member4@member4.com", "member3@member3.com");

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> authenticationApi.register(
                new RegisterDTO().setUuid(department1memberRole3Uuid).setGivenName("member1").setSurname("member1").setEmail("member1@member1.com").setPassword("password1")),
            ExceptionCode.DUPLICATE_USER, status));

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> authenticationApi.register(
                new RegisterDTO().setUuid(department1memberRole1Uuid).setGivenName("member1").setSurname("member1").setEmail("member1@member1.com").setPassword("password1")),
            ExceptionCode.DUPLICATE_AUTHENTICATION, status));

        Long userId2 = testUserService.authenticate().getId();
        BoardRepresentation boardR2 = transactionTemplate.execute(status -> boardApi.postBoard(
            new BoardDTO()
                .setName("board2")
                .setDepartment(new DepartmentDTO()
                    .setName("department2"))));

        DepartmentRepresentation departmentR2 = boardR2.getDepartment();
        Long departmentId2 = departmentR2.getId();
        Long boardId2 = boardR2.getId();

        transactionTemplate.execute(status -> resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentId2, Arrays.asList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member1").setSurname("member1").setEmail("member1@member1.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member3").setSurname("member3").setEmail("member3@member3.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR2 = transactionTemplate.execute(status -> postApi.postPost(boardId2, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName2 = postR2.getName();
        String boardName2 = boardR2.getName();
        String departmentName2 = departmentR2.getName();

        Resource department2 = transactionTemplate.execute(status -> resourceService.findOne(departmentId2));
        String department2memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department2, member1, Role.MEMBER)).getUuid();
        String department2memberRole3Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department2, member3, Role.MEMBER)).getUuid();

        String parentRedirect2 = serverUrl + "/redirect?resource=" + boardId2;
        String resourceRedirect2 = serverUrl + "/redirect?resource=" + postR2.getId();
        User user2 = transactionTemplate.execute(status -> userCacheService.findOne(userId2));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user2,
                ImmutableMap.<String, String>builder().put("recipient", user2.getGivenName()).put("department", departmentName2).put("board", boardName2)
                    .put("post", postName2).put("resourceRedirect", resourceRedirect2).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member1,
                ImmutableMap.<String, String>builder().put("recipient", "member1").put("department", departmentName2).put("board", boardName2).put("post", postName2)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect2)
                    .put("invitationUuid",  department2memberRole1Uuid).put("modal", "login").put("parentRedirect", parentRedirect2)
                    .put("recipientUuid", member1.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member3,
                ImmutableMap.<String, String>builder().put("recipient", "member3").put("department", departmentName2).put("board", boardName2).put("post", postName2)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect2)
                    .put("invitationUuid",  department2memberRole3Uuid).put("modal", "register").put("parentRedirect", parentRedirect2)
                    .put("recipientUuid", member3.getUuid()).build()));

        Long postId2 = postR2.getId();
        transactionTemplate.execute(status -> authenticationApi.login(
            new LoginDTO().setUuid(department2memberRole1Uuid).setEmail("member1@member1.com").setPassword("password1")));
        testUserService.setAuthentication(member1.getId());

        postR2 = transactionTemplate.execute(status -> postApi.getPost(postId2, TestHelper.mockHttpServletRequest("ip1")));
        Assert.assertNotNull(postR2.getReferral());

        transactionTemplate.execute(status -> authenticationApi.login(
            new LoginDTO().setUuid(department2memberRole3Uuid).setEmail("member4@member4.com").setPassword("password4")));
        testUserService.setAuthentication(member2.getId());

        postR2 = transactionTemplate.execute(status -> postApi.getPost(postId2, TestHelper.mockHttpServletRequest("ip1")));
        Assert.assertNotNull(postR2.getReferral());

        testUserService.setAuthentication(userId2);
        List<String> emails2 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId2, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails2, "member1@member1.com", "member4@member4.com");

        Long userId3 = testUserService.authenticate().getId();
        BoardRepresentation boardR3 = transactionTemplate.execute(status -> boardApi.postBoard(
            new BoardDTO()
                .setName("board3")
                .setDepartment(new DepartmentDTO()
                    .setName("department3"))));

        DepartmentRepresentation departmentR3 = boardR3.getDepartment();
        Long departmentId3 = departmentR3.getId();
        Long boardId3 = boardR3.getId();

        transactionTemplate.execute(status -> resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentId3, Arrays.asList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member5").setSurname("member5").setEmail("member5@member5.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member6").setSurname("member6").setEmail("member6@member6.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member7").setSurname("member7").setEmail("member7@member7.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR3 = transactionTemplate.execute(status -> postApi.postPost(boardId3, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName3 = postR3.getName();
        String boardName3 = boardR3.getName();
        String departmentName3 = departmentR3.getName();

        User member5 = transactionTemplate.execute(status -> userRepository.findByEmail("member5@member5.com"));
        User member6 = transactionTemplate.execute(status -> userRepository.findByEmail("member6@member6.com"));
        User member7 = transactionTemplate.execute(status -> userRepository.findByEmail("member7@member7.com"));

        Resource department3 = transactionTemplate.execute(status -> resourceService.findOne(departmentId3));
        String department3memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department3, member5, Role.MEMBER)).getUuid();
        String department3memberRole2Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department3, member6, Role.MEMBER)).getUuid();
        String department3memberRole3Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department3, member7, Role.MEMBER)).getUuid();

        String parentRedirect3 = serverUrl + "/redirect?resource=" + boardId3;
        String resourceRedirect3 = serverUrl + "/redirect?resource=" + postR3.getId();
        User user3 = transactionTemplate.execute(status -> userCacheService.findOne(userId3));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user3,
                ImmutableMap.<String, String>builder().put("recipient", user3.getGivenName()).put("department", departmentName3).put("board", boardName3)
                    .put("post", postName3).put("resourceRedirect", resourceRedirect3).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member5,
                ImmutableMap.<String, String>builder().put("recipient", "member5").put("department", departmentName3).put("board", boardName3).put("post", postName3)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect3)
                    .put("invitationUuid",  department3memberRole1Uuid).put("modal", "register").put("parentRedirect", parentRedirect3)
                    .put("recipientUuid", member5.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member6,
                ImmutableMap.<String, String>builder().put("recipient", "member6").put("department", departmentName3).put("board", boardName3).put("post", postName3)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect3)
                    .put("invitationUuid",  department3memberRole2Uuid).put("modal", "register").put("parentRedirect", parentRedirect3)
                    .put("recipientUuid", member6.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member7,
                ImmutableMap.<String, String>builder().put("recipient", "member7").put("department", departmentName3).put("board", boardName3).put("post", postName3)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect3)
                    .put("invitationUuid",  department3memberRole3Uuid).put("modal", "register").put("parentRedirect", parentRedirect3)
                    .put("recipientUuid", member7.getUuid()).build()));

        Long postId3 = postR3.getId();
        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department3memberRole1Uuid).setClientId("clientId").setCode("code").setRedirectUri("redirectUri")));
        testUserService.setAuthentication(member5.getId());

        postR3 = transactionTemplate.execute(status -> postApi.getPost(postId3, TestHelper.mockHttpServletRequest("ip5")));
        Assert.assertNotNull(postR3.getReferral());

        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department3memberRole2Uuid).setClientId("clientId2").setCode("code2").setRedirectUri("redirectUri2")));
        testUserService.setAuthentication(member6.getId());

        postR3 = transactionTemplate.execute(status -> postApi.getPost(postId3, TestHelper.mockHttpServletRequest("ip6")));
        Assert.assertNotNull(postR3.getReferral());

        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department3memberRole3Uuid).setClientId("clientId3").setCode("code3").setRedirectUri("redirectUri3")));
        testUserService.setAuthentication(member1.getId());

        postR3 = transactionTemplate.execute(status -> postApi.getPost(postId3, TestHelper.mockHttpServletRequest("ip7")));
        Assert.assertNotNull(postR3.getReferral());

        testUserService.setAuthentication(userId3);
        List<String> emails3 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId3, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails3, "alastair@prism.hr", "jakub@prism.hr", "member1@member1.com");

        Long userId4 = testUserService.authenticate().getId();
        BoardRepresentation boardR4 = transactionTemplate.execute(status -> boardApi.postBoard(
            new BoardDTO()
                .setName("board4")
                .setDepartment(new DepartmentDTO()
                    .setName("department4"))));

        DepartmentRepresentation departmentR4 = boardR4.getDepartment();
        Long departmentId4 = departmentR4.getId();
        Long boardId4 = boardR4.getId();

        transactionTemplate.execute(status -> resourceApi.createResourceUsers(Scope.DEPARTMENT, departmentId4, Collections.singletonList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member8").setSurname("member8").setEmail("member8@member8.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR4 = transactionTemplate.execute(status -> postApi.postPost(boardId4, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName4 = postR4.getName();
        String boardName4 = boardR4.getName();
        String departmentName4 = departmentR4.getName();

        User member8 = transactionTemplate.execute(status -> userRepository.findByEmail("member8@member8.com"));

        Resource department4 = transactionTemplate.execute(status -> resourceService.findOne(departmentId4));
        String department4memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department4, member8, Role.MEMBER)).getUuid();

        String parentRedirect4 = serverUrl + "/redirect?resource=" + boardId4;
        String resourceRedirect4 = serverUrl + "/redirect?resource=" + postR4.getId();
        User user4 = transactionTemplate.execute(status -> userCacheService.findOne(userId4));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user4,
                ImmutableMap.<String, String>builder().put("recipient", user4.getGivenName()).put("department", departmentName4).put("board", boardName4)
                    .put("post", postName4).put("resourceRedirect", resourceRedirect4).put("modal", "login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member8,
                ImmutableMap.<String, String>builder().put("recipient", "member8").put("department", departmentName4).put("board", boardName4).put("post", postName4)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect4)
                    .put("invitationUuid",  department4memberRole1Uuid).put("modal", "register").put("parentRedirect", parentRedirect4)
                    .put("recipientUuid", member8.getUuid()).build()));

        Long postId4 = postR4.getId();
        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department4memberRole1Uuid).setClientId("clientId").setCode("code").setRedirectUri("redirectUri")));
        testUserService.setAuthentication(member5.getId());

        postR4 = transactionTemplate.execute(status -> postApi.getPost(postId4, TestHelper.mockHttpServletRequest("ip5")));
        Assert.assertNotNull(postR4.getReferral());

        testUserService.setAuthentication(userId4);
        List<String> emails4 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId4, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails4, "alastair@prism.hr");
    }

    private void addAndRemoveUserRoles(User user, Scope scope, Long resourceId) {
        List<UserRoleRepresentation> users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // add ADMINISTRATOR role
        UserDTO newUser = new UserDTO().setEmail("board@mail.com").setGivenName("Sample").setSurname("User");
        UserRoleRepresentation resourceManager = resourceApi.createResourceUser(scope, resourceId, new UserRoleDTO().setUser(newUser).setRole(Role.ADMINISTRATOR));
        users = resourceApi.getUserRoles(scope, resourceId, null).getUsers();
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail(user.getEmail())).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));
        verifyContains(users, new UserRoleRepresentation().setUser(new UserRepresentation().setEmail("board@mail.com")).setRole(Role.ADMINISTRATOR).setState(State.ACCEPTED));

        // replace with MEMBER role
        UserRoleRepresentation resourceUser = resourceApi.updateResourceUser(scope, resourceId, resourceManager.getUser().getId(),
            new UserRoleDTO().setUser(newUser).setRole(Role.MEMBER).setCategories(Collections.singletonList(MemberCategory.MASTER_STUDENT)));
        verifyContains(Collections.singletonList(resourceUser), new UserRoleRepresentation().setUser(
            new UserRepresentation().setEmail("board@mail.com")).setRole(Role.MEMBER).setState(State.ACCEPTED));

        // remove from resource
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

    private void verifyMember(String expectedEmail, LocalDate expectedExpiryDate, List<MemberCategory> expectedMemberCategories, UserRoleRepresentation actual) {
        Assert.assertEquals(expectedEmail, actual.getUser().getEmail());
        Assert.assertEquals(expectedExpiryDate, actual.getExpiryDate());
        Assert.assertEquals(expectedMemberCategories, actual.getCategories());
    }

}
