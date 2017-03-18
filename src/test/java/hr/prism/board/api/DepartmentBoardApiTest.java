package hr.prism.board.api;

import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class DepartmentBoardApiTest {
    
    @Test
    public void shouldHandleApiException() {
        DepartmentBoardApi departmentBoardApi = new DepartmentBoardApi();
        Map<String, String> processedException = departmentBoardApi.handleException(new ApiException(ExceptionCode.DUPLICATE_BOARD));
        Assert.assertEquals("exceptionCode", processedException.keySet().iterator().next());
        Assert.assertEquals(ExceptionCode.DUPLICATE_BOARD.name(), processedException.values().iterator().next());
    }
    
}
