package hr.prism.board.domain;

import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@NamedEntityGraph(
    name = "userRole.extended",
    attributeNodes = @NamedAttributeNode(value = "categories"))
@Table(name = "user_role", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "user_id", "role"}))
public class UserRole extends BoardEntity {

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @OneToOne(mappedBy = "userRole")
    private Activity activity;

    @SortNatural
    @OneToMany(mappedBy = "userRole")
    private SortedSet<UserRoleCategory> categories = new TreeSet<>();

    @Transient
    private boolean viewed;

    public String getUuid() {
        return uuid;
    }

    public UserRole setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Resource getResource() {
        return resource;
    }

    public UserRole setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserRole setUser(User user) {
        this.user = user;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRole setEmail(String email) {
        this.email = email;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public UserRole setRole(Role role) {
        this.role = role;
        return this;
    }

    public State getState() {
        return state;
    }

    public UserRole setState(State state) {
        this.state = state;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public UserRole setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public UserRole setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public Set<UserRoleCategory> getCategories() {
        return categories;
    }

    public boolean isViewed() {
        return viewed;
    }

    public UserRole setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

}
