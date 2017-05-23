package hr.prism.board.dto;

import hr.prism.board.domain.Role;

import java.util.List;
import java.util.Set;

public class ResourceUserBulkDTO {

    private List<UserDTO> users;

    private Set<Role> roles;

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public ResourceUserBulkDTO setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }
}
