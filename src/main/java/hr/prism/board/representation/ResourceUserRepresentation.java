package hr.prism.board.representation;

import com.google.common.base.MoreObjects;
import hr.prism.board.domain.Role;

import java.util.Set;

public class ResourceUserRepresentation {

    private UserRepresentation user;

    private Set<Role> roles;

    public UserRepresentation getUser() {
        return user;
    }

    public ResourceUserRepresentation setUser(UserRepresentation user) {
        this.user = user;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public ResourceUserRepresentation setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("user", user)
            .add("roles", roles)
            .toString();
    }
}
