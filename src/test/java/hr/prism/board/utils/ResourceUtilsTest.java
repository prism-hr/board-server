package hr.prism.board.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.utils.ResourceUtils.suggestHandle;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ResourceUtilsTest {

    @Test
    public void suggestHandle_successWhenSmallContiguousText() {
        assertEquals("iamtiny", suggestHandle("IAmTiny"));
    }

    @Test
    public void suggestHandle_successWhenLargeContiguousText() {
        assertEquals("iamaveryverylongpieceofte", suggestHandle("IAmAVeryVeryLongPieceOfText"));
    }

    @Test
    public void suggestHandle_successWhenSmallSpacedText() {
        assertEquals("i-am-a-piece", suggestHandle("I am a piece"));
    }

    @Test
    public void suggestHandle_successWhenLargeSpacedText() {
        assertEquals("i-am-a-piece-of-text-with", suggestHandle("I am a piece of text with spaces"));
    }

}
