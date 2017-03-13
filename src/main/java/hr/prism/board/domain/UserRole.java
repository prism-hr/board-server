package hr.prism.board.domain;

import javax.persistence.*;

@Entity
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
    
}
