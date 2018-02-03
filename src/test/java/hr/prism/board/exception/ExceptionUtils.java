package hr.prism.board.exception;

import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;

public class ExceptionUtils {

    @SuppressWarnings("unchecked")
    public static <T extends BoardException> T verifyException(Class<T> exceptionClass, Runnable operation, ExceptionCode exceptionCode) {
        return verifyException(exceptionClass, operation, exceptionCode, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BoardException> T verifyException(Class<T> exceptionClass, Runnable operation, ExceptionCode exceptionCode, TransactionStatus status) {
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

        return (T) boardException;
    }

    public static void verifyDuplicateException(Runnable operation, ExceptionCode exceptionCode, Long id) {
        BoardDuplicateException boardDuplicateException = verifyException(BoardDuplicateException.class, operation, exceptionCode, null);
        Assert.assertEquals(id, boardDuplicateException.getId());
    }

    public static void verifyDuplicateException(Runnable operation, ExceptionCode exceptionCode, Long id, TransactionStatus status) {
        BoardDuplicateException boardDuplicateException = verifyException(BoardDuplicateException.class, operation, exceptionCode, status);
        Assert.assertEquals(id, boardDuplicateException.getId());
    }

}
