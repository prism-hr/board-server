package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.*;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.TestUserService;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class BoardApiIT extends AbstractIT {
    
    @Inject
    private DepartmentApi departmentApi;
    
    @Inject
    private BoardApi boardApi;
    
    @Inject
    private PostApi postApi;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private TestUserService testUserService;
    
    @Inject
    private TestHelper testHelper;
    
    @Test
    public void shouldCreateBoard() {
        User user = testUserService.authenticate();
        verifyPostBoard(user,
            new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setMemberCategories(ImmutableList.of("category1", "category2"))),
            "new-board", "new-department");
    }
    
    @Test
    public void shouldNotCreateDuplicateBoard() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("New Board")
            .setDescription("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        verifyPostBoard(user, boardDTO, "new-board", "new-department");
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("New Board With Long Name")
            .setDescription("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        verifyPostBoard(user, boardDTO, "new-board-with-long", "new-department");
        
        boardDTO.setName("New Board With Long Name Two");
        verifyPostBoard(user, boardDTO, "new-board-with-long-2", "new-department");
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardRs.getValue().getName()));
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, status);
            return null;
        });
    }
    
    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        Pair<BoardRepresentation, BoardRepresentation> boardRs = verifyPostTwoBoards();
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setHandle(Optional.of(boardRs.getValue().getHandle()));
            ExceptionUtil.verifyApiException(ApiException.class, () -> boardApi.updateBoard(boardRs.getKey().getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE, status);
            return null;
        });
    }
    
    @Test
    public void shouldCreateMultipleBoardsAndReturnCorrectResourceListsForUsers() {
        User user = testUserService.authenticate();
        Long departmentId = verifyPostBoard(user,
            new BoardDTO()
                .setName("Board 1")
                .setDescription("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-1", "department-1")
            .getDepartment().getId();
        
        User secondUser = testUserService.authenticate();
        verifyPostBoard(secondUser,
            new BoardDTO()
                .setName("Board 2")
                .setDescription("Purpose")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId)
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-2", "department-1", false);
        
        transactionTemplate.execute(TransactionStatus -> {
            testUserService.setAuthentication(user.getStormpathId());
            List<BoardRepresentation> boardRs = boardApi.getBoards();
            List<DepartmentRepresentation> departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            boardRs.forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND)));
            
            testUserService.setAuthentication(secondUser.getStormpathId());
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            boardRs.stream().filter(boardR -> boardR.getName().equals("Board 1"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            boardRs.stream().filter(boardR -> boardR.getName().equals("Board 2"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                    Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            
            testUserService.setAuthentication(null);
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();
            
            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());
            
            boardRs.forEach(boardR -> Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()), Matchers
                .containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            return null;
        });
    }
    
    @Test
    public void shouldCreateAndUpdateTwoBoardsWithinOneDepartment() {
        User user = testUserService.authenticate();
        Long departmentId = verifyPostBoard(user,
            new BoardDTO()
                .setName("Board 1")
                .setDescription("Purpose 1")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-1", "department-1")
            .getDepartment().getId();
        
        verifyPostBoard(user, new BoardDTO()
                .setName("Board 2")
                .setDescription("Purpose 2")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId)
                    .setName("Department 1")
                    .setMemberCategories(new ArrayList<>())),
            "board-2", "department-1");
        
        transactionTemplate.execute(status -> {
            DepartmentRepresentation departmentR = testHelper.verifyGetDepartment(departmentId);
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(departmentR.getId());
            Assert.assertEquals(2, boardRs.size());
            
            List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("Board 1", "Board 2"));
            
            departmentApi.updateDepartment(departmentR.getId(),
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("new-department-updated")));
            return null;
        });
        
        transactionTemplate.execute(status -> {
            Department department = departmentService.getDepartment(departmentId);
            Assert.assertEquals("new-department-updated", department.getHandle());
            
            int index = 1;
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(department.getId());
            for (BoardRepresentation boardR : boardRs) {
                Assert.assertEquals("new-department-updated/board-" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                index++;
            }
            
            return null;
        });
    }
    
    @Test
    public void shouldUpdateBoard() {
        User user = testUserService.authenticate();
        Long boardId = verifyPostBoard(user,
            new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(ImmutableList.of("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setMemberCategories(new ArrayList<>())),
            "new-board", "new-department")
            .getId();
        
        transactionTemplate.execute(status -> {
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO()
                .setName(Optional.of("New Board Updated"))
                .setDescription(Optional.of("Purpose Updated"))
                .setHandle(Optional.of("new-board-updated"))
                .setPostCategories(Optional.of(ImmutableList.of("c")))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PUBLIC));
            boardApi.updateBoard(boardId, boardPatchDTO);
            return null;
        });
        
        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = testHelper.verifyGetBoard(boardId);
            Assert.assertEquals("New Board Updated", boardR.getName());
            Assert.assertEquals("Purpose Updated", boardR.getDescription());
            Assert.assertEquals("new-board-updated", boardR.getHandle());
            Assert.assertThat(boardR.getPostCategories(), Matchers.contains("c"));
            Assert.assertEquals(PostVisibility.PUBLIC, boardR.getDefaultPostVisibility());
            return null;
        });
    }
    
    @Test
    public void shouldCreateAndListBoards() {
        User user = testUserService.authenticate();
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                verifyPostBoard(user,
                    new BoardDTO()
                        .setName("Board " + i + " " + j)
                        .setDescription("Purpose")
                        .setPostCategories(ImmutableList.of("category3", "category4"))
                        .setDepartment(new DepartmentDTO()
                            .setName("Department " + i)
                            .setMemberCategories(ImmutableList.of("category1", "category2"))),
                    "board-" + i + "-" + j, "department-" + i);
            }
        }
        
        transactionTemplate.execute(transactionStatus -> {
            List<DepartmentRepresentation> departmentRepresentations = departmentApi.getDepartments();
            Assert.assertEquals(Arrays.asList("Department 1", "Department 2", "Department 3"),
                departmentRepresentations.stream().map(DepartmentRepresentation::getName).collect(Collectors.toList()));
            
            List<BoardRepresentation> boardRepresentations = boardApi.getBoards();
            Assert.assertEquals(Arrays.asList("Board 1 1", "Board 1 2", "Board 1 3", "Board 2 1", "Board 2 2", "Board 2 3", "Board 3 1", "Board 3 2", "Board 3 3"),
                boardRepresentations.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
            return null;
        });
    }
    
    @Test
    public void shouldNotBeAbleToCorruptBoardByPatching() {
        testUserService.authenticate();
        Long boardId = transactionTemplate.execute(status -> {
            BoardRepresentation boardRepresentation = boardApi.postBoard(
                new BoardDTO()
                    .setName("New Board")
                    .setDescription("Purpose")
                    .setPostCategories(ImmutableList.of("category3", "category4"))
                    .setDepartment(new DepartmentDTO()
                        .setName("New Department")
                        .setMemberCategories(ImmutableList.of("category1", "category2"))));
            return boardRepresentation.getId();
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    boardApi.updateBoard(boardId, new BoardPatchDTO().setName(Optional.empty())),
                ExceptionCode.MISSING_BOARD_NAME, null);
            status.setRollbackOnly();
            return null;
        });
        
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiException.class, () ->
                    boardApi.updateBoard(boardId, new BoardPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
                ExceptionCode.MISSING_BOARD_HANDLE, null);
            status.setRollbackOnly();
            return null;
        });
    }
    
    @Test
    public void shouldAuditBoardAndMakeChangesPrivatelyVisible() {
        User boardUser = testUserService.authenticate();
        Long boardId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("New Board")
                .setDescription("Purpose")
                .setPostCategories(Arrays.asList("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("New Department")
                    .setDocumentLogo(new DocumentDTO().setCloudinaryId("c").setCloudinaryUrl("u").setFileName("f"))
                    .setMemberCategories(ImmutableList.of("a", "b")));
            
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            return boardR.getId();
        });
        
        User postUser = testUserService.authenticate();
        transactionTemplate.execute(transactionStatus -> postApi.postPost(boardId, TestHelper.samplePost()));
        
        // Test that we do not audit viewing
        transactionTemplate.execute(status -> {
            boardApi.getBoard(boardId);
            return null;
        });
        
        testUserService.setAuthentication(boardUser.getStormpathId());
        transactionTemplate.execute(status -> {
            boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setName(Optional.of("New Board 2"))
                    .setDescription(Optional.of("Purpose 2"))
                    .setHandle(Optional.of("new-board-2"))
                    .setPostCategories(Optional.of(Arrays.asList("c", "d")))
                    .setDefaultPostVisibility(Optional.of(PostVisibility.PRIVATE)));
            return null;
        });
        
        transactionTemplate.execute(status -> {
            boardApi.updateBoard(boardId,
                new BoardPatchDTO()
                    .setName(Optional.of("New Board 3"))
                    .setDescription(Optional.of("Purpose 3"))
                    .setHandle(Optional.of("new-board-3"))
                    .setPostCategories(Optional.empty()));
            return null;
        });
        
        BoardRepresentation boardR = transactionTemplate.execute(status -> boardApi.getBoard(boardId));
        List<ResourceOperationRepresentation> resourceOperationRs = transactionTemplate.execute(status -> boardApi.getBoardOperations(boardId));
        Assert.assertEquals(3, resourceOperationRs.size());
        
        ResourceOperationRepresentation resourceOperationR1 = resourceOperationRs.get(0);
        Assert.assertEquals(Action.EDIT, resourceOperationR1.getAction());
        testHelper.verifyUser(boardUser, resourceOperationR1.getUser());
        
        ResourceChangeListRepresentation resourceChangeListR1 = resourceOperationR1.getChangeList();
        Assert.assertEquals(4, resourceChangeListR1.size());
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("New Board 2").setNewValue("New Board 3"),
            resourceChangeListR1.get("name"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("Purpose 2").setNewValue("Purpose 3"),
            resourceChangeListR1.get("description"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("new-board-2").setNewValue("new-board-3"),
            resourceChangeListR1.get("handle"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue(Arrays.asList("c", "d")).setNewValue(null),
            resourceChangeListR1.get("postCategories"));
        
        ResourceOperationRepresentation resourceOperationR2 = resourceOperationRs.get(1);
        Assert.assertEquals(Action.EDIT, resourceOperationR2.getAction());
        testHelper.verifyUser(boardUser, resourceOperationR2.getUser());
        
        ResourceChangeListRepresentation resourceChangeListR2 = resourceOperationR2.getChangeList();
        Assert.assertEquals(5, resourceChangeListR2.size());
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("New Board").setNewValue("New Board 2"),
            resourceChangeListR2.get("name"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("Purpose").setNewValue("Purpose 2"),
            resourceChangeListR2.get("description"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("new-board").setNewValue("new-board-2"),
            resourceChangeListR2.get("handle"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue(Arrays.asList("a", "b")).setNewValue(Arrays.asList("c", "d")),
            resourceChangeListR2.get("postCategories"));
        Assert.assertEquals(new ResourceChangeListRepresentation.ResourceChangeRepresentation().setOldValue("PART_PRIVATE").setNewValue("PRIVATE"),
            resourceChangeListR2.get("defaultPostVisibility"));
        
        ResourceOperationRepresentation resourceOperationR3 = resourceOperationRs.get(2);
        Assert.assertEquals(Action.EXTEND, resourceOperationR3.getAction());
        testHelper.verifyUser(boardUser, resourceOperationR3.getUser());
        Assert.assertNull(resourceOperationR3.getChangeList());
        
        Assert.assertEquals(resourceOperationR1.getCreatedTimestamp(), boardR.getUpdatedTimestamp());
        Assert.assertEquals(resourceOperationR3.getCreatedTimestamp(), boardR.getCreatedTimestamp());
        
        // Test that post administrator cannot view audit trail
        testUserService.setAuthentication(postUser.getStormpathId());
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> boardApi.getBoardOperations(boardId), ExceptionCode.FORBIDDEN_ACTION, status);
            return null;
        });
        
        // Test that a member of the public cannot view audit trail
        testUserService.unauthenticate();
        transactionTemplate.execute(status -> {
            ExceptionUtil.verifyApiException(ApiForbiddenException.class, () -> boardApi.getBoardOperations(boardId), ExceptionCode.UNAUTHENTICATED_USER, status);
            return null;
        });
    }
    
    private Pair<BoardRepresentation, BoardRepresentation> verifyPostTwoBoards() {
        User user = testUserService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("New Board")
            .setDescription("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        BoardRepresentation boardR = verifyPostBoard(user, boardDTO, "new-board", "new-department");
    
        BoardDTO otherBoardDTO = new BoardDTO()
            .setName("Other New Board")
            .setDescription("Purpose")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("New Department")
                .setMemberCategories(ImmutableList.of("category1", "category2")));
        BoardRepresentation otherBoardR = verifyPostBoard(user, otherBoardDTO, "other-new-board", "new-department");
        return new Pair<>(boardR, otherBoardR);
    }
    
    private BoardRepresentation verifyPostBoard(User user, BoardDTO boardDTO, String expectedHandle, String expectedDepartmentHandle) {
        return verifyPostBoard(user, boardDTO, expectedHandle, expectedDepartmentHandle, true);
    }
    
    private BoardRepresentation verifyPostBoard(User user, BoardDTO boardDTO, String expectedHandle, String expectedDepartmentHandle, boolean expectDepartmentAdministrator) {
        return transactionTemplate.execute(status -> {
            BoardRepresentation postedBoardR = boardApi.postBoard(boardDTO);
            testHelper.verifyBoard(user, boardDTO, postedBoardR, expectDepartmentAdministrator);
            Assert.assertEquals(expectedHandle, postedBoardR.getHandle());
            Assert.assertEquals(expectedDepartmentHandle, postedBoardR.getDepartment().getHandle());
            return postedBoardR;
        });
    }
    
}
