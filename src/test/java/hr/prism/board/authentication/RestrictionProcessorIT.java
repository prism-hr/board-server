package hr.prism.board.authentication;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.api.AbstractIT;
import hr.prism.board.api.DepartmentBoardApi;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.UserTestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class RestrictionProcessorIT extends AbstractIT {

    @Inject
    private MockApi mockApi;

    @Inject
    private DepartmentBoardApi departmentBoardApi;

    @Inject
    private UserTestService userTestService;

    @Test
    public void shouldForbidUnauthenticatedUser() {
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(null, null));
        SecurityContextHolder.setContext(securityContext);
        ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getBoards(), "User not authenticated");
    }

    @Test
    public void shouldForbidUserWithNoResourceRoles() {
        userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            departmentBoardApi.postBoard(
                new BoardDTO()
                    .setName("Board")
                    .setPurpose("Purpose")
                    .setDepartment(new DepartmentDTO()
                        .setName("Department")
                        .setHandle("department")
                        .setMemberCategories(ImmutableList.of("member1", "member2")))
                    .setSettings(new BoardSettingsDTO()
                        .setHandle("board")
                        .setPostCategories(ImmutableList.of("post3", "post4"))));
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            mockApi.getBoards();
            mockApi.getDepartments();

            userTestService.authenticate();
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getBoards(), "User has no board roles");
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getDepartments(), "User has no department roles");

            transactionStatus.setRollbackOnly();
            return null;
        });
    }

    @Test
    public void shouldForbidInvocationWithInvalidArgument() {
        userTestService.authenticate();
        ExceptionUtil.verifyIllegalStateException(() -> mockApi.invalidArgument(null), "Argument declaration invalid");
    }

    @Test
    public void shouldForbidRequestWithNoResource() {
        userTestService.authenticate();
        ExceptionUtil.verifyIllegalStateException(() -> mockApi.getBoard(0L), "Could not find board");
        ExceptionUtil.verifyIllegalStateException(() -> mockApi.getDepartment(0L), "Could not find department");
        ExceptionUtil.verifyIllegalStateException(() -> mockApi.getBoard("none"), "Could not find board");
        ExceptionUtil.verifyIllegalStateException(() -> mockApi.getDepartment("none"), "Could not find department");
    }

    @Test
    public void shouldForbidUserWithNoResourceRole() {
        userTestService.authenticate();
        BoardRepresentation boardRepresentation = transactionTemplate.execute(transactionStatus ->
            departmentBoardApi.postBoard(
                new BoardDTO()
                    .setName("Board")
                    .setPurpose("Purpose")
                    .setDepartment(new DepartmentDTO()
                        .setName("Department")
                        .setHandle("department")
                        .setMemberCategories(ImmutableList.of("member1", "member2")))
                    .setSettings(new BoardSettingsDTO()
                        .setHandle("board")
                        .setPostCategories(ImmutableList.of("post3", "post4")))));

        Long boardId = boardRepresentation.getId();
        Long departmentId = boardRepresentation.getDepartment().getId();

        String departmentHandle = boardRepresentation.getDepartment().getHandle();
        String boardHandle = departmentHandle + "/" + boardRepresentation.getHandle();

        transactionTemplate.execute(transactionStatus -> {
            mockApi.getBoard(boardId);
            mockApi.getDepartment(departmentId);

            mockApi.getBoard(boardHandle);
            mockApi.getDepartment(departmentHandle);

            User otherUser = userTestService.authenticate();
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getBoard(boardId),
                "User " + otherUser.toString() + " does not have role(s): administrator for: board " + boardId);
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getDepartment(departmentId),
                "User " + otherUser.toString() + " does not have role(s): administrator for: department " + departmentId);

            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getBoard(boardHandle),
                "User " + otherUser.toString() + " does not have role(s): administrator for: board " + boardId);
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getDepartment(departmentHandle),
                "User " + otherUser.toString() + " does not have role(s): administrator for: department " + departmentId);

            transactionStatus.setRollbackOnly();
            return null;
        });
    }

    @Test
    public void shouldForbidUserWithDifferentResourceRole() {
        User user = userTestService.authenticate();
        BoardRepresentation boardRepresentation = transactionTemplate.execute(transactionStatus ->
            departmentBoardApi.postBoard(
                new BoardDTO()
                    .setName("Board")
                    .setPurpose("Purpose")
                    .setDepartment(new DepartmentDTO()
                        .setName("Department")
                        .setHandle("department")
                        .setMemberCategories(ImmutableList.of("member1", "member2")))
                    .setSettings(new BoardSettingsDTO()
                        .setHandle("board")
                        .setPostCategories(ImmutableList.of("post3", "post4")))));

        User otherUser = userTestService.authenticate();
        BoardRepresentation otherBoardRepresentation = transactionTemplate.execute(transactionStatus ->
            departmentBoardApi.postBoard(
                new BoardDTO()
                    .setName("Other Board")
                    .setPurpose("Other Purpose")
                    .setDepartment(new DepartmentDTO()
                        .setId(boardRepresentation.getDepartment().getId()))
                    .setSettings(new BoardSettingsDTO()
                        .setHandle("other-board")
                        .setPostCategories(ImmutableList.of("post3", "post4")))));

        Long boardId = boardRepresentation.getId();
        Long departmentId = boardRepresentation.getDepartment().getId();

        String departmentHandle = boardRepresentation.getDepartment().getHandle();
        String boardHandle = departmentHandle + "/" + boardRepresentation.getHandle();

        Long otherBoardId = otherBoardRepresentation.getId();
        String otherBoardHandle = departmentHandle + "/" + otherBoardRepresentation.getHandle();

        transactionTemplate.execute(transactionStatus -> {
            userTestService.setAuthentication(user.getStormpathId());
            mockApi.getBoard(boardId);
            mockApi.getDepartment(departmentId);

            mockApi.getBoard(boardHandle);
            mockApi.getDepartment(departmentHandle);

            userTestService.setAuthentication(otherUser.getStormpathId());
            mockApi.getBoard(otherBoardId);
            mockApi.getBoard(otherBoardHandle);

            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getBoard(boardId),
                "User " + otherUser.toString() + " does not have role(s): administrator for: board " + boardId);
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getDepartment(departmentId),
                "User " + otherUser.toString() + " does not have role(s): administrator for: department " + departmentId);

            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getBoard(boardHandle),
                "User " + otherUser.toString() + " does not have role(s): administrator for: board " + boardId);
            ExceptionUtil.verifyApiForbiddenException(() -> mockApi.getDepartment(departmentHandle),
                "User " + otherUser.toString() + " does not have role(s): administrator for: department " + departmentId);

            transactionStatus.setRollbackOnly();
            return null;
        });
    }

    @Test
    public void shouldForbidInvocationWithInvalidSignature() {
        userTestService.authenticate();
        ExceptionUtil.verifyIllegalStateException(() -> mockApi.invalidSignature(null), "Method declaration invalid");
    }

}
