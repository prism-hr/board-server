package hr.prism.board.representation;

import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public class ResourceRepresentation<T extends ResourceRepresentation> {

    private Long id;

    private Scope scope;

    private String name;

    private State state;

    private LocalDateTime createdTimestamp;

    private LocalDateTime updatedTimestamp;

    private List<ActionRepresentation> actions;

    public Long getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public T setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public State getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    public T setState(State state) {
        this.state = state;
        return (T) this;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    @SuppressWarnings("unchecked")
    public T setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return (T) this;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    @SuppressWarnings("unchecked")
    public T setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
        return (T) this;
    }

    public List<ActionRepresentation> getActions() {
        return actions;
    }

    @SuppressWarnings("unchecked")
    public T setActions(List<ActionRepresentation> actions) {
        this.actions = actions;
        return (T) this;
    }

}
