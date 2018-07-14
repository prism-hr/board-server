package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentTest {

    @Test
    public void setIndexData_success() {
        University university = new University();
        university.setName("University College London");
        university.setCreatedTimestamp(LocalDateTime.of(2018, 1, 1, 0, 0));
        university.setIndexDataAndQuarter();

        Department department = new Department();
        department.setParent(university);
        department.setName("Computer Science");
        department.setSummary("The best computer science department in the UK");
        department.setCreatedTimestamp(LocalDateTime.of(2018, 1, 1, 0, 0));
        department.setIndexDataAndQuarter();

        assertEquals(
            "U516 C420 L535 C513 S520 T000 B230 C513 S520 D163 I500 T000 U200",
            department.getIndexData());

        assertEquals("20181", department.getQuarter());
    }

    @Test
    public void setIndexDataAndQuarter_successWhenEmpty() {
        Department department = new Department();
        department.setIndexDataAndQuarter();

        assertNull(department.getIndexData());
        assertNull(department.getQuarter());
    }

}
