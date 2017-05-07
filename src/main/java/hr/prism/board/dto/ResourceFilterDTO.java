package hr.prism.board.dto;

import hr.prism.board.domain.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ResourceFilterDTO {
    
    @ResourceFilter(
        parameter = "scope",
        statement = "resource.scope = :scope")
    private Scope scope;
    
    @ResourceFilter(
        parameter = "id",
        statement = "resource.id = :id")
    private Long id;
    
    @ResourceFilter(
        parameter = "handle",
        statement = "resource.handle = :handle")
    private String handle;
    
    @ResourceFilter(
        parameter = "parentId",
        statement = "resource.parent_id = :parentId")
    private Long parentId;
    
    private Boolean includePublicResources;
    
    private String orderStatement;
    
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
    
    public String getHandle() {
        return handle;
    }
    
    public ResourceFilterDTO setHandle(String handle) {
        this.handle = handle;
        return this;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public ResourceFilterDTO setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }
    
    public Boolean getIncludePublicResources() {
        return includePublicResources;
    }
    
    public ResourceFilterDTO setIncludePublicResources(Boolean includePublicResources) {
        this.includePublicResources = includePublicResources;
        return this;
    }
    
    public String getOrderStatement() {
        return orderStatement;
    }
    
    public ResourceFilterDTO setOrderStatement(String orderStatement) {
        this.orderStatement = orderStatement;
        return this;
    }
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResourceFilter {
        
        String parameter();
        
        String statement();
        
        boolean secured() default false;
        
    }
    
}
