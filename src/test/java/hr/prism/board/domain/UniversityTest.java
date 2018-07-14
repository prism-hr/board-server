package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class UniversityTest {

    @Test
    public void setIndexData_success() {
        University university = new University();
        university.setName("University College London");
        university.setCreatedTimestamp(LocalDateTime.of(2018, 1, 1, 0, 0));
        university.setIndexDataAndQuarter();

        assertEquals("U516 C420 L535", university.getIndexData());
        assertEquals("20181", university.getQuarter());
    }

    @Test
    public void setIndexDataAndQuarter_successWhenEmpty() {
        University university = new University();
        university.setIndexDataAndQuarter();

        assertNull(university.getIndexData());
        assertNull(university.getQuarter());
    }

}
