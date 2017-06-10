package hr.prism.board.representation;

import com.google.common.base.MoreObjects;

import java.util.Set;

public class ResourceUserRepresentation {

    private UserRepresentation user;

    private Set<UserRoleRepresentation> roles;

    public UserRepresentation getUser() {
        return user;
    }

    public ResourceUserRepresentation setUser(UserRepresentation user) {
        this.user = user;
        return this;
    }

    public Set<UserRoleRepresentation> getRoles() {
        return roles;
    }

    public ResourceUserRepresentation setRoles(Set<UserRoleRepresentation> roles) {
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
