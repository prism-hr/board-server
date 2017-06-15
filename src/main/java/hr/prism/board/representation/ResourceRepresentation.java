package hr.prism.board.representation;

import hr.prism.board.domain.Scope;
import hr.prism.board.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public class ResourceRepresentation {

    private Long id;

    private Scope scope;

    private String name;

    private String summary;

    private State state;

    private LocalDateTime createdTimestamp;

    private LocalDateTime updatedTimestamp;

    private List<ActionRepresentation> actions;

    public Long getId() {
        return id;
    }

    public ResourceRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public ResourceRepresentation setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public String getName() {
        return name;
    }

    public ResourceRepresentation setName(String name) {
        this.name = name;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public ResourceRepresentation setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public State getState() {
        return state;
    }

    public ResourceRepresentation setState(State state) {
        this.state = state;
        return this;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public ResourceRepresentation setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public ResourceRepresentation setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
        return this;
    }

    public List<ActionRepresentation> getActions() {
        return actions;
    }

    public ResourceRepresentation setActions(List<ActionRepresentation> actions) {
        this.actions = actions;
        return this;
    }

}
