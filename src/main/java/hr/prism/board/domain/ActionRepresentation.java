package hr.prism.board.domain;

import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import org.apache.commons.lang3.ObjectUtils;

public class ActionRepresentation implements Comparable<ActionRepresentation> {
    
    private Action action;
    
    private Scope scope;
    
    private State state;
    
    public Action getAction() {
        return action;
    }
    
    public ActionRepresentation setAction(Action action) {
        this.action = action;
        return this;
    }
    
    public Scope getScope() {
        return scope;
    }
    
    public ActionRepresentation setScope(Scope scope) {
        this.scope = scope;
        return this;
    }
    
    public State getState() {
        return state;
    }
    
    public ActionRepresentation setState(State state) {
        this.state = state;
        return this;
    }
    
    @Override
    public int compareTo(ActionRepresentation o) {
        int compare = ObjectUtils.compare(action, o.getAction());
        return compare == 0 ? ObjectUtils.compare(scope, o.getScope()) : compare;
    }
    
}
