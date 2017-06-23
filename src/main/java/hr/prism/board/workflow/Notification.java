package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;

public class Notification {

    @JsonIgnore
    private Workflow workflow;

    @JsonIgnore
    private ObjectMapper objectMapper;

    private Scope scope;

    private Role role;

    private State state;

    private boolean excludingCreator = false;

    private String template;

    public Notification setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        return this;
    }

    public Notification setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

    public State getState() {
        return state;
    }

    public Notification setState(State state) {
        this.state = state;
        return this;
    }

    public Notification whenTransitioningTo(State state) {
        this.state = state;
        return this;
    }

    public boolean isExcludingCreator() {
        return excludingCreator;
    }

    public Notification setExcludingCreator(boolean excludingCreator) {
        this.excludingCreator = excludingCreator;
        return this;
    }

    public Notification excludingCreator() {
        this.excludingCreator = true;
        return this;
    }

    public String getTemplate() {
        return template;
    }

    public Notification setTemplate(String template) {
        this.template = template;
        return this;
    }

    public Workflow with(String template) {
        this.template = template;
        return this.workflow;
    }

    @Override
    public String toString() {
        if (this.objectMapper == null) {
            return super.toString();
        }

        try {
            return this.objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize notification");
        }
    }

}
