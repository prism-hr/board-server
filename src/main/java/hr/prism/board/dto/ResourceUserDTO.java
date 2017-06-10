package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

public class ResourceUserDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
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
