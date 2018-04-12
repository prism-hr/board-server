package hr.prism.board.exception;

public class BoardExceptionFactory {

    public static void throwFor(Class<? extends BoardException> exceptionClass, ExceptionCode exceptionCode,
                                String message) {
        if (exceptionClass == BoardException.class) {
            throw new BoardException(exceptionCode, message);
        } else if (exceptionClass == BoardForbiddenException.class) {
            throw new BoardForbiddenException(exceptionCode, message);
        }

        throw new BoardNotModifiedException(exceptionCode, message);
    }

}
