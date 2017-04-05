package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

public abstract class ResourceDTO<R extends ResourceDTO<R>> {
    
    private Long id;
    
    @NotEmpty
    private String name;
    
    public Long getId() {
        return id;
    }
    
    @SuppressWarnings("unchecked")
    public R setId(Long id) {
        this.id = id;
        return (R) this;
    }
    
    public String getName() {
        return name;
    }
    
    @SuppressWarnings("unchecked")
    public R setName(String name) {
        this.name = name;
        return (R) this;
    }
    
}
