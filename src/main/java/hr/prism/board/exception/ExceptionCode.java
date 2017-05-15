package hr.prism.board.exception;

public enum ExceptionCode {
    
    PROBLEM,
    UNSUPPORTED_AUTHENTICATOR,
    UNIDENTIFIABLE_USER,
    UNREGISTERED_USER,
    UNAUTHENTICATED_USER,
    DUPLICATE_USER,
    FORBIDDEN_ACTION,
    
    DUPLICATE_DEPARTMENT,
    DUPLICATE_DEPARTMENT_HANDLE,
    
    DUPLICATE_BOARD,
    DUPLICATE_BOARD_HANDLE,
    
    MISSING_POST_EXISTING_RELATION,
    MISSING_POST_APPLY,
    MISSING_POST_MEMBER_CATEGORIES,
    MISSING_POST_POST_CATEGORIES,
    CORRUPTED_POST_APPLY,
    CORRUPTED_POST_EXISTING_RELATION_EXPLANATION,
    CORRUPTED_POST_MEMBER_CATEGORIES,
    CORRUPTED_POST_POST_CATEGORIES,
    INVALID_POST_MEMBER_CATEGORIES,
    INVALID_POST_POST_CATEGORIES
    
}
