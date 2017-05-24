package hr.prism.board.permission;

import com.google.common.base.Joiner;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

import java.util.ArrayList;
import java.util.List;

import static hr.prism.board.domain.Role.PUBLIC;

public class Permissions extends ArrayList<Permissions.Permit> {
    
    private Permit permit;
    
    public Permissions.Permit permitThatAnybody() {
        Permit permit = new Permit().setRole(PUBLIC);
        this.permit = permit;
        add(permit);
        return permit;
    }
    
    public Permissions.Permit permitThat(Scope scope, Role role) {
        Permit permit = new Permit().setResource1Scope(scope).setRole(role);
        this.permit = permit;
        add(permit);
        return permit;
    }
    
    public Permissions.Permit creating(Scope scope) {
        return this.permit.setResource3Scope(scope);
    }
    
    public Permissions transitioningTo(State state) {
        this.permit.setResource3State(state);
        return this;
    }
    
    public Permissions.Permit notifying(Role role) {
        return this.permit.setRole2(role);
    }
    
    @Override
    public boolean add(Permit permit) {
        return super.add(permit.setPermissions(this));
    }
    
    static class Permit {
        
        private Permissions permissions;
        
        private Scope resource1Scope;
        
        private Role role;
        
        private Scope resource2Scope;
        
        private State resource2State;
        
        private Action action;
        
        private Scope resource3Scope;
        
        private State resource3State;
    
        private Role role2;
    
        private String notification;
        
        public Permit setPermissions(Permissions permissions) {
            this.permissions = permissions;
            return this;
        }
        
        public Permit setResource1Scope(Scope resource1Scope) {
            this.resource1Scope = resource1Scope;
            return this;
        }
        
        public Role getRole() {
            return role;
        }
        
        public Permit setRole(Role role) {
            this.role = role;
            return this;
        }
        
        public Permit setResource3Scope(Scope resource3Scope) {
            this.resource3Scope = resource3Scope;
            return this;
        }
        
        public Permit setResource3State(State resource3State) {
            this.resource3State = resource3State;
            return this;
        }
    
        public Permit setRole2(Role role2) {
            this.role2 = role2;
            return this;
        }
    
        public Permit setNotification(String notification) {
            this.notification = notification;
            return this;
        }
        
        public Permit can(Action action, Scope scope) {
            this.resource2Scope = scope;
            if (this.resource1Scope == null) {
                this.resource1Scope = scope;
            }
            
            this.action = action;
            return this;
        }
        
        public Permissions inState(State state) {
            if (this.resource2State == null) {
                this.resource2State = state;
            } else {
                this.resource3State = state;
            }
            
            return this.permissions;
        }
    
        public Permissions with(String notification) {
            this.notification = notification;
            return this.permissions;
        }
        
        @Override
        public String toString() {
            List<String> values = new ArrayList<>();
            for (Object value : new Object[]{resource1Scope, role, resource2Scope, resource2State, action, resource3Scope, resource3State, role2, notification}) {
                String valueString;
                if (value == null) {
                    valueString = "NULL";
                } else {
                    valueString = "'" + value.toString() + "'";
                }
        
                values.add(valueString);
            }
    
            return "(" + Joiner.on(", ").join(values) + ")";
        }
        
    }
    
    @Override
    public String toString() {
        return Joiner.on(", ").join(this);
    }
    
}
