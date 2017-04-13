package hr.prism.board.enums;

public enum RelationWithDepartment {
    
    /**
     * No questions needed
     */
    STAFF,
    
    /**
     * 1) When did you start
     * 2) When will you graduate
     * 3) What is your study level (member category)
     */
    STUDENT,
    
    /**
     * 1) When did you start
     * 2) When did you leave
     */
    PREVIOUS_STAFF,
    
    /**
     * 1) When did you start
     * 2) When did you graduate
     * 3) What was your study level (member category - could be more than one)
     */
    PREVIOUS_STUDENT,
    
    /**
     * 1) What you do with us
     * 2) Who do you work with
     */
    COLLABORATOR,
    
    /**
     * 1) Please explain - if you are contacting us for the first time, tell us what you offer
     */
    OTHER

}
