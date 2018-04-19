package hr.prism.board.representation;

import hr.prism.board.enums.Role;

import java.util.List;

import static hr.prism.board.enums.UserRoleType.STAFF;

public class StaffRepresentation extends NewUserRoleRepresentation<StaffRepresentation> {

    private List<Role> roles;

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

}
