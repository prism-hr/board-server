package hr.prism.board.api;

import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class BoardApiTest {
    
    @Test
    public void shouldHandleApiException() {
        BoardApi boardApi = new BoardApi();
        Map<String, String> processedException = boardApi.handleException(new ApiException(ExceptionCode.DUPLICATE_BOARD));
        Assert.assertEquals("exceptionCode", processedException.keySet().iterator().next());
        Assert.assertEquals(ExceptionCode.DUPLICATE_BOARD.name(), processedException.values().iterator().next());
    }
    
    @Test
    public void shouldNotSuggestDuplicateHandle() {
        
    }
    
}
