package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import java.util.Optional;

public class DepartmentServiceTest {
    
    private DepartmentService departmentService = new DepartmentService();
    
    private Department department = new Department();
    
    @Test
    public void shouldNotBeAbleToPatchDepartmentWithNullName() {
        ExceptionUtil.verifyApiException(() ->
                departmentService.updateDepartment(department, new DepartmentPatchDTO().setName(Optional.empty())),
            ExceptionCode.MISSING_DEPARTMENT_NAME, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchDepartmentWithNullHandle() {
        ExceptionUtil.verifyApiException(() ->
                departmentService.updateDepartment(department, new DepartmentPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
            ExceptionCode.MISSING_DEPARTMENT_HANDLE, null);
    }
    
}
