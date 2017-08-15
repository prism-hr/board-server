package hr.prism.board.representation;

import hr.prism.board.enums.Action;

import java.time.LocalDateTime;

public class ResourceOperationRepresentation {

    private Action action;

    private UserRepresentation user;

    private ChangeListRepresentation changeList;

    private String comment;

    private LocalDateTime createdTimestamp;

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

    public ChangeListRepresentation getChangeList() {
        return changeList;
    }

    public ResourceOperationRepresentation setChangeList(ChangeListRepresentation changeList) {
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

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public ResourceOperationRepresentation setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

}
