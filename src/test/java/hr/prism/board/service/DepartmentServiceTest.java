package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import java.util.Optional;

public class DepartmentServiceTest {
    
    private DepartmentService departmentService = new DepartmentService();
    
    private Department department = new Department();
    
    @Test
    public void shouldNotBeAbleToPatchDepartmentWithNullName() {
        ExceptionUtil.verifyIllegalStateException(() ->
                departmentService.updateDepartment(department, new DepartmentPatchDTO().setName(Optional.empty())),
            "Attempted to set department name to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchDepartmentWithNullHandle() {
        ExceptionUtil.verifyIllegalStateException(() ->
                departmentService.updateDepartment(department, new DepartmentPatchDTO().setName(Optional.of("name")).setHandle(Optional.empty())),
            "Attempted to set department handle to null");
    }
    
}
