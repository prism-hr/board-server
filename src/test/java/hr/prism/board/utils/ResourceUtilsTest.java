package hr.prism.board.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.utils.ResourceUtils.suggestHandle;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ResourceUtilsTest {

    @Test
    public void shouldSuggestHandleForVeryShortText() {
        assertEquals("iamtiny", suggestHandle("IAmTiny"));
    }

    @Test
    public void shouldSuggestHandleForVeryLongText() {
        assertEquals("iamaveryverylongpieceofte", suggestHandle("IAmAVeryVeryLongPieceOfText"));
    }

    @Test
    public void shouldSuggestHandleForTextWithSpaces() {
        assertEquals("i-am-a-piece", suggestHandle("I am a piece"));
        assertEquals("i-am-a-piece-of-text-with", suggestHandle("I am a piece of text with spaces"));
        assertEquals("i-am-another-piece-of", suggestHandle("I am another piece of text with spaces"));
    }

}
