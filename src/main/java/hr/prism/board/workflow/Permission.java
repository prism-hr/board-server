package hr.prism.board.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;

import java.util.ArrayList;
import java.util.List;

public class Permission {

    private Workflow workflow;

    private ObjectMapper objectMapper;

    private Scope resource1Scope;

    private Role role;

    private Scope resource2Scope;

    private State resource2State;

    private Action action;

    private Scope resource3Scope;

    private State resource3State;

    private State resource4State;

    private List<Notification> notifications;

    public Permission setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        return this;
    }

    public Permission setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public Permission setResource1Scope(Scope resource1Scope) {
        this.resource1Scope = resource1Scope;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public Permission setRole(Role role) {
        this.role = role;
        return this;
    }

    public Permission setResource3Scope(Scope resource3Scope) {
        this.resource3Scope = resource3Scope;
        return this;
    }

    public Permission setResource3State(State resource3State) {
        this.resource3State = resource3State;
        return this;
    }

    public Permission setResource4State(State resource4State) {
        this.resource4State = resource4State;
        return this;
    }

    public Notification addNotification(Scope scope, Role role) {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }

        Notification notification = new Notification().setWorkflow(this.workflow).setObjectMapper(this.objectMapper).setScope(scope).setRole(role);
        this.notifications.add(notification);
        return notification;
    }

    public Permission can(Action action, Scope scope) {
        this.resource2Scope = scope;
        if (this.resource1Scope == null) {
            this.resource1Scope = scope;
        }

        this.action = action;
        return this;
    }

    public Workflow inState(State state) {
        if (this.resource2State == null) {
            this.resource2State = state;
        } else if (this.resource3State == null) {
            this.resource3State = state;
        } else {
            this.resource4State = state;
        }

        return this.workflow;
    }

    @Override
    public String toString() {
        List<String> values = new ArrayList<>();
        for (Object value : new Object[]{resource1Scope, role, resource2Scope, resource2State, action, resource3Scope, resource3State, resource4State, notifications}) {
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
