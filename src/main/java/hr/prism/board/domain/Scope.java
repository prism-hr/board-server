package hr.prism.board.domain;

public enum Scope {

    DEPARTMENT(Value.DEPARTMENT),
    BOARD(Value.BOARD),
    POST(Value.POST);

    public String value;

    Scope(String value) {
        this.value = value;
    }

    public static class Value {

        public static final String DEPARTMENT = "DEPARTMENT";

        public static final String BOARD = "BOARD";

        public static final String POST = "POST";

    }

}
