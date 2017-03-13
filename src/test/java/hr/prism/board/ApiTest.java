package hr.prism.board;

import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.UserTestService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

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

    @Test
    public void shouldCreateBoards() {
        userTestService.authenticateAs("admin1@prism.hr");
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("Department 1");
        BoardDTO boardDTO = new BoardDTO().setName("Board 1").setPurpose("Purpose 1").setDepartment(departmentDTO);
        BoardRepresentation boardRepresentation = api.postBoard(boardDTO);
        Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());

        Long departmentId = boardRepresentation.getDepartment().getId();
        departmentDTO = new DepartmentDTO().setId(departmentId).setName("Department 2");
        boardDTO = new BoardDTO().setName("Board 2").setPurpose("Purpose 2").setDepartment(departmentDTO);
        boardRepresentation = api.postBoard(boardDTO);
        Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());

        Assert.assertEquals(1, api.getDepartments().size());
        Assert.assertEquals(2, api.getBoards().size());
    }

    @Test
    public void shouldUpdateDepartmentWithNoLogo() {
        userTestService.authenticateAs("admin2@prism.hr");
        DepartmentDTO departmentDTO = new DepartmentDTO().setName("Department 3");
        BoardDTO boardDTO = new BoardDTO().setName("Board 3").setPurpose("Purpose 3").setDepartment(departmentDTO);
        BoardRepresentation boardRepresentation = api.postBoard(boardDTO);
        Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());

        departmentDTO.setId(boardRepresentation.getDepartment().getId());
        api.updateDepartment(departmentDTO);
        Assert.assertEquals(boardDTO.getName(), boardRepresentation.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardRepresentation.getPurpose());
    }

}
