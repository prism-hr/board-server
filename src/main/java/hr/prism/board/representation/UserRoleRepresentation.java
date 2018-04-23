package hr.prism.board.representation;

import hr.prism.board.enums.RoleType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class UserRoleRepresentation<T extends UserRoleRepresentation> {

    private RoleType roleType;

    private UserRepresentation user;

    public UserRoleRepresentation(RoleType roleType) {
        this.roleType = roleType;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    @SuppressWarnings("unchecked")
    public T setRoleType(RoleType roleType) {
        this.roleType = roleType;
        return (T) this;
    }

    public UserRepresentation getUser() {
        return user;
    }

    @SuppressWarnings("unchecked")
    public T setUser(UserRepresentation user) {
        this.user = user;
        return (T) this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(roleType)
            .append(user)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        UserRoleRepresentation<?> that = (UserRoleRepresentation<?>) other;
        return new EqualsBuilder()
            .append(roleType, that.roleType)
            .append(user, that.user)
            .isEquals();
    }

}
