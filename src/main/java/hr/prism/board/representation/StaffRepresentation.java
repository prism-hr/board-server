package hr.prism.board.representation;

import hr.prism.board.enums.Role;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

import static hr.prism.board.enums.RoleType.STAFF;

public class StaffRepresentation extends UserRoleRepresentation<StaffRepresentation> {

    private List<Role> roles = new ArrayList<>();

    public StaffRepresentation() {
        super(STAFF);
    }

    public List<Role> getRoles() {
        return roles;
    }

    public StaffRepresentation setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public StaffRepresentation addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(roles)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        StaffRepresentation that = (StaffRepresentation) other;
        return new EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(roles, that.roles)
            .isEquals();
    }

}
