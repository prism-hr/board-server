package hr.prism.board.workflow;

import com.google.common.base.Joiner;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.enums.State;

import static hr.prism.board.domain.Role.PUBLIC;

public class Workflow extends ArrayList<Permission> {

    private Permission permission;

    private ObjectMapper objectMapper;

    public Workflow(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Permission permitThatAnybody() {
        Permission permission = new Permission().setWorkflow(this).setObjectMapper(this.objectMapper).setRole(PUBLIC);
        this.permission = permission;
        add(permission);
        return permission;
    }

    public Permission permitThat(Scope scope, Role role) {
        Permission permission = new Permission().setWorkflow(this).setObjectMapper(this.objectMapper).setResource1Scope(scope).setRole(role);
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

    @Override
    public String toString() {
        return Joiner.on(", ").join(this);
    }

}