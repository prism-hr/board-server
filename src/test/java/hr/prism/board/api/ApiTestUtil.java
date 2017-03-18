package hr.prism.board.api;

import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;

class ApiTestUtil {
    
    static void verifyApiException(Runnable block, ExceptionCode expectedExceptionCode, TransactionStatus transactionStatus) {
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
    
}
