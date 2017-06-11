package hr.prism.board.exception;

import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;

public class ExceptionUtils {

    public static <T extends BoardException> void verifyApiException(Class<T> exceptionClass, Runnable operation, ExceptionCode exceptionCode, TransactionStatus status) {
        BoardException boardException = null;
        try {
            operation.run();
            Assert.fail("ApiException not thrown");
        } catch (BoardException e) {
            boardException = e;
        }

        Assert.assertEquals(exceptionClass, boardException.getClass());
        Assert.assertEquals(exceptionCode, boardException.getExceptionCode());
        if (status != null) {
            status.setRollbackOnly();
        }
    }

}
