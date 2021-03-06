package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentBadgeOptionsDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.service.DepartmentBadgeService;
import hr.prism.board.service.DepartmentService;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceFilter.ResourceFilterList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static hr.prism.board.enums.BadgeListType.SLIDER;
import static hr.prism.board.enums.BadgeType.LIST;
import static hr.prism.board.enums.State.ACCEPTED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/api_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
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
    private DepartmentBadgeService departmentBadgeService;

    @MockBean
    private DepartmentMapper departmentMapper;

    private User user;

    private DepartmentDTO departmentDTO;

    private Department department;

    private ResourceFilter filter;

    private DepartmentBadgeOptionsDTO departmentBadgeOptions;

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
                .setState(ResourceFilterList.of(ACCEPTED));

        departmentBadgeOptions =
            new DepartmentBadgeOptionsDTO()
                .setBadgeType(LIST)
                .setBadgeListType(SLIDER)
                .setPostCount(1)
                .setPreview(true);

        when(departmentService.createDepartment(user, 1L, departmentDTO)).thenReturn(department);
        when(departmentService.getDepartments(user, filter)).thenReturn(singletonList(department));
        when(departmentService.getDepartments(null, new ResourceFilter())).thenReturn(emptyList());
        when(departmentService.getById(any(User.class), eq(2L))).thenReturn(department);
        when(departmentService.getByHandle(any(User.class), eq("university/department"))).thenReturn(department);

        when(departmentBadgeService.getBadge(any(User.class), eq(2L),
            eq(departmentBadgeOptions))).thenReturn("department badge string");
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

    @Test
    public void getDepartment_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/departments/2")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(departmentService, times(1)).getById(user, 2L);
        verify(departmentMapper, times(1)).apply(department);
    }

    @Test
    public void getDepartment_successWhenUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/departments/2")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(departmentService, times(1)).getById(null, 2L);
        verify(departmentMapper, times(1)).apply(department);
    }

    @Test
    public void getDepartment_successWhenHandleAndAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        mockMvc.perform(
            get("/api/departments?handle=university/department")
                .contentType(APPLICATION_JSON_UTF8)
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(departmentService, times(1)).getByHandle(user, "university/department");
        verify(departmentMapper, times(1)).apply(department);
    }

    @Test
    public void getDepartment_successWhenHandleAndUnauthenticated() throws Exception {
        mockMvc.perform(
            get("/api/departments?handle=university/department")
                .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        verify(departmentService, times(1))
            .getByHandle(null, "university/department");
        verify(departmentMapper, times(1)).apply(department);
    }

    @Test
    public void getDepartmentBadge_successWhenAuthenticated() throws Exception {
        String authorization = apiHelper.login("alastair@prism.hr", "password");

        String response =
            mockMvc.perform(
                get("/api/departments/2/badge" +
                    "?badgeType=LIST&badgeListType=SLIDER&postCount=1&preview=true&callback=departmentBadge")
                    .contentType(APPLICATION_JSON_UTF8)
                    .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("/**/departmentBadge(\"department badge string\");", response);
        verify(departmentBadgeService, times(1)).getBadge(user, 2L, departmentBadgeOptions);
    }

    @Test
    public void getDepartmentBadge_successWhenUnauthenticated() throws Exception {
        String response =
            mockMvc.perform(
                get("/api/departments/2/badge" +
                    "?badgeType=LIST&badgeListType=SLIDER&postCount=1&preview=true&callback=departmentBadge")
                    .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("/**/departmentBadge(\"department badge string\");", response);
        verify(departmentBadgeService, times(1))
            .getBadge(null, 2L, departmentBadgeOptions);
    }

}
