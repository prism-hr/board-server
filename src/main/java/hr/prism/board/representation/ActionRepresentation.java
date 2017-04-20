package hr.prism.board.representation;

import com.google.common.base.MoreObjects;
import hr.prism.board.domain.Scope;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

public class ActionRepresentation implements Comparable<ActionRepresentation> {
    
    private Action action;

    private Scope scope;

    private State state;

    public ResourceAction(Action action, Scope scope, State state) {
        this.action = action;
        this.scope = scope;
        this.state = state;
    }

    public ResourceAction(Action action, State state) {
        this.action = action;
        this.state = state;
    }

    public ResourceAction(Action action) {
        this.action = action;
    }

    public ResourceAction() {
    }

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("action", action)
            .add("scope", scope)
            .add("state", state)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAction that = (ResourceAction) o;
        return action == that.action &&
            scope == that.scope &&
            state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, scope, state);
    }
    
}
