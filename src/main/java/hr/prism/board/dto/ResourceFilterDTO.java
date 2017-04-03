package hr.prism.board.dto;

import hr.prism.board.domain.Scope;

public class ResourceFilterDTO {
    
    @ResourceFilter(column = "resource.scope", placeholder = ":scope")
    private Scope scope;
    
    @ResourceFilter(column = "resource.id", placeholder = ":id")
    private Long id;
    
    @ResourceFilter(column = "parent.id", placeholder = ":parentId")
    private Long parentId;
    
    public Scope getScope() {
        return scope;
    }
    
    public ResourceFilterDTO setScope(Scope scope) {
        this.scope = scope;
        return this;
    }
    
    public Long getId() {
        return id;
    }
    
    public ResourceFilterDTO setId(Long id) {
        this.id = id;
        return this;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public ResourceFilterDTO setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }
    
}
