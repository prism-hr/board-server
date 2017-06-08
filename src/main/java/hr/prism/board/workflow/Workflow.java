package hr.prism.board.workflow;

import com.google.common.base.Joiner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;

import static hr.prism.board.domain.Role.PUBLIC;

public class Workflow extends ArrayList<Workflow.Permission> {

    private Permission permission;

    private ObjectMapper objectMapper;

    public Workflow(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Permission permitThatAnybody() {
        Permission permission = new Permission().setRole(PUBLIC);
        this.permission = permission;
        add(permission);
        return permission;
    }

    public Permission permitThat(Scope scope, Role role) {
        Permission permission = new Permission().setResource1Scope(scope).setRole(role);
        this.permission = permission;
        add(permission);
        return permission;
    }

    public Permission creating(Scope scope) {
        return this.permission.setResource3Scope(scope);
    }

    public Workflow transitioningTo(State state) {
        this.permission.setResource3State(state);
        return this;
    }

    public Notification notifying(Scope scope, Role role) {
        return this.permission.addNotification(scope, role);
    }

    @Override
    public boolean add(Permission permission) {
        return super.add(permission.setWorkflow(this));
    }

    public class Permission {

        private Workflow workflow;

        private Scope resource1Scope;

        private Role role;

        private Scope resource2Scope;

        private State resource2State;

        private Action action;

        private Scope resource3Scope;

        private State resource3State;

        private List<Notification> notifications;

        public Permission setWorkflow(Workflow workflow) {
            this.workflow = workflow;
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

        public Notification addNotification(Scope scope, Role role) {
            if (this.notifications == null) {
                this.notifications = new ArrayList<>();
            }

            Notification notification = new Notification().setWorkflow(this.workflow).setScope(scope).setRole(role);
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
            } else {
                this.resource3State = state;
            }

            return this.workflow;
        }

        @Override
        public String toString() {
            List<String> values = new ArrayList<>();
            for (Object value : new Object[]{resource1Scope, role, resource2Scope, resource2State, action, resource3Scope, resource3State, notifications}) {
                String valueString;
                if (value == null) {
                    valueString = "NULL";
                } else {
                    try {
                        valueString = "'" + objectMapper.writeValueAsString(value) + "'";
                    } catch (JsonProcessingException e) {
                        throw new ApiException(ExceptionCode.PROBLEM, e);
                    }
                }

                values.add(valueString);
            }

            return "(" + Joiner.on(", ").join(values) + ")";
        }

    }

    public static class Notification {

        private Workflow workflow;

        private Scope scope;

        private Role role;

        private String notification;

        public Notification setWorkflow(Workflow workflow) {
            this.workflow = workflow;
            return this;
        }

        public Scope getScope() {
            return scope;
        }

        public Notification setScope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Role getRole() {
            return role;
        }

        public Notification setRole(Role role) {
            this.role = role;
            return this;
        }

        public String getNotification() {
            return notification;
        }

        public Workflow with(String notification) {
            this.notification = notification;
            return this.workflow;
        }

    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(this);
    }

}
