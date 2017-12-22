package hr.prism.board.representation;

import hr.prism.board.enums.ResourceTask;

public class ResourceTaskRepresentation {
    
    private Long id;
    
    private ResourceTask task;
    
    private Boolean completed;
    
    public Long getId() {
        return id;
    }
    
    public ResourceTaskRepresentation setId(Long id) {
        this.id = id;
        return this;
    }
    
    public ResourceTask getTask() {
        return task;
    }
    
    public ResourceTaskRepresentation setTask(ResourceTask task) {
        this.task = task;
        return this;
    }
    
    public Boolean getCompleted() {
        return completed;
    }
    
    public ResourceTaskRepresentation setCompleted(Boolean completed) {
        this.completed = completed;
        return this;
    }
    
}
