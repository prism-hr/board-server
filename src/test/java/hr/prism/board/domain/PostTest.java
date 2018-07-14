package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class PostTest {

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

        Location location = new Location();
        location.setName("London, United Kingdom");

        Organization organization = new Organization();
        organization.setName("TransferWise");

        Post post = new Post();
        post.setParent(board);
        post.setName("Software Engineer");
        post.setSummary("Developing software in Java");
        post.setDescription("Working as part of an agile team, using Spring Boot and MySQL");
        post.setLocation(location);
        post.setOrganization(organization);
        post.setCreatedTimestamp(LocalDateTime.of(2018, 1, 1, 0, 0));
        post.setIndexDataAndQuarter();

        assertEquals(
            "U516 C420 L535 C513 S520 T000 B230 C513 S520 D163 I500 T000 U200 C660 O163 S136 E525 D141 " +
                "S136 I500 J100 W625 A200 P630 O100 A500 A240 T500 U252 S165 B300 A530 M240 L535 U533 K523 T652",
            post.getIndexData());

        assertEquals("20181", post.getQuarter());
    }

    @Test
    public void setIndexDataAndQuarter_successWhenEmpty() {
        Post post = new Post();
        post.setIndexDataAndQuarter();

        assertNull(post.getIndexData());
        assertNull(post.getQuarter());
    }

}
