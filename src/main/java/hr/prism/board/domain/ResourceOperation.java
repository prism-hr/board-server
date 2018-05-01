package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "resource_operation")
@NamedEntityGraph(
    name = "resource.operation",
    attributeNodes = @NamedAttributeNode(value = "user"))
public class ResourceOperation extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(STRING)
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

    @SuppressWarnings("UnusedReturnValue")
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

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ResourceOperation that = (ResourceOperation) other;
        return new EqualsBuilder()
            .append(resource, that.resource)
            .append(action, that.action)
            .append(user, that.user)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(resource)
            .append(action)
            .append(user)
            .toHashCode();
    }

}
