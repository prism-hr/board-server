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

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @SortNatural
    @OneToMany(mappedBy = "userRole")
    private SortedSet<UserRoleCategory> categories = new TreeSet<>();

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

    public Set<UserRoleCategory> getCategories() {
        return categories;
    }
}
