package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.UserNotificationSuppressionRepresentation;
import hr.prism.board.representation.UserRepresentation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@TestContext
@RunWith(SpringRunner.class)
public class UserApiIT extends AbstractIT {

    @Inject
    private UserApi userApi;

    @Test
    public void shouldCreateAndUpdateUser() {
        testUserService.authenticate();

        DocumentDTO imageDTO = new DocumentDTO().setCloudinaryId("userImage").setCloudinaryUrl("userImage").setFileName("userImage");
        UserPatchDTO userDTO = new UserPatchDTO()
            .setGivenName(Optional.of("first"))
            .setSurname(Optional.of("second"))
            .setDocumentImage(Optional.of(imageDTO));
        userApi.updateUser(userDTO);

        UserRepresentation user = userApi.getCurrentUser();
        Assert.assertEquals("first", user.getGivenName());
        Assert.assertEquals("second", user.getSurname());
        verifyDocument(imageDTO, user.getDocumentImage());
    }

    @Test
    public void shouldCreateAndUpdateNotificationSuppressions() {
        User adminUser = testUserService.authenticate();
        Long department1id = transactionTemplate.execute(status ->
            departmentApi.postDepartment((DepartmentDTO) new DepartmentDTO().setName("department1"))).getId();

        Long board11id = transactionTemplate.execute(status ->
            boardApi.postBoard(((BoardDTO) new BoardDTO().setName("board11")).setDepartment(new DepartmentDTO().setId(department1id)))).getId();

        Long board12id = transactionTemplate.execute(status ->
            boardApi.postBoard(((BoardDTO) new BoardDTO().setName("board12")).setDepartment(new DepartmentDTO().setId(department1id)))).getId();

        Long board13id = transactionTemplate.execute(status ->
            boardApi.postBoard(((BoardDTO) new BoardDTO().setName("board13")).setDepartment(new DepartmentDTO().setId(department1id)))).getId();

        Long department2id = transactionTemplate.execute(status ->
            departmentApi.postDepartment((DepartmentDTO) new DepartmentDTO().setName("department2"))).getId();

        Long board21id = transactionTemplate.execute(status ->
            boardApi.postBoard(((BoardDTO) new BoardDTO().setName("board21")).setDepartment(new DepartmentDTO().setId(department2id)))).getId();

        Long board22id = transactionTemplate.execute(status ->
            boardApi.postBoard(((BoardDTO) new BoardDTO().setName("board22")).setDepartment(new DepartmentDTO().setId(department2id)))).getId();

        Long board23id = transactionTemplate.execute(status ->
            boardApi.postBoard(((BoardDTO) new BoardDTO().setName("board23")).setDepartment(new DepartmentDTO().setId(department2id)))).getId();

        User memberUser1 = testUserService.authenticate();
        User memberUser2 = testUserService.authenticate();
        User memberUser3 = testUserService.authenticate();

        testUserService.setAuthentication(adminUser.getId());
        for (String memberUserEmail : new String[]{memberUser1.getEmail(), memberUser2.getEmail()}) {
            transactionTemplate.execute(status ->
                resourceApi.createResourceUser(Scope.BOARD, board11id, new ResourceUserDTO().setUser(
                    new UserDTO().setEmail(memberUserEmail)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.ADMINISTRATOR)))));

            transactionTemplate.execute(status ->
                resourceApi.createResourceUser(Scope.BOARD, board12id, new ResourceUserDTO().setUser(
                    new UserDTO().setEmail(memberUserEmail)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.AUTHOR)))));

            transactionTemplate.execute(status ->
                resourceApi.createResourceUser(Scope.BOARD, board13id, new ResourceUserDTO().setUser(
                    new UserDTO().setEmail(memberUserEmail)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));

            transactionTemplate.execute(status ->
                resourceApi.createResourceUser(Scope.BOARD, board21id, new ResourceUserDTO().setUser(
                    new UserDTO().setEmail(memberUserEmail)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.ADMINISTRATOR)))));

            transactionTemplate.execute(status ->
                resourceApi.createResourceUser(Scope.BOARD, board22id, new ResourceUserDTO().setUser(
                    new UserDTO().setEmail(memberUserEmail)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.AUTHOR)))));

            transactionTemplate.execute(status ->
                resourceApi.createResourceUser(Scope.BOARD, board23id, new ResourceUserDTO().setUser(
                    new UserDTO().setEmail(memberUserEmail)).setRoles(Collections.singleton(new UserRoleDTO().setRole(Role.MEMBER)))));
        }

        Long memberUser1Id = memberUser1.getId();
        Long memberUser2Id = memberUser2.getId();
        Long memberUser3Id = memberUser3.getId();

        testUserService.setAuthentication(memberUser1Id);
        transactionTemplate.execute(status -> userApi.postSuppressions());
        List<UserNotificationSuppressionRepresentation> memberUser1Suppressions = transactionTemplate.execute(status -> userApi.getSuppressions());
        Assert.assertEquals(6, memberUser1Suppressions.size());
        memberUser1Suppressions.forEach(suppression -> Assert.assertEquals(true, suppression.getSuppressed()));

        testUserService.unauthenticate();
        transactionTemplate.execute(status -> userApi.postSuppression(board11id, memberUser2.getUuid()));
        testUserService.setAuthentication(memberUser2Id);
        transactionTemplate.execute(status -> userApi.postSuppression(board12id, null));
        List<UserNotificationSuppressionRepresentation> memberUser2Suppressions = transactionTemplate.execute(status -> userApi.getSuppressions());
        Assert.assertEquals(6, memberUser2Suppressions.size());
        memberUser2Suppressions.subList(0, 2).forEach(suppression -> Assert.assertEquals(true, suppression.getSuppressed()));
        memberUser2Suppressions.subList(2, 6).forEach(suppression -> Assert.assertEquals(false, suppression.getSuppressed()));

        testUserService.unauthenticate();
        transactionTemplate.execute(status -> ExceptionUtils.verifyException(
            BoardForbiddenException.class, () -> userApi.postSuppression(board11id, memberUser3.getUuid()), ExceptionCode.FORBIDDEN_RESOURCE, status));
        testUserService.setAuthentication(memberUser3Id);
        transactionTemplate.execute(status -> ExceptionUtils.verifyException(
            BoardForbiddenException.class, () -> userApi.postSuppression(board11id, null), ExceptionCode.FORBIDDEN_RESOURCE, status));
        transactionTemplate.execute(status -> userApi.postSuppressions());
        List<UserNotificationSuppressionRepresentation> memberUser3Suppressions = transactionTemplate.execute(status -> userApi.getSuppressions());
        Assert.assertEquals(0, memberUser3Suppressions.size());
    }

}
