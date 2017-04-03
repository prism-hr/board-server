package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

public class ResourceAction {
    
    private Action action;
    
    private Scope scope;
    
    private State state;
    
    public Action getAction() {
        return action;
    }
    
    public ResourceAction setAction(Action action) {
        this.action = action;
        return this;
    }
    
    public Scope getScope() {
        return scope;
    }
    
    public ResourceAction setScope(Scope scope) {
        this.scope = scope;
        return this;
    }
    
    public State getState() {
        return state;
    }
    
    public ResourceAction setState(State state) {
        this.state = state;
        return this;
    }
    
}
