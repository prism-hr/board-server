package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(use = NAME, include = EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = Activity.class, name = Update.ACTIVITY),
    @Type(value = Notification.class, name = Update.NOTIFICATION)})
public abstract class Update<T extends Update> {

    static final String ACTIVITY = "activity";

    static final String NOTIFICATION = "notification";

    @JsonIgnore
    private Workflow workflow;

    @JsonIgnore
    private ObjectMapper objectMapper;

    private String type;

    private Scope scope;

    private Role role;

    @JsonIgnore
    private Long userId;

    public Workflow getWorkflow() {
        return workflow;
    }

    @SuppressWarnings("unchecked")
    public T setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        return (T) this;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @SuppressWarnings("unchecked")
    public T setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return (T) this;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public T setType(String type) {
        this.type = type;
        return (T) this;
    }

    public Scope getScope() {
        return scope;
    }

    @SuppressWarnings("unchecked")
    public T setScope(Scope scope) {
        this.scope = scope;
        return (T) this;
    }

    public Role getRole() {
        return role;
    }

    @SuppressWarnings("unchecked")
    public T setRole(Role role) {
        this.role = role;
        return (T) this;
    }

    public Long getUserId() {
        return userId;
    }

    @SuppressWarnings("unchecked")
    public T setUserId(Long userId) {
        this.userId = userId;
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
