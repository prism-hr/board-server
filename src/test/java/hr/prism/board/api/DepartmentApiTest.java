package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.service.DepartmentBadgeService;
import hr.prism.board.service.DepartmentDashboardService;
import hr.prism.board.service.DepartmentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentApiTest {

    @Mock
    private DepartmentService departmentService;

    @Mock
    private DepartmentDashboardService departmentDashboardService;

    @Mock
    private DepartmentBadgeService departmentBadgeService;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private ResourceOperationMapper resourceOperationMapper;

    @Mock
    private ObjectMapper objectMapper;

    private DepartmentApi departmentApi;

    @Before
    public void setUp() {
        departmentApi = new DepartmentApi(departmentService, departmentDashboardService, departmentBadgeService,
            departmentMapper, resourceOperationMapper, objectMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(departmentBadgeService, departmentDashboardService, departmentBadgeService,
            departmentMapper, resourceOperationMapper, objectMapper);
        reset(departmentBadgeService, departmentDashboardService, departmentBadgeService, departmentMapper,
            resourceOperationMapper, objectMapper);
    }

    @Test
    public void createDepartment_success() {
        Department department = new Department();
        department.setId(1L);

        DepartmentDTO departmentDTO =
            new DepartmentDTO()
                .setName("department");

        when(departmentService.createDepartment(1L, departmentDTO)).thenReturn(department);

        departmentApi.createDepartment(1L, departmentDTO);

        verify(departmentService, times(1)).createDepartment(1L, departmentDTO);
        verify(departmentMapper, times(1)).apply(department);
    }

}
