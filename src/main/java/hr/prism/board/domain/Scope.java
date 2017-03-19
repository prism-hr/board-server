package hr.prism.board.domain;

public enum Scope {
    
    DEPARTMENT(Value.DEPARTMENT),
    BOARD(Value.BOARD);
    
    public String value;
    
    Scope(String value) {
        this.value = value;
    }
    
    public static class Value {
        
        public static final String DEPARTMENT = "DEPARTMENT";
        
        public static final String BOARD = "BOARD";
        
    }
    
}
