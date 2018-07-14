package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {

    @Test
    public void setIndexData_success() {
        User user =
            new User()
                .setGivenName("Alastair")
                .setSurname("Knowles")
                .setEmail("alastair@prism.hr")
                .setIndexData();

        assertEquals("A423 K542 alastair@prism.hr", user.getIndexData());
    }

    @Test
    public void setIndexDataAndQuarter_successWhenEmpty() {
        assertNull(new User().setIndexData().getIndexData());
    }

}
