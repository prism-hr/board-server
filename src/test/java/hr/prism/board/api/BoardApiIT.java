package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardPatchDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.service.UserTestService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private BoardService boardService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserTestService userTestService;

    @Inject
    private DepartmentBoardHelper departmentBoardHelper;

    @Test
    public void shouldCreateBoardWithAllPossibleFieldsSet() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldCreateBoard Board")
                .setPurpose("Purpose")
                .setHandle("scb")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldCreateBoard Department")
                    .setHandle("scb")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));

            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoard() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoard Board")
                .setPurpose("Purpose")
                .setHandle("sncdb")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoard Department")
                    .setHandle("sncdb")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));

            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);

            ExceptionUtil.verifyApiException(() -> boardApi.postBoard(boardDTO), ExceptionCode.DUPLICATE_BOARD, transactionStatus);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardHandle() {
        User user = userTestService.authenticate();
        BoardDTO boardDTO = new BoardDTO()
            .setName("shouldNotCreateDuplicateBoardHandle Board 1")
            .setPurpose("Purpose")
            .setHandle("sncdbh")
            .setPostCategories(ImmutableList.of("category3", "category4"))
            .setDepartment(new DepartmentDTO()
                .setName("shouldNotCreateDuplicateBoardHandle Department")
                .setHandle("sncdbh")
                .setMemberCategories(ImmutableList.of("category1", "category2")));

        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            ExceptionUtil.verifyApiException(() -> boardApi.postBoard(boardDTO.setName("shouldNotCreateDuplicateBoardHandle Board 2")),
                ExceptionCode.DUPLICATE_BOARD_HANDLE, transactionStatus);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardByUpdating Board 1")
                .setPurpose("Purpose")
                .setHandle("sncdbbu1")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardByUpdating Department")
                    .setHandle("sncdbbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);

            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardByUpdating Board 2")
                .setPurpose("Purpose")
                .setHandle("sncdbbu2")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardByUpdating Department")
                    .setHandle("sncdbbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);

            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setName(Optional.of(boardDTO2.getName()));
            ExceptionUtil.verifyApiException(() -> boardApi.updateBoard(boardR1.getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD, transactionStatus);
            return null;
        });
    }

    @Test
    public void shouldNotCreateDuplicateBoardHandleByUpdating() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO1 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardHandleByUpdating Board 1")
                .setPurpose("Purpose")
                .setHandle("sncdbhbu1")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardHandleByUpdating Department")
                    .setHandle("sncdbhbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            BoardRepresentation boardR1 = boardApi.postBoard(boardDTO1);
            departmentBoardHelper.verifyBoard(user, boardDTO1, boardR1, true);

            BoardDTO boardDTO2 = new BoardDTO()
                .setName("shouldNotCreateDuplicateBoardHandleByUpdating Board 2")
                .setPurpose("Purpose")
                .setHandle("sncdbhbu2")
                .setPostCategories(ImmutableList.of("category3", "category4"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldNotCreateDuplicateBoardHandleByUpdating Department")
                    .setHandle("sncdbhbu1")
                    .setMemberCategories(ImmutableList.of("category1", "category2")));
            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);

            BoardPatchDTO boardPatchDTO = new BoardPatchDTO();
            boardPatchDTO.setHandle(Optional.of(boardDTO2.getHandle()));
            ExceptionUtil.verifyApiException(() -> boardApi.updateBoard(boardR1.getId(), boardPatchDTO), ExceptionCode.DUPLICATE_BOARD_HANDLE, transactionStatus);
            return null;
        });
    }

    @Test
    public void shouldCreateMultipleBoardsAndReturnCorrectResourceListsForUsers() {
        User user = userTestService.authenticate();
        User otherUser = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            userTestService.setAuthentication(user.getStormpathId());
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose 1")
                .setHandle("Handle1")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setHandle("Handle1")
                    .setMemberCategories(new ArrayList<>()));

            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);

            Long departmentId = boardR.getDepartment().getId();
            userTestService.setAuthentication(otherUser.getStormpathId());
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose 2")
                .setHandle("Handle2")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId));

            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());

            departmentBoardHelper.verifyBoard(otherUser, boardDTO2, boardR2, false);
            return null;
        });

        transactionTemplate.execute(TransactionStatus -> {
            userTestService.setAuthentication(user.getStormpathId());
            List<BoardRepresentation> boardRs = boardApi.getBoards();
            List<DepartmentRepresentation> departmentRs = departmentApi.getDepartments();

            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());

            boardRs.forEach(boardR -> Assert.assertThat(boardR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND)));

            userTestService.setAuthentication(otherUser.getStormpathId());
            boardRs = boardApi.getBoards();
            departmentRs = departmentApi.getDepartments();

            Assert.assertEquals(2, boardRs.size());
            Assert.assertEquals(1, departmentRs.size());

            boardRs.stream().filter(boardR -> boardR.getName().equals("Board 1"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));
            boardRs.stream().filter(boardR -> boardR.getName().equals("Board 2"))
                .forEach(boardR -> Assert.assertThat(boardR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND)));
            departmentRs.forEach(departmentR -> Assert.assertThat(departmentR.getActions(), Matchers.containsInAnyOrder(Action.VIEW, Action.EXTEND)));

            return null;
        });
    }

    @Test
    public void shouldCreateAndUpdateTwoBoardsWithinOneDepartment() {
        User user = userTestService.authenticate();
        Long createdDepartmentId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board 1")
                .setPurpose("Purpose 1")
                .setHandle("Handle1")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setName("Department 1")
                    .setHandle("Handle1")
                    .setMemberCategories(new ArrayList<>()));

            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);

            Long departmentId = boardR.getDepartment().getId();
            BoardDTO boardDTO2 = new BoardDTO()
                .setName("Board 2")
                .setPurpose("Purpose 2")
                .setHandle("Handle2")
                .setPostCategories(new ArrayList<>())
                .setDepartment(new DepartmentDTO()
                    .setId(departmentId));

            BoardRepresentation boardR2 = boardApi.postBoard(boardDTO2);
            boardDTO2.getDepartment()
                .setName("Department 1")
                .setHandle("Handle1")
                .setMemberCategories(new ArrayList<>());

            departmentBoardHelper.verifyBoard(user, boardDTO2, boardR2, true);
            return boardR.getDepartment().getId();
        });

        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation departmentR = departmentBoardHelper.verifyGetDepartment(createdDepartmentId);
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(departmentR.getId());
            Assert.assertEquals(2, boardRs.size());

            List<String> boardNames = boardRs.stream().map(BoardRepresentation::getName).collect(Collectors.toList());
            Assert.assertThat(boardNames, Matchers.containsInAnyOrder("Board 1", "Board 2"));

            departmentApi.updateDepartment(departmentR.getId(),
                new DepartmentPatchDTO()
                    .setHandle(Optional.of("Handle2")));
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            Department department = departmentService.getDepartment(createdDepartmentId);
            Assert.assertEquals("Handle2", department.getHandle());

            int index = 1;
            List<BoardRepresentation> boardRs = boardApi.getBoardsByDepartment(department.getId());
            for (BoardRepresentation boardR : boardRs) {
                Assert.assertEquals("Handle2/Handle" + index, boardR.getDepartment().getHandle() + "/" + boardR.getHandle());
                index++;
            }

            return null;
        });
    }

    @Test
    public void shouldUpdateBoard() {
        User user = userTestService.authenticate();
        Long boardId = transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("shouldUpdateBoardSettings Board")
                .setPurpose("Purpose")
                .setHandle("sub")
                .setPostCategories(ImmutableList.of("a", "b"))
                .setDepartment(new DepartmentDTO()
                    .setName("shouldUpdateBoardSettings Department")
                    .setHandle("sub")
                    .setMemberCategories(new ArrayList<>()));
            BoardRepresentation boardR = boardApi.postBoard(boardDTO);
            departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
    
            BoardPatchDTO boardPatchDTO = new BoardPatchDTO()
                .setName(Optional.of("sub newName"))
                .setPurpose(Optional.of("Purpose2"))
                .setHandle(Optional.of("sub2"))
                .setPostCategories(Optional.of(ImmutableList.of("c")))
                .setDefaultPostVisibility(Optional.of(PostVisibility.PUBLIC));
            boardApi.updateBoard(boardR.getId(), boardPatchDTO);
            return boardR.getId();
        });

        transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = departmentBoardHelper.verifyGetBoard(boardId);

            Assert.assertEquals("sub newName", boardR.getName());
            Assert.assertEquals("Purpose2", boardR.getPurpose());
            Assert.assertEquals("sub2", boardR.getHandle());
            Assert.assertThat(boardR.getPostCategories(), Matchers.contains("c"));
            Assert.assertEquals(PostVisibility.PUBLIC, boardR.getDefaultPostVisibility());
            return null;
        });
    }
    
    @Test
    public void shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser() {
        User user = userTestService.authenticate();
        transactionTemplate.execute(transactionStatus -> {
            for (int i = 1; i < 4; i++) {
                String departmentName = "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser " + i;
                String departmentHandle = "sgdab" + i;
                for (int j = 1; j < 4; j++) {
                    String boardSuffix = i + " " + j;
                    BoardDTO boardDTO = new BoardDTO()
                        .setName("shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser " + boardSuffix)
                        .setPurpose("Purpose")
                        .setHandle("sgdab" + boardSuffix)
                        .setPostCategories(ImmutableList.of("category3", "category4"))
                        .setDepartment(new DepartmentDTO()
                            .setName(departmentName)
                            .setHandle(departmentHandle)
                            .setMemberCategories(ImmutableList.of("category1", "category2")));
                    BoardRepresentation boardR = boardApi.postBoard(boardDTO);
                    departmentBoardHelper.verifyBoard(user, boardDTO, boardR, true);
                }
            }

            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            List<DepartmentRepresentation> departmentRepresentations = departmentApi.getDepartments();
            Assert.assertEquals(Arrays.asList(
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3"),
                departmentRepresentations.stream().map(DepartmentRepresentation::getName).collect(Collectors.toList()));

            List<BoardRepresentation> boardRepresentations = boardApi.getBoards();
            Assert.assertEquals(Arrays.asList(
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1 1", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1 2",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 1 3", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2 1",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2 2", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 2 3",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3 1", "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3 2",
                "shouldCreateMultipleBoardsAndGetCorrectResourceListsForUser 3 3"),
                boardRepresentations.stream().map(BoardRepresentation::getName).collect(Collectors.toList()));
            return null;
        });
    }

}
