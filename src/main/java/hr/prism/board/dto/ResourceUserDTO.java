package hr.prism.board.dto;

import hr.prism.board.domain.Role;

import java.util.Set;

public class ResourceUserDTO {

    private UserDTO user;

    private Set<Role> roles;

    public UserDTO getUser() {
        return user;
    }

    public ResourceUserDTO setUser(UserDTO user) {
        this.user = user;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public ResourceUserDTO setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }
}
