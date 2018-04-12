package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@TestContext
@RunWith(SpringRunner.class)
public class UserApiIT extends AbstractIT {

    @Value("${local.server.port}")
    private String localServerPort;

    private CompletableFuture<Object> completableFuture = new CompletableFuture<>();

    @Test
    public void shouldCreateAndUpdateUser() {
        testUserService.authenticate();
        DocumentDTO imageDTO = new DocumentDTO().setCloudinaryId("userImage")
            .setCloudinaryUrl("userImage")
            .setFileName("userImage");
        UserPatchDTO userDTO = new UserPatchDTO()
            .setGivenName(Optional.of("first"))
            .setSurname(Optional.of("second"))
            .setEmail(Optional.of("changed@email.com"))
            .setDocumentImage(Optional.of(imageDTO));
        userApi.updateUser(userDTO);

        UserRepresentation user = userApi.getUser();
        Assert.assertEquals("first", user.getGivenName());
        Assert.assertEquals("second", user.getSurname());
        Assert.assertEquals("changed@email.com", user.getEmail());
        verifyDocument(imageDTO, user.getDocumentImage());
    }

    @Test
    public void shouldNotCreateDuplicateUserByUpdating() {
        User user1 = testUserService.authenticate();
        User user2 = testUserService.authenticate();

        testUserService.setAuthentication(user1.getId());
        ExceptionUtils.verifyException(
            BoardException.class,
            () -> userApi.updateUser(
                new UserPatchDTO().setEmail(Optional.of(user2.getEmail()))),
            ExceptionCode.DUPLICATE_USER);
    }

    @Test
    public void shouldCreateAndUpdateNotificationSuppressions() {
        User adminUser = testUserService.authenticate();
        Long universityId = universityService.getOrCreateUniversity("University College London", "ucl").getId();

        Long department1id = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department1")).getId();
        departmentApi.updateDepartment(department1id, new DepartmentPatchDTO().setMemberCategories(Optional.empty()));
        Long board11id = boardApi.createBoard(department1id, new BoardDTO().setName("board11")).getId();
        Long board12id = boardApi.createBoard(department1id, new BoardDTO().setName("board12")).getId();
        boardApi.createBoard(department1id, new BoardDTO().setName("board13"));

        Long department2id = departmentApi.createDepartment(universityId, new DepartmentDTO().setName("department2")
            .setMemberCategories(Collections.singletonList(MemberCategory.UNDERGRADUATE_STUDENT))).getId();
        boardApi.createBoard(department2id, new BoardDTO().setName("board23"));

        User memberUser1 = testUserService.authenticate();
        User memberUser2 = testUserService.authenticate();
        User memberUser3 = testUserService.authenticate();

        String memberUser1Email = memberUser1.getEmail();
        String memberUser2Email = memberUser2.getEmail();
        testUserService.setAuthentication(adminUser.getId());
        for (String memberUserEmail : new String[]{memberUser1Email, memberUser2Email}) {
            departmentUserApi.createUserRole(department1id, new UserRoleDTO().setUser(
                new UserDTO().setEmail(memberUserEmail)).setRole(Role.MEMBER));

            departmentUserApi.createUserRole(department2id, new UserRoleDTO().setUser(
                new UserDTO().setEmail(memberUserEmail)).setRole(Role.AUTHOR));
        }

        departmentUserApi.createUserRole(department2id, new UserRoleDTO().setUser(
            new UserDTO().setEmail(memberUser1Email)).setRole(Role.MEMBER)
            .setMemberCategory(MemberCategory.UNDERGRADUATE_STUDENT));

        Long adminUserId = adminUser.getId();
        Long memberUser1Id = memberUser1.getId();
        Long memberUser2Id = memberUser2.getId();
        Long memberUser3Id = memberUser3.getId();

        testUserService.setAuthentication(adminUserId);
        String[] expectedBoardNames = new String[]{"board11", "board12", "board13", "board23"};
        List<UserNotificationSuppressionRepresentation> adminUserSuppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.getSuppressions(), expectedBoardNames);
        Assert.assertEquals(4, adminUserSuppressions.size());
        adminUserSuppressions.forEach(suppression -> Assert.assertEquals(false, suppression.getSuppressed()));

        testUserService.setAuthentication(memberUser1Id);
        userApi.postSuppressions();
        List<UserNotificationSuppressionRepresentation> memberUser1Suppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.getSuppressions(), expectedBoardNames);
        Assert.assertEquals(4, memberUser1Suppressions.size());
        memberUser1Suppressions.forEach(suppression -> Assert.assertEquals(true, suppression.getSuppressed()));

        testUserService.unauthenticate();
        userApi.postSuppression(board11id, memberUser2.getUuid());
        testUserService.setAuthentication(memberUser2Id);
        userApi.postSuppression(board12id, null);
        List<UserNotificationSuppressionRepresentation> memberUser2Suppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.getSuppressions(), expectedBoardNames);
        Assert.assertEquals(4, memberUser2Suppressions.size());
        memberUser2Suppressions.subList(0, 2)
            .forEach(suppression -> Assert.assertEquals(true, suppression.getSuppressed()));
        memberUser2Suppressions.subList(2, 4)
            .forEach(suppression -> Assert.assertEquals(false, suppression.getSuppressed()));

        testUserService.unauthenticate();
        ExceptionUtils.verifyException(
            BoardForbiddenException.class, () -> userApi.postSuppression(board11id, memberUser3.getUuid()), ExceptionCode.FORBIDDEN_RESOURCE);
        testUserService.setAuthentication(memberUser3Id);
        ExceptionUtils.verifyException(
            BoardForbiddenException.class, () -> userApi.postSuppression(board11id, null), ExceptionCode.FORBIDDEN_RESOURCE);
        userApi.postSuppressions();
        List<UserNotificationSuppressionRepresentation> memberUser3Suppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.getSuppressions(), expectedBoardNames);
        Assert.assertEquals(0, memberUser3Suppressions.size());

        testUserService.setAuthentication(memberUser2Id);
        userApi.deleteSuppression(board12id);

        memberUser2Suppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.getSuppressions(), expectedBoardNames);
        Assert.assertEquals(4, memberUser2Suppressions.size());
        memberUser2Suppressions.subList(0, 1)
            .forEach(suppression -> Assert.assertEquals(true, suppression.getSuppressed()));
        memberUser2Suppressions.subList(1, 4)
            .forEach(suppression -> Assert.assertEquals(false, suppression.getSuppressed()));

        memberUser2Suppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.postSuppressions(), expectedBoardNames);
        Assert.assertEquals(4, memberUser2Suppressions.size());
        memberUser2Suppressions.forEach(suppression -> Assert.assertEquals(true, suppression.getSuppressed()));
        userApi.deleteSuppressions();

        memberUser2Suppressions =
            removeSuppressionsForAutomaticallyCreatedBoards(userApi.getSuppressions(), expectedBoardNames);
        Assert.assertEquals(4, memberUser2Suppressions.size());
        memberUser2Suppressions.forEach(suppression -> Assert.assertEquals(false, suppression.getSuppressed()));
    }

    private static List<UserNotificationSuppressionRepresentation> removeSuppressionsForAutomaticallyCreatedBoards(
        List<UserNotificationSuppressionRepresentation> suppressions, String... expectedBoardNames) {
        suppressions.removeIf(suppression -> !ArrayUtils.contains(expectedBoardNames, suppression.getResource()
            .getName()));
        return suppressions;
    }

}
