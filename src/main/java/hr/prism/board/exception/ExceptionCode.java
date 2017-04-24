package hr.prism.board.exception;

public enum ExceptionCode {
    
    PROBLEM,
    UNAUTHENTICATED_USER,
    FORBIDDEN_ACTION,
    
    DUPLICATE_DEPARTMENT,
    DUPLICATE_DEPARTMENT_HANDLE,
    MISSING_DEPARTMENT_NAME,
    MISSING_DEPARTMENT_HANDLE,
    
    DUPLICATE_BOARD,
    DUPLICATE_BOARD_HANDLE,
    MISSING_BOARD_NAME,
    MISSING_BOARD_HANDLE,
    MISSING_BOARD_DEFAULT_VISIBILITY,
    
    MISSING_POST_NAME,
    MISSING_POST_DESCRIPTION,
    MISSING_POST_ORGANIZATION_NAME,
    MISSING_POST_EXISTING_RELATION,
    MISSING_POST_LOCATION,
    MISSING_POST_APPLY,
    CORRUPTED_POST_APPLY,
    CORRUPTED_POST_EXISTING_RELATION_EXPLANATION
    
}
