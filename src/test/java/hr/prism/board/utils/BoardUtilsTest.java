package hr.prism.board.utils;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;

import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.utils.BoardUtils.makeAcademicYearStart;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BoardUtilsTest {

    @Test
    public void makeSoundex_successWhenSearchTerm() {
        String soundex = makeSoundex("undergraduate 19-24");
        assertEquals("U536 019 024", soundex);
    }

    @Test
    public void makeSoundex_successWhenMixtureOfWordsPhrasesEnumsObjectsAndNumbers() {
        Location location = new Location();
        location.setName("London, United Kingdom");

        String soundex = makeSoundex(
            ImmutableList.of(
                "word",
                "phrase containing word",
                UNDERGRADUATE_STUDENT,
                location,
                2018));

        assertEquals("W630 P620 C535 W630 U536 S335 L535 U533 K523 2018", soundex);
    }

    @Test
    public void makeAcademicYearStart_success() {
        LocalDate academicYearStart = makeAcademicYearStart();

        assertThat(academicYearStart).isLessThanOrEqualTo(now());
        assertEquals(10, academicYearStart.getMonthValue());
        assertEquals(1, academicYearStart.getDayOfMonth());
    }

}
