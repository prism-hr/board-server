package hr.prism.board.dto;

import hr.prism.board.enums.Role;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

import static hr.prism.board.enums.RoleType.STAFF;

public class StaffDTO extends UserRoleDTO<StaffDTO> {

    @NotEmpty
    private List<Role> roles;

    public StaffDTO() {
        super(STAFF);
    }

    public List<Role> getRoles() {
        return roles;
    }

    public StaffDTO setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

}
