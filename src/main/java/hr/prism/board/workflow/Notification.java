package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;

import java.util.Map;

public class Notification {

    @JsonIgnore
    private Workflow workflow;

    @JsonIgnore
    private ObjectMapper objectMapper;

    private Scope scope;

    private Long userId;

    private Role role;

    private State state;

    private boolean excludingCreator = false;

    private hr.prism.board.enums.Notification notification;

    private Map<String, String> customProperties;

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

    public Long getUserId() {
        return userId;
    }

    public Notification setUserId(Long userId) {
        this.userId = userId;
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

    public hr.prism.board.enums.Notification getNotification() {
        return notification;
    }

    public Notification setNotification(hr.prism.board.enums.Notification notification) {
        this.notification = notification;
        return this;
    }

    public Workflow with(hr.prism.board.enums.Notification notification) {
        this.notification = notification;
        return this.workflow;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public Notification setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
        return this;
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
