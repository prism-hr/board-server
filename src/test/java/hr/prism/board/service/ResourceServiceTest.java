package hr.prism.board.service;

import org.junit.Assert;
import org.junit.Test;

public class ResourceServiceTest {

    @Test
    public void shouldSuggestHandleForVeryShortText() {
        Assert.assertEquals("iamtiny", ResourceService.suggestHandle("IAmTiny"));
    }

    @Test
    public void shouldSuggestHandleForVeryLongText() {
        Assert.assertEquals("iamaveryverylongpieceofte", ResourceService.suggestHandle("IAmAVeryVeryLongPieceOfText"));
    }

    @Test
    public void shouldSuggestHandleForTextWithSpaces() {
        Assert.assertEquals("i-am-a-piece", ResourceService.suggestHandle("I am a piece"));
        Assert.assertEquals("i-am-a-piece-of-text-with", ResourceService.suggestHandle("I am a piece of text with spaces"));
        Assert.assertEquals("i-am-another-piece-of", ResourceService.suggestHandle("I am another piece of text with spaces"));
    }

}
