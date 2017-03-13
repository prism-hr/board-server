package hr.prism.board;

import com.google.common.collect.ImmutableList;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.UserTestService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.ArrayList;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ApiTest {

    @Inject
    private Api api;

    @Inject
    private UserTestService userTestService;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    @Before
    public void setUp() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Test
    public void shouldCreateBoard() {
        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticate();
            DepartmentDTO departmentDTO = new DepartmentDTO().setName("shouldCreateBoard Department").setMemberCategories(ImmutableList.of("category1", "category2"));
            BoardSettingsDTO settingsDTO = new BoardSettingsDTO().setPostCategories(ImmutableList.of("category3", "category4"));
            BoardDTO boardDTO = new BoardDTO().setName("shouldCreateBoard Board").setPurpose("Purpose").setDepartment(departmentDTO)
                .setSettings(settingsDTO);
            BoardRepresentation boardR = api.postBoard(boardDTO);
            DepartmentRepresentation departmentR = boardR.getDepartment();
            Assert.assertEquals(boardDTO.getName(), boardR.getName());
            Assert.assertEquals(boardDTO.getPurpose(), boardR.getPurpose());
            Assert.assertThat(boardR.getPostCategories(), Matchers.containsInAnyOrder("category3", "category4"));

            Assert.assertEquals(departmentDTO.getName(), departmentR.getName());
            Assert.assertThat(departmentR.getMemberCategories(), Matchers.containsInAnyOrder("category1", "category2"));

            return null;
        });
    }

    @Test
    public void shouldCreateTwoBoards() {
        Long createdDepartmentId = transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticate();
            DepartmentDTO departmentDTO = new DepartmentDTO().setName("Department 1");
            BoardSettingsDTO settingsDTO = new BoardSettingsDTO().setPostCategories(new ArrayList<>());
            BoardDTO boardDTO = new BoardDTO().setName("Board 1").setPurpose("Purpose 1").setDepartment(departmentDTO)
                .setSettings(settingsDTO);
            BoardRepresentation boardRepresentation = api.postBoard(boardDTO);
            Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
            Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());

            Long departmentId = boardRepresentation.getDepartment().getId();
            departmentDTO = new DepartmentDTO().setId(departmentId).setName("Department 2");
            boardDTO = new BoardDTO().setName("Board 2").setPurpose("Purpose 2").setDepartment(departmentDTO)
                .setSettings(settingsDTO);
            boardRepresentation = api.postBoard(boardDTO);
            Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
            Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());
            return departmentId;
        });

        transactionTemplate.execute(transactionStatus -> {
            DepartmentRepresentation department = api.getDepartment(createdDepartmentId);
            Assert.assertEquals(2, department.getBoards().size());
            return null;
        });
    }

    @Test
    public void shouldUpdateDepartmentWithNoLogo() {
        userTestService.authenticate();
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("Department 3");
        BoardSettingsDTO settingsDTO = new BoardSettingsDTO().setPostCategories(new ArrayList<>());
        BoardDTO boardDTO = new BoardDTO().setName("Board 3").setPurpose("Purpose 3").setDepartment(departmentDTO)
            .setSettings(settingsDTO);
        BoardRepresentation boardRepresentation = api.postBoard(boardDTO);
        Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());

        departmentDTO.setId(boardRepresentation.getDepartment().getId());
        api.updateDepartment(departmentDTO);
        Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());
    }

}
