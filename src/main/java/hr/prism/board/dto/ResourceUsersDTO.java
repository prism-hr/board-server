package hr.prism.board.dto;

import java.util.List;
import java.util.Set;

public class ResourceUsersDTO {

    private List<UserDTO> users;

    private Set<UserRoleDTO> roles;

    public List<UserDTO> getUsers() {
        return users;
    }

    public ResourceUsersDTO setUsers(List<UserDTO> users) {
        this.users = users;
        return this;
    }

    public Set<UserRoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRoleDTO> roles) {
        this.roles = roles;
    }
}
