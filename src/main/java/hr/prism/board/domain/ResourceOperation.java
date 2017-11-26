package hr.prism.board.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;

import hr.prism.board.enums.Action;

@Entity
@SuppressWarnings("unused")
@Table(name = "resource_operation")
@NamedEntityGraph(
    name = "resource.operation",
    attributeNodes = @NamedAttributeNode(value = "user"))
public class ResourceOperation extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
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

    public ResourceOperation setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public ResourceOperation setAction(Action action) {
        this.action = action;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ResourceOperation setUser(User user) {
        this.user = user;
        return this;
    }

    public String getChangeList() {
        return changeList;
    }

    public ResourceOperation setChangeList(String changeList) {
        this.changeList = changeList;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ResourceOperation setComment(String comment) {
        this.comment = comment;
        return this;
    }

}
