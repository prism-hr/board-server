package hr.prism.board.domain;

public enum Scope {
    
    DEPARTMENT(Value.DEPARTMENT, Department.class),
    BOARD(Value.BOARD, Board.class),
    POST(Value.POST, Post.class);

    public String value;
    
    public Class<? extends Resource> resourceClass;
    
    Scope(String value, Class<? extends Resource> resourceClass) {
        this.value = value;
        this.resourceClass = resourceClass;
    }

    public static class Value {

        public static final String DEPARTMENT = "DEPARTMENT";

        public static final String BOARD = "BOARD";

        public static final String POST = "POST";

    }

}
