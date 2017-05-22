package hr.prism.board.enums;

public enum Action {
    
    VIEW(false),
    AUDIT(false),
    EDIT(true),
    EXTEND(true),
    ACCEPT(true), // Comment optional
    SUSPEND(true), // Comment required
    CORRECT(true), // Comment optional
    REJECT(true), // Comment required
    PUBLISH(true),
    RETIRE(true),
    RESTORE(true), // Comment optional for restore from reject, no comment for restore from withdrawn
    WITHDRAW(true);
    
    private boolean resourceOperation;
    
    Action(boolean resourceOperation) {
        this.resourceOperation = resourceOperation;
    }
    
    public boolean isResourceOperation() {
        return resourceOperation;
    }
    
}
