package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ResourceUserDTO {

    @NotNull
    @Valid
    private UserDTO user;

    @NotNull
    @Valid
    private UserRoleDTO role;

    public UserDTO getUser() {
        return user;
    }

    public ResourceUserDTO setUser(UserDTO user) {
        this.user = user;
        return this;
    }

    public UserRoleDTO getRole() {
        return role;
    }

    public ResourceUserDTO setRole(UserRoleDTO role) {
        this.role = role;
        return this;
    }
}
