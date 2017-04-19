package hr.prism.board.service;

import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Optional;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class DepartmentServiceIT {
    
    @Inject
    private DepartmentService departmentService;
    
    private Department department = new Department();
    
    @Test
    public void shouldNotBeAbleToPatchDepartmentWithNullName() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                departmentService.updateDepartment(department, new DepartmentPatchDTO().setName(Optional.empty())),
            ExceptionCode.MISSING_DEPARTMENT_NAME, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchDepartmentWithNullHandle() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                departmentService.updateDepartment(department, new DepartmentPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
            ExceptionCode.MISSING_DEPARTMENT_HANDLE, null);
    }
    
}
