package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.AgeRange.NINETEEN_TWENTYFOUR;
import static hr.prism.board.enums.Gender.FEMALE;
import static org.junit.Assert.*;

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
    public void setIndexData_successWhenEmpty() {
        assertNull(new User().setIndexData().getIndexData());
    }

    @Test
    public void isResponseDataIncomplete_successWhenEmpty() {
        assertTrue(new User().isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoGender() {
        assertTrue(
            new User()
                .setAgeRange(NINETEEN_TWENTYFOUR)
                .setLocationNationality(new Location())
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoAgeRange() {
        assertTrue(
            new User()
                .setGender(FEMALE)
                .setLocationNationality(new Location())
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoLocation() {
        assertTrue(
            new User()
                .setGender(FEMALE)
                .setAgeRange(NINETEEN_TWENTYFOUR)
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenComplete() {
        assertFalse(
            new User()
                .setGender(FEMALE)
                .setAgeRange(NINETEEN_TWENTYFOUR)
                .setLocationNationality(new Location())
                .isResponseDataIncomplete());
    }

}
