package hr.prism.board.exception;

public class BoardDuplicateException extends BoardException {

    private Long id;

    public BoardDuplicateException(ExceptionCode exceptionCode, Long id) {
        super(exceptionCode);
        this.id = id;
    }

    public BoardDuplicateException(ExceptionCode exceptionCode, Throwable t, Long id) {
        super(exceptionCode, t);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
