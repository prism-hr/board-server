package hr.prism.board.representation;

import hr.prism.board.enums.UserRoleType;

public abstract class NewUserRoleRepresentation<T extends NewUserRoleRepresentation> {

    private UserRoleType userRoleType;

    private UserRepresentation user;

    public NewUserRoleRepresentation(UserRoleType userRoleType) {
        this.userRoleType = userRoleType;
    }

    public UserRoleType getUserRoleType() {
        return userRoleType;
    }

    @SuppressWarnings("unchecked")
    public T setUserRoleType(UserRoleType userRoleType) {
        this.userRoleType = userRoleType;
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
