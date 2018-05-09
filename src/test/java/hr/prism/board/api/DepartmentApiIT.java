package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.value.ResourceFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static hr.prism.board.enums.State.ACCEPTED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/departmentApi_setUp.sql")
@Sql(value = "classpath:data/departmentApi_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class DepartmentApiIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ApiHelper apiHelper;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private DepartmentMapper departmentMapper;

    private User user;

    private DepartmentDTO departmentDTO;

    private Department department;

    private ResourceFilter filter;

    @Before
    public void setUp() {
        user = new User();
        user.setId(1L);

        departmentDTO =
            new DepartmentDTO()
                .setName("department");

        department = new Department();
        department.setId(2L);

        filter =
            new ResourceFilter()
                .setSearchTerm("search")
                .setState(ACCEPTED);

        when(departmentService.createDepartment(user, 1L, departmentDTO)).thenReturn(department);
        when(departmentService.getDepartments(user, filter)).thenReturn(singletonList(department));
        when(departmentService.getDepartments(null, new ResourceFilter())).thenReturn(emptyList());
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(departmentService, departmentMapper);
    }

    @Test
    public void createDepartment_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            post("/api/universities/1/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization)
                .content(objectMapper.writeValueAsString(departmentDTO)))
            .andExpect(status().isOk());

        verify(departmentService, times(1)).createDepartment(user, 1L, departmentDTO);
        verify(departmentMapper, times(1)).apply(department);
    }

    @Test
    public void createDepartment_failureWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            post("/api/universities/1/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(departmentDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getDepartments_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/departments?searchTerm=search&state=ACCEPTED")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(departmentService, times(1)).getDepartments(user, filter);
        verify(departmentMapper, times(1)).apply(department);
    }

    @Test
    public void getDepartments_successWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/departments")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(departmentService, times(1)).getDepartments(null, new ResourceFilter());
    }

}
