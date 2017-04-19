package hr.prism.board.service;

import hr.prism.board.TestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Optional;

@TestContext
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
