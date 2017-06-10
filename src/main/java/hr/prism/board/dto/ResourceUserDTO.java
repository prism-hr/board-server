package hr.prism.board.dto;

import java.util.Set;

public class ResourceUserDTO {

    private UserDTO user;

    private Set<UserRoleDTO> roles;

    public UserDTO getUser() {
        return user;
    }

    public ResourceUserDTO setUser(UserDTO user) {
        this.user = user;
        return this;
    }

    public Set<UserRoleDTO> getRoles() {
        return roles;
    }

    public ResourceUserDTO setRoles(Set<UserRoleDTO> roles) {
        this.roles = roles;
        return this;
    }
}
