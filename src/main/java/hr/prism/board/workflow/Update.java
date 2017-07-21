package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;

@SuppressWarnings("unchecked")
public abstract class Update<T extends Update> {

    @JsonIgnore
    private Workflow workflow;

    @JsonIgnore
    private ObjectMapper objectMapper;

    private Scope scope;

    private Role role;

    private boolean excludingCreator = false;

    public Workflow getWorkflow() {
        return workflow;
    }

    public T setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        return (T) this;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public T setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return (T) this;
    }

    public Scope getScope() {
        return scope;
    }

    public T setScope(Scope scope) {
        this.scope = scope;
        return (T) this;
    }

    public Role getRole() {
        return role;
    }

    public T setRole(Role role) {
        this.role = role;
        return (T) this;
    }

    public boolean isExcludingCreator() {
        return excludingCreator;
    }

    public T setExcludingCreator(boolean excludingCreator) {
        this.excludingCreator = excludingCreator;
        return (T) this;
    }

    public T excludingCreator() {
        this.excludingCreator = true;
        return (T) this;
    }

    @Override
    public String toString() {
        if (this.objectMapper == null) {
            return super.toString();
        }

        try {
            return this.objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize " + this.getClass().getSimpleName().toLowerCase());
        }
    }

}
