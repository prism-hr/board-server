package hr.prism.board.utils;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BoardUtilsTest {

    @Test
    public void makeSoundex_successWhenMixtureOfWordsPhrasesEnumsAndNumbers() {
        String soundex = makeSoundex(
            ImmutableList.of(
                "word",
                "phrase containing word",
                UNDERGRADUATE_STUDENT.name(),
                "2018"));

        assertEquals("W630 P620 C535 W630 U536 S335 2018", soundex);
    }

}
