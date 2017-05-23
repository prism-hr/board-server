package hr.prism.board.dto;

import hr.prism.board.domain.Role;

import java.util.List;
import java.util.Set;

public class ResourceUsersDTO {
    
    private List<UserDTO> users;
    
    private Set<Role> roles;
    
    public List<UserDTO> getUsers() {
        return users;
    }
    
    public ResourceUsersDTO setUsers(List<UserDTO> users) {
        this.users = users;
        return this;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public ResourceUsersDTO setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }
    
}
