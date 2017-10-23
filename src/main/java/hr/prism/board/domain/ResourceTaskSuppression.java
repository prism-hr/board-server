package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "resource_task_suppression", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_task_id", "user_id"}))
public class ResourceTaskSuppression extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_task_id", nullable = false)
    private ResourceTask resourceTask;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ResourceTask getResourceTask() {
        return resourceTask;
    }

    public ResourceTaskSuppression setResourceTask(ResourceTask resourceTask) {
        this.resourceTask = resourceTask;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ResourceTaskSuppression setUser(User user) {
        this.user = user;
        return this;
    }

}
