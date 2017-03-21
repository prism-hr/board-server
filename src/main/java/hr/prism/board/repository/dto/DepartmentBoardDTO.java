package hr.prism.board.repository.dto;

import hr.prism.board.enums.PostVisibility;

public class DepartmentBoardDTO {
    
    private Long departmentId;
    
    private Long id;
    
    private String name;
    
    private String purpose;
    
    private String postCategories;
    
    private PostVisibility defaultPostVisibility;
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public DepartmentBoardDTO setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
        return this;
    }
    
    public Long getId() {
        return id;
    }
    
    public DepartmentBoardDTO setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public DepartmentBoardDTO setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public DepartmentBoardDTO setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }
    
    public String getPostCategories() {
        return postCategories;
    }
    
    public DepartmentBoardDTO setPostCategories(String postCategories) {
        this.postCategories = postCategories;
        return this;
    }
    
    public PostVisibility getDefaultPostVisibility() {
        return defaultPostVisibility;
    }
    
    public DepartmentBoardDTO setDefaultPostVisibility(PostVisibility defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
        return this;
    }
    
}
