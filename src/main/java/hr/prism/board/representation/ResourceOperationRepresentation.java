package hr.prism.board.representation;

import hr.prism.board.enums.Action;

public class ResourceOperationRepresentation {
    
    private Action action;
    
    private UserRepresentation user;
    
    private ResourceChangeListRepresentation changeList;
    
    private String comment;
    
    public Action getAction() {
        return action;
    }
    
    public ResourceOperationRepresentation setAction(Action action) {
        this.action = action;
        return this;
    }
    
    public UserRepresentation getUser() {
        return user;
    }
    
    public ResourceOperationRepresentation setUser(UserRepresentation user) {
        this.user = user;
        return this;
    }
    
    public ResourceChangeListRepresentation getChangeList() {
        return changeList;
    }
    
    public ResourceOperationRepresentation setChangeList(ResourceChangeListRepresentation changeList) {
        this.changeList = changeList;
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public ResourceOperationRepresentation setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
}
