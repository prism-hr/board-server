package hr.prism.board.service;

import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.dao.EntityDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.DepartmentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
public class DepartmentService {

    @Inject
    private UserService userService;

    @Inject
    private EntityDAO entityDAO;

    @Inject
    private DepartmentDAO departmentDAO;

    public Department createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setUser(userService.getCurrentUser());
        entityDAO.save(department);
        return department;
    }

    public List<Department> getDepartments() {
        return departmentDAO.getDepartments();
    }

}
