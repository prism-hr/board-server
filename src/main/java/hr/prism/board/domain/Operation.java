package hr.prism.board.domain;

import hr.prism.board.enums.Action;

import javax.persistence.*;

@Entity
@Table(name = "operation")
public class Operation extends BoardEntity {
    
    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;
    
    @Column(name = "action", nullable = false)
    private Action action;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "change_list")
    private String changeList;
    
    @Column(name = "comment")
    private String comment;
    
    public Resource getResource() {
        return resource;
    }
    
    public Operation setResource(Resource resource) {
        this.resource = resource;
        return this;
    }
    
    public Action getAction() {
        return action;
    }
    
    public Operation setAction(Action action) {
        this.action = action;
        return this;
    }
    
    public User getUser() {
        return user;
    }
    
    public Operation setUser(User user) {
        this.user = user;
        return this;
    }
    
    public String getChangeList() {
        return changeList;
    }
    
    public Operation setChangeList(String changeList) {
        this.changeList = changeList;
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public Operation setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
}
