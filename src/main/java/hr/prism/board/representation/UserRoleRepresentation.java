package hr.prism.board.representation;

import hr.prism.board.enums.RoleType;

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

}
