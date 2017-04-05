package hr.prism.board.representation;

import hr.prism.board.domain.Role;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

import java.util.List;

public class AbstractResourceRepresentation {
    
    private Long id;
    
    private String name;
    
    private State state;
    
    private List<Role> roles;
    
    private List<Action> actions;
    
    public Long getId() {
        return id;
    }
    
    public AbstractResourceRepresentation setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public AbstractResourceRepresentation setName(String name) {
        this.name = name;
        return this;
    }
    
    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        this.state = state;
    }
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public AbstractResourceRepresentation setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }
    
    public List<Action> getActions() {
        return actions;
    }
    
    public AbstractResourceRepresentation setActions(List<Action> actions) {
        this.actions = actions;
        return this;
    }
}
