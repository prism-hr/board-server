package hr.prism.board.representation;

import hr.prism.board.domain.ResourceAction;
import hr.prism.board.enums.State;

import java.util.List;

public class ResourceRepresentation {

    private Long id;

    private String name;

    private State state;

    private List<ResourceAction> actions;

    public Long getId() {
        return id;
    }

    public ResourceRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ResourceRepresentation setName(String name) {
        this.name = name;
        return this;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<ResourceAction> getActions() {
        return actions;
    }

    public ResourceRepresentation setActions(List<ResourceAction> actions) {
        this.actions = actions;
        return this;
    }
}
