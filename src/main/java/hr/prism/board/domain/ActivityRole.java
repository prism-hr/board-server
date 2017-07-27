package hr.prism.board.domain;

import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@Table(name = "activity_role", uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "scope", "role"}))
public class ActivityRole extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private Scope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public Activity getActivity() {
        return activity;
    }

    public ActivityRole setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public ActivityRole setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public ActivityRole setRole(Role role) {
        this.role = role;
        return this;
    }

}
