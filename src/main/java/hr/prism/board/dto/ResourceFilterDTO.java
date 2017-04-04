package hr.prism.board.dto;

import hr.prism.board.domain.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ResourceFilterDTO {
    
    @ResourceFilter(
        parameter = ":scope",
        statement = "resource.scope = :scope")
    private Scope scope;
    
    @ResourceFilter(
        parameter = ":id",
        statement = "resource.id = :id")
    private Long id;
    
    @ResourceFilter(
        parameter = ":userId",
        statement = "user_role.user_id = :userId",
        secured = true)
    private Long userId;
    
    @ResourceFilter(
        parameter = ":boardId",
        statement = "resource.id in (select resource2_id from resource_relation where resource1_id = :boardId)")
    private Long boardId;
    
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
    
    public Long getUserId() {
        return userId;
    }
    
    public ResourceFilterDTO setUserId(Long userId) {
        this.userId = userId;
        return this;
    }
    
    public Long getBoardId() {
        return boardId;
    }
    
    public ResourceFilterDTO setBoardId(Long boardId) {
        this.boardId = boardId;
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
