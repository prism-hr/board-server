package hr.prism.board.domain;

import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@Table(name = "activity", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "user_role_id", "role"}))
public class Activity extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private Scope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity", nullable = false)
    private hr.prism.board.enums.Activity activity;

    @Column(name = "filter_by_category", nullable = false)
    private Boolean filterByCategory;

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

    public Scope getScope() {
        return scope;
    }

    public Activity setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public Activity setRole(Role role) {
        this.role = role;
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

}
