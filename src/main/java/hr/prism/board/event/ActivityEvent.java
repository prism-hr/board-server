package hr.prism.board.event;

import hr.prism.board.domain.BoardEntity;
import hr.prism.board.workflow.Activity;
import org.springframework.context.ApplicationEvent;

import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

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
        return reflectionHashCode(this);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object other) {
        return reflectionEquals(this, other);
    }

}
