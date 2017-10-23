package hr.prism.board.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resource_task", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "task"}))
public class ResourceTask {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "task", nullable = false)
    private hr.prism.board.enums.ResourceTask task;

    @Column(name = "notified_count")
    private Integer notifiedCount;

    @Column(name = "notified_timestamp")
    private LocalDateTime notifiedTimestamp;

    @OneToMany(mappedBy = "resourceTask")
    private Set<ResourceTaskSuppression> suppressions = new HashSet<>();

    public Resource getResource() {
        return resource;
    }

    public ResourceTask setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public hr.prism.board.enums.ResourceTask getTask() {
        return task;
    }

    public ResourceTask setTask(hr.prism.board.enums.ResourceTask task) {
        this.task = task;
        return this;
    }

    public Integer getNotifiedCount() {
        return notifiedCount;
    }

    public ResourceTask setNotifiedCount(Integer notifiedCount) {
        this.notifiedCount = notifiedCount;
        return this;
    }

    public LocalDateTime getNotifiedTimestamp() {
        return notifiedTimestamp;
    }

    public ResourceTask setNotifiedTimestamp(LocalDateTime notifiedTimestamp) {
        this.notifiedTimestamp = notifiedTimestamp;
        return this;
    }

    public Set<ResourceTaskSuppression> getSuppressions() {
        return suppressions;
    }

}
