package hr.prism.board.exception;

import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;

public class ExceptionUtils {
    
    public static <T extends ApiException> void verifyApiException(Class<T> exceptionClass, Runnable operation, ExceptionCode exceptionCode, TransactionStatus status) {
        ApiException apiException = null;
        try {
            operation.run();
            Assert.fail("ApiException not thrown");
        } catch (ApiException e) {
            apiException = e;
        }
        
        Assert.assertEquals(exceptionClass, apiException.getClass());
        Assert.assertEquals(exceptionCode, apiException.getExceptionCode());
        if (status != null) {
            status.setRollbackOnly();
        }
    }
    
}
