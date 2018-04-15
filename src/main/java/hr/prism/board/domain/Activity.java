package hr.prism.board.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "activity", uniqueConstraints = @UniqueConstraint(
    columnNames = {"resource_id", "user_role_id", "resource_event_id", "activity"}))
@NamedNativeQuery(
    name = "userActivities",
    query =
        "SELECT activity.id as activityId " +
            "FROM activity " +
            "LEFT JOIN activity_role " +
            "ON activity.id = activity_role.activity_id " +
            "LEFT JOIN resource " +
            "ON activity.resource_id = resource.id " +
            "LEFT JOIN resource_relation " +
            "ON resource.id = resource_relation.resource2_id " +
            "LEFT JOIN resource AS parent_resource " +
            "ON resource_relation.resource1_id = parent_resource.id " +
            "LEFT JOIN user_role " +
            "ON parent_resource.id = user_role.resource_id " +
            "LEFT JOIN resource_category " +
            "ON resource.id = resource_category.resource_id " +
            "LEFT JOIN activity_user " +
            "ON activity.id = activity_user.activity_id " +
            "WHERE (activity_user.id IS NULL " +
            "AND activity_role.scope = parent_resource.scope " +
            "AND activity_role.role = user_role.role " +
            "AND user_role.user_id = :userId " +
            "AND user_role.state IN (:userRoleStates) " +
            "AND (activity.filter_by_category = 0 " +
            "OR resource_category.id IS NULL " +
            "OR resource_category.type = :categoryType " +
            "AND resource_category.name = user_role.member_category) " +
            "OR activity_user.user_id = :userId) " +
            "AND activity.id NOT IN (" +
            "SELECT activity_event.activity_id " +
            "FROM activity_event " +
            "WHERE activity_event.user_id = :userId " +
            "AND activity_event.event = :activityEvent) " +
            "ORDER BY activity.updated_timestamp DESC, activity.id DESC " +
            "LIMIT 25",
    resultSetMapping = "userActivities")

@SqlResultSetMapping(
    name = "userActivities",
    columns = @ColumnResult(
        name = "activityId",
        type = Long.class))
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

    @Enumerated(STRING)
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
