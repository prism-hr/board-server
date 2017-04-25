package hr.prism.board.enums;

public enum Action {
    
    VIEW(false),
    AUDIT(false),
    EDIT(true),
    EXTEND(true),
    ACCEPT(true),
    SUSPEND(true),
    CORRECT(true),
    REJECT(true),
    PUBLISH(true),
    RETIRE(true),
    RESTORE(true),
    WITHDRAW(true);
    
    private boolean resourceOperation;
    
    Action(boolean resourceOperation) {
        this.resourceOperation = resourceOperation;
    }
    
    public boolean isResourceOperation() {
        return resourceOperation;
    }
    
}
