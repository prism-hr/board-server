package hr.prism.board.event;

import hr.prism.board.domain.BoardEntity;
import hr.prism.board.workflow.Activity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class ActivityEvent extends ApplicationEvent {

    private Long resourceId;

    private Class<? extends BoardEntity> entityClass;

    private Long entityId;

    private List<Activity> activities;

    public ActivityEvent(Object source, Long resourceId) {
        super(source);
        this.resourceId = resourceId;
    }

    public ActivityEvent(Object source, Long resourceId, List<Activity> activities) {
        super(source);
        this.resourceId = resourceId;
        this.activities = activities;
    }

    public ActivityEvent(Object source, Long resourceId, Class<? extends BoardEntity> entityClass, Long entityId) {
        super(source);
        this.resourceId = resourceId;
        this.entityClass = entityClass;
        this.entityId = entityId;
    }

    public ActivityEvent(Object source, Long resourceId, Class<? extends BoardEntity> entityClass, Long entityId,
                         List<Activity> activities) {
        super(source);
        this.resourceId = resourceId;
        this.entityClass = entityClass;
        this.entityId = entityId;
        this.activities = activities;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Class<? extends BoardEntity> getEntityClass() {
        return entityClass;
    }

    public Long getEntityId() {
        return entityId;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(resourceId)
            .append(entityClass)
            .append(entityId)
            .append(activities)
            .toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ActivityEvent that = (ActivityEvent) object;
        return new EqualsBuilder()
            .append(resourceId, that.resourceId)
            .append(entityClass, that.entityClass)
            .append(entityId, that.entityId)
            .append(activities, that.activities)
            .isEquals();
    }

}
