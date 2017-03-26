package hr.prism.board.representation;

import hr.prism.board.domain.Role;
import hr.prism.board.enums.PostVisibility;

import java.util.List;

public class BoardRepresentation {
    
    private Long id;
    
    private String name;
    
    private String purpose;
    
    private String handle;
    
    private DepartmentRepresentation department;
    
    private List<String> postCategories;
    
    private PostVisibility defaultPostVisibility;
    
    private List<Role> roles;
    
    public Long getId() {
        return id;
    }
    
    public BoardRepresentation setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public BoardRepresentation setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public BoardRepresentation setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }
    
    public String getHandle() {
        return handle;
    }
    
    public BoardRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }
    
    public DepartmentRepresentation getDepartment() {
        return department;
    }
    
    public BoardRepresentation setDepartment(DepartmentRepresentation department) {
        this.department = department;
        return this;
    }
    
    public List<String> getPostCategories() {
        return postCategories;
    }
    
    public BoardRepresentation setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }
    
    public PostVisibility getDefaultPostVisibility() {
        return defaultPostVisibility;
    }
    
    public BoardRepresentation setDefaultPostVisibility(PostVisibility defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
        return this;
    }
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public BoardRepresentation setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }
    
}
