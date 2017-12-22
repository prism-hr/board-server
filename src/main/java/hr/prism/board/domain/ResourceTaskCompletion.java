package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@SuppressWarnings("unused")
@Table(name = "resource_task_completion", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_task_id", "user_id"}))
public class ResourceTaskCompletion extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_task_id", nullable = false)
    private ResourceTask resourceTask;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ResourceTask getResourceTask() {
        return resourceTask;
    }

    public ResourceTaskCompletion setResourceTask(ResourceTask resourceTask) {
        this.resourceTask = resourceTask;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ResourceTaskCompletion setUser(User user) {
        this.user = user;
        return this;
    }

}
