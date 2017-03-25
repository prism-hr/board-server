package hr.prism.board.exception;

import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;

public class ExceptionUtil {
    
    public static void verifyApiException(Runnable block, ExceptionCode expectedExceptionCode, TransactionStatus transactionStatus) {
        ApiException apiException = null;
        try {
            block.run();
            Assert.fail("ApiException not thrown");
        } catch (ApiException e) {
            apiException = e;
        }
        
        Assert.assertEquals(expectedExceptionCode, apiException.getExceptionCode());
        if (transactionStatus != null) {
            transactionStatus.setRollbackOnly();
        }
    }
    
    public static void verifyApiForbiddenException(Runnable block, String expectedMessage) {
        ApiForbiddenException apiForbiddenException = null;
        try {
            block.run();
            Assert.fail("ApiForbiddenException not thrown");
        } catch (ApiForbiddenException e) {
            apiForbiddenException = e;
        }
        
        Assert.assertEquals(expectedMessage, apiForbiddenException.getMessage());
    }
    
    public static void verifyIllegalStateException(Runnable block, String expectedMessage) {
        IllegalStateException apiForbiddenException = null;
        try {
            block.run();
            Assert.fail("ApiForbiddenException not thrown");
        } catch (IllegalStateException e) {
            apiForbiddenException = e;
        }
        
        Assert.assertEquals(expectedMessage, apiForbiddenException.getMessage());
    }
    
}
