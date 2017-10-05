package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "resource_event_search", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_event_id", "search"}))
public class ResourceEventSearch extends Search {

    @ManyToOne
    @JoinColumn(name = "resource_event_id", nullable = false)
    private ResourceEvent resourceEvent;

    public ResourceEvent getResourceEvent() {
        return resourceEvent;
    }

    public void setResourceEvent(ResourceEvent resourceEvent) {
        this.resourceEvent = resourceEvent;
    }

}
