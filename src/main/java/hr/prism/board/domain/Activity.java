package hr.prism.board.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@SuppressWarnings("unused")
@Table(name = "activity", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "user_role_id", "resource_event_id", "activity"}))
public class Activity extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @OneToOne
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    @OneToOne
    @JoinColumn(name = "resource_event_id")
    private ResourceEvent resourceEvent;

    @OneToOne
    @JoinColumn(name = "resource_task_id")
    private ResourceTask resourceTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity", nullable = false)
    private hr.prism.board.enums.Activity activity;

    @Column(name = "filter_by_category", nullable = false)
    private Boolean filterByCategory;

    @OneToMany(mappedBy = "activity")
    private Set<ActivityRole> activityRoles = new HashSet<>();

    @OneToMany(mappedBy = "activity")
    private Set<ActivityUser> activityUsers = new HashSet<>();

    @Transient
    private boolean viewed;

    public Resource getResource() {
        return resource;
    }

    public Activity setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public Activity setUserRole(UserRole userRole) {
        this.userRole = userRole;
        return this;
    }

    public ResourceEvent getResourceEvent() {
        return resourceEvent;
    }

    public Activity setResourceEvent(ResourceEvent resourceEvent) {
        this.resourceEvent = resourceEvent;
        return this;
    }

    public ResourceTask getResourceTask() {
        return resourceTask;
    }

    public Activity setResourceTask(ResourceTask resourceTask) {
        this.resourceTask = resourceTask;
        return this;
    }

    public hr.prism.board.enums.Activity getActivity() {
        return activity;
    }

    public Activity setActivity(hr.prism.board.enums.Activity activity) {
        this.activity = activity;
        return this;
    }

    public Boolean getFilterByCategory() {
        return filterByCategory;
    }

    public Activity setFilterByCategory(Boolean filterByCategory) {
        this.filterByCategory = filterByCategory;
        return this;
    }

    public Set<ActivityRole> getActivityRoles() {
        return activityRoles;
    }

    public Set<ActivityUser> getActivityUsers() {
        return activityUsers;
    }

    public boolean isViewed() {
        return viewed;
    }

    public Activity setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }
}
