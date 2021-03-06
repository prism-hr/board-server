package hr.prism.board.enums;

import hr.prism.board.domain.*;
import hr.prism.board.interceptor.PostStateChangeInterceptor;
import hr.prism.board.interceptor.StateChangeInterceptor;

public enum Scope {

    UNIVERSITY(Value.UNIVERSITY, University.class),
    DEPARTMENT(Value.DEPARTMENT, Department.class),
    BOARD(Value.BOARD, Board.class),
    POST(Value.POST, Post.class, PostStateChangeInterceptor.class);

    public String value;

    public Class<? extends Resource> resourceClass;

    public Class<? extends StateChangeInterceptor> stateChangeInterceptorClass;

    Scope(String value, Class<? extends Resource> resourceClass) {
        this.value = value;
        this.resourceClass = resourceClass;
    }

    Scope(String value, Class<? extends Resource> resourceClass,
          Class<? extends StateChangeInterceptor> stateChangeInterceptorClass) {
        this(value, resourceClass);
        this.stateChangeInterceptorClass = stateChangeInterceptorClass;
    }

    public static class Value {

        public static final String UNIVERSITY = "UNIVERSITY";

        public static final String DEPARTMENT = "DEPARTMENT";

        public static final String BOARD = "BOARD";

        public static final String POST = "POST";

    }

}
