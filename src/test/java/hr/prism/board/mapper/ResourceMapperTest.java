package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.University;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static hr.prism.board.enums.Action.VIEW;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ResourceMapperTest {

    private ResourceMapper resourceMapper = new ResourceMapper();

    @Test
    public void apply_success() {
        LocalDateTime createdTimestamp = LocalDateTime.of(2018, 1, 1, 8, 0, 0);
        LocalDateTime updatedTimestamp = LocalDateTime.of(2018, 1, 1, 9, 0, 0);

        Department department = new Department();
        department.setId(1L);
        department.setScope(DEPARTMENT);
        department.setName("department");
        department.setState(ACCEPTED);
        department.setCreatedTimestamp(createdTimestamp);
        department.setUpdatedTimestamp(updatedTimestamp);
        department.setActions(singletonList(
            new ActionRepresentation().setAction(VIEW)));

        DepartmentRepresentation departmentRepresentation =
            resourceMapper.apply(department, DepartmentRepresentation.class);

        assertEquals(1L, departmentRepresentation.getId().longValue());
        assertEquals(DEPARTMENT, departmentRepresentation.getScope());
        assertEquals("department", departmentRepresentation.getName());
        assertEquals(ACCEPTED, departmentRepresentation.getState());
        assertEquals(createdTimestamp, departmentRepresentation.getCreatedTimestamp());
        assertEquals(updatedTimestamp, departmentRepresentation.getUpdatedTimestamp());

        assertThat(
            department.getActions().stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(VIEW);
    }

    @Test
    public void applySmall_success() {
        Department department = new Department();
        department.setId(1L);
        department.setScope(DEPARTMENT);
        department.setName("department");

        DepartmentRepresentation departmentRepresentation =
            resourceMapper.applySmall(department, DepartmentRepresentation.class);

        assertEquals(1L, departmentRepresentation.getId().longValue());
        assertEquals(DEPARTMENT, departmentRepresentation.getScope());
        assertEquals("department", departmentRepresentation.getName());
    }

    @Test
    public void getHandle_success() {
        University university = new University();
        university.setHandle("university");

        Department department = new Department();
        department.setHandle("university/department");

        String handle = resourceMapper.getHandle(department, university);
        assertEquals("department", handle);
    }

}
