package hr.prism.board.exception;

public class BoardDuplicateException extends BoardException {

    private Long id;


    public BoardDuplicateException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode, message);
    }

    public BoardDuplicateException(ExceptionCode exceptionCode, String message, Long id) {
        super(exceptionCode, message);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
