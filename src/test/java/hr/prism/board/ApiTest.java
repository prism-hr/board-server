package hr.prism.board;

import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.UserTestService;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.List;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ApiTest {

    @Inject
    private Api api;

    @Inject
    private Flyway flyway;

    @Inject
    private UserTestService userTestService;

    @PostConstruct
    public void setup() {
        userTestService.createUser("admin@prism.hr");
    }

    @Test
    public void shouldCreateBoards() {
        userTestService.authenticateAs("admin@prism.hr");
        DepartmentDTO departmentDTO = new DepartmentDTO().withName("Department 1");
        BoardDTO boardDTO = new BoardDTO().withName("Board 1").withPurpose("Purpose 1").withDepartment(departmentDTO);
        BoardRepresentation boardRepresentation = api.postBoard(boardDTO);

        Long departmentId = boardRepresentation.getDepartment().getId();
        departmentDTO = new DepartmentDTO().withId(departmentId).withName("Elsewhat");
        boardDTO = new BoardDTO().withName("Board 2").withPurpose("Purpose 2").withDepartment(departmentDTO);
        api.postBoard(boardDTO);

        List<DepartmentRepresentation> departments = api.getBoardsGroupedByDepartment();
        Assert.assertEquals(1, departments.size());
    }

    @PreDestroy
    private void preDestroy() {
        flyway.clean();
    }

}
