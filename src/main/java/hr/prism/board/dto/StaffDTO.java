package hr.prism.board.dto;

import hr.prism.board.enums.Role;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class StaffDTO {

    @Valid
    @NotNull
    private UserDTO user;

    @NotEmpty
    private List<Role> roles;

    public UserDTO getUser() {
        return user;
    }

    public StaffDTO setUser(UserDTO user) {
        this.user = user;
        return this;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public StaffDTO setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

}
