package hr.prism.board.api;

import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DepartmentApi {
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private DepartmentMapper departmentMapper;
    
    @RequestMapping(value = "/departments", method = RequestMethod.GET)
    public List<DepartmentRepresentation> getDepartments() {
        return departmentService.getDepartments().stream().map(departmentMapper.create()).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.GET)
    public DepartmentRepresentation getDepartment(@PathVariable Long id) {
        return departmentMapper.create().apply(departmentService.getDepartment(id));
    }
    
    @RequestMapping(value = "/departments", method = RequestMethod.GET, params = "handle")
    public DepartmentRepresentation getDepartmentByHandle(@RequestParam String handle) {
        return departmentMapper.create().apply(departmentService.getDepartment(handle));
    }
    
    @RequestMapping(value = "/departments/{id}", method = RequestMethod.PUT)
    public void updateDepartment(@PathVariable Long id, @RequestBody @Valid DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(id, departmentDTO);
    }
    
}

