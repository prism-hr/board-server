package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class BoardTest {

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

        Board board = new Board();
        board.setParent(department);
        board.setName("Career Opportunities");
        board.setCreatedTimestamp(LocalDateTime.of(2018, 1, 1, 0, 0));
        board.setIndexDataAndQuarter();

        assertEquals(
            "U516 C420 L535 C513 S520 T000 B230 C513 S520 D163 I500 T000 U200 C660 O163",
            board.getIndexData());

        assertEquals("20181", board.getQuarter());
    }

    @Test
    public void setIndexDataAndQuarter_successWhenEmpty() {
        Board board = new Board();
        board.setIndexDataAndQuarter();

        assertNull(board.getIndexData());
        assertNull(board.getQuarter());
    }

}
