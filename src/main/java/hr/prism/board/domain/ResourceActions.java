package hr.prism.board.domain;

import com.google.common.collect.HashMultimap;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

import java.util.Collection;

public class ResourceActions {
    
    private HashMultimap<Long, ResourceAction> resourceActions = HashMultimap.create();
    
    public Collection<Long> getIds() {
        return resourceActions.keySet();
    }
    
    public Collection<ResourceAction> getActions(Long id) {
        return resourceActions.get(id);
    }
    
    public void putAction(Long id, ResourceAction action) {
        resourceActions.put(id, action);
    }
    
    public boolean isEmpty() {
        return resourceActions.isEmpty();
    }
    
    public static class ResourceAction {
        
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
    
}
