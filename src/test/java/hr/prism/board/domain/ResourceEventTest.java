package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.AgeRange.NINETEEN_TWENTYFOUR;
import static hr.prism.board.enums.Gender.FEMALE;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ResourceEventTest {

    @Test
    public void setIndexData_success() {
        Location locationNationality = new Location();
        locationNationality.setName("London, United Kingdom");

        ResourceEvent resourceEvent =
            new ResourceEvent()
                .setGender(FEMALE)
                .setAgeRange(NINETEEN_TWENTYFOUR)
                .setLocationNationality(locationNationality)
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("Computer Science")
                .setMemberYear(2018)
                .setIndexData();

        assertEquals("F540 019 024 L535 U533 K523 U536 S335 C513 S520 2018", resourceEvent.getIndexData());
    }

    @Test
    public void setIndexData_successWhenEmpty() {
        assertNull(new ResourceEvent().setIndexData().getIndexData());
    }

}
